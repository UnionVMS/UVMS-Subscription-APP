/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */

package eu.europa.ec.fisheries.uvms.subscription.service.mapper;

import eu.europa.ec.fisheries.uvms.commons.date.DateUtils;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.ec.fisheries.wsdl.subscription.module.*;
import eu.europa.ec.fisheries.wsdl.user.types.Channel;
import eu.europa.ec.fisheries.wsdl.user.types.EndPoint;
import eu.europa.ec.fisheries.wsdl.user.types.Organisation;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CustomMapper {

    private static final String UNKNOWN = "UNKNOWN";

    private CustomMapper(){

    }

    public static Map<String, Object> mapCriteriaToQueryParameters(SubscriptionDataQuery query) {

        Map<String, Object> queryParameters = new HashMap<>();

        queryParameters.put("enabled", true);
        queryParameters.put("endPoint", null);
        queryParameters.put("subscriptionType", null);
        queryParameters.put("accessibility", null);
        queryParameters.put("startDate", null);
        queryParameters.put("endDate", null);
        queryParameters.put("channel", null);
        queryParameters.put("name", null);
        queryParameters.put("description", null);
        queryParameters.put("organisation", null);
        queryParameters.put("messageType", null);

        MessageType messageType = query.getMessageType();
        if (messageType != null) {
            queryParameters.put("messageType", messageType.value());
        }

        List<SubscriptionDataCriteria> criteria = query.getCriteria();

        for (SubscriptionDataCriteria criterion : criteria) {

            CriteriaType criteriaType = criterion.getCriteria();
            String value = criterion.getValue();
            switch (criteriaType) {
                case SENDER:

                    queryParameters.put("organisation", Long.valueOf( value ));
                    break;

                case VESSEL:

                    break;

                case VALIDITY_PERIOD:
                    if (SubCriteriaType.START_DATE.equals(criterion.getSubCriteria())) {
                        queryParameters.put("startDate", DateUtils.parseToUTCDate(value, criterion.getValueType().value()));
                    } else if (SubCriteriaType.END_DATE.equals(criterion.getSubCriteria())) {
                        queryParameters.put("endDate", DateUtils.parseToUTCDate(value, criterion.getValueType().value()));
                    }
                    break;

                case AREA:

                    break;
                default:
            }
        }

        return queryParameters;

    }

    public static List<SubscriptionEntity> enrichSubscriptionList(List<SubscriptionEntity> resultList, List<Organisation> organisationList) {

        if (organisationList == null || organisationList.isEmpty()) {
            return resultList;
        }

        Map<Long, Organisation> organisationMap = organisationList.stream().collect(Collectors.toMap(Organisation::getId, o -> o));

        for (SubscriptionEntity subscription : resultList) {
            Organisation orgDomain = organisationMap.get(subscription.getOrganisation());
            if (orgDomain != null) {
                String fullOrgName = StringUtils.isNotEmpty(orgDomain.getParentOrganisation()) ? orgDomain.getParentOrganisation() + " / " + orgDomain.getName() : orgDomain.getName();
                subscription.setOrganisationName(fullOrgName);
                Optional<EndPoint> endpoint = orgDomain.getEndPoints().stream()
                        .filter(endPoint -> subscription.getEndPoint() == endPoint.getId())
                        .findFirst();
                subscription.setEndpointName(endpoint.map(EndPoint::getName).orElse(UNKNOWN));
                subscription.setChannelName(endpoint
                        .map(EndPoint::getChannels)
                        .flatMap(channelForSubscription(subscription))
                        .map(Channel::getDataFlow)
                        .orElse(UNKNOWN)
                );
            }
            else {
                subscription.setOrganisationName(UNKNOWN);
                subscription.setEndpointName(UNKNOWN);
                subscription.setChannelName(UNKNOWN);
            }
        }

        return resultList;
    }

    private static Function<List<Channel>,Optional<Channel>> channelForSubscription(SubscriptionEntity subscription) {
        return channels -> channels.stream()
                .filter(channel -> subscription.getChannel() == channel.getId())
                .findFirst();
    }
}
