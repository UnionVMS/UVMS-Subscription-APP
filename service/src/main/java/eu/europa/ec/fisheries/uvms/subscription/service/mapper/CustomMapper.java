/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */

package eu.europa.ec.fisheries.uvms.subscription.service.mapper;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import eu.europa.ec.fisheries.uvms.commons.date.DateUtils;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionOutput;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionSubscriber;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.list.SubscriptionListDto;
import eu.europa.ec.fisheries.wsdl.subscription.module.CriteriaType;
import eu.europa.ec.fisheries.wsdl.subscription.module.MessageType;
import eu.europa.ec.fisheries.wsdl.subscription.module.SubCriteriaType;
import eu.europa.ec.fisheries.wsdl.subscription.module.SubscriptionDataCriteria;
import eu.europa.ec.fisheries.wsdl.subscription.module.SubscriptionDataQuery;
import eu.europa.ec.fisheries.wsdl.user.types.Channel;
import eu.europa.ec.fisheries.wsdl.user.types.EndPoint;
import eu.europa.ec.fisheries.wsdl.user.types.Organisation;
import org.apache.commons.lang.StringUtils;

@ApplicationScoped
public class CustomMapper {

    private static final String UNKNOWN = "UNKNOWN";

    private SubscriptionMapper mapper;

    /**
     * Default constructor, required by frameworks.
     */
    @SuppressWarnings("unused")
    CustomMapper() {
        // NOOP
    }

    @Inject
    public CustomMapper(SubscriptionMapper mapper) {
        this.mapper = mapper;
    }

    public Map<String, Object> mapCriteriaToQueryParameters(SubscriptionDataQuery query) {

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

    public List<SubscriptionListDto> enrichSubscriptionList(List<SubscriptionEntity> resultList, List<Organisation> organisationList) {

        if (organisationList == null || organisationList.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, Organisation> organisationMap = organisationList.stream().collect(Collectors.toMap(Organisation::getId, Function.identity()));

        return resultList.stream().map(subscription -> {
            SubscriptionListDto dto = mapper.asListDto(subscription);
            Optional.ofNullable(subscription.getOutput()).ifPresent(output -> dto.setMessageType(output.getMessageType().name()));
            Organisation orgDomain = Optional.ofNullable(subscription.getOutput()).map(SubscriptionOutput::getSubscriber).map(SubscriptionSubscriber::getOrganisationId).map(organisationMap::get).orElse(null);
            if (orgDomain != null) {
                String fullOrgName = StringUtils.isNotEmpty(orgDomain.getParentOrganisation()) ? orgDomain.getParentOrganisation() + " / " + orgDomain.getName() : orgDomain.getName();
                dto.setOrganisationName(fullOrgName);
                Optional<EndPoint> endpoint = orgDomain.getEndPoints().stream()
                        .filter(ep -> Optional.ofNullable(subscription.getOutput()).map(SubscriptionOutput::getSubscriber).map(SubscriptionSubscriber::getEndpointId).map(id -> id == ep.getId()).orElse(false))
                        .findFirst();
                dto.setEndpointName(endpoint.map(EndPoint::getName).orElse(UNKNOWN));
                dto.setChannelName(endpoint
                        .map(EndPoint::getChannels)
                        .flatMap(channelForSubscription(subscription))
                        .map(Channel::getDataFlow)
                        .orElse(UNKNOWN)
                );
            }
            else {
                dto.setOrganisationName(UNKNOWN);
                dto.setEndpointName(UNKNOWN);
                dto.setChannelName(UNKNOWN);
            }
            return dto;
        }).collect(Collectors.toList());
    }

    private static Function<List<Channel>,Optional<Channel>> channelForSubscription(SubscriptionEntity subscription) {
        Optional<Long> subscriptionChannelId = Optional.ofNullable(subscription.getOutput()).map(SubscriptionOutput::getSubscriber).map(SubscriptionSubscriber::getChannelId);
        return channels -> channels.stream()
                .filter(channel -> subscriptionChannelId.map(channelId -> channelId == channel.getId()).orElse(false))
                .findFirst();
    }
}
