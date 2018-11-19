/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */

package eu.europa.ec.fisheries.uvms.subscription.service.mapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import eu.europa.ec.fisheries.uvms.commons.date.DateUtils;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.ec.fisheries.wsdl.subscription.module.*;
import eu.europa.ec.fisheries.wsdl.user.types.Channel;
import eu.europa.ec.fisheries.wsdl.user.types.EndPoint;
import eu.europa.ec.fisheries.wsdl.user.types.Organisation;
import org.apache.commons.lang.StringUtils;

public class CustomMapper {

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

        boolean isOrgAvailable;
        boolean isChannelAvailable;
        boolean isEndPointAvailable;

        if (organisationList == null || organisationList.isEmpty()) {
            return resultList;
        }
        for (SubscriptionEntity subscription : resultList){
            isOrgAvailable = false;
             for (Organisation orgDomain: organisationList){
                if (subscription.getOrganisation() == orgDomain.getId()) {
                    isOrgAvailable = true;
                    isEndPointAvailable = false;
                    if (orgDomain.getParentOrganisation() != null
                            && !StringUtils.isEmpty( orgDomain.getParentOrganisation() )) {

                        StringBuilder sb = new StringBuilder();
                        sb.append( orgDomain.getParentOrganisation() );
                        sb.append( " / " ).append( orgDomain.getName() );
                        subscription.setOrganisationName( sb.toString() );
                    }else
                        subscription.setOrganisationName( orgDomain.getName());

                    for (EndPoint endPoint : orgDomain.getEndPoints()) {
                        if (subscription.getEndPoint() == endPoint.getId()) {
                            isEndPointAvailable = true;
                            isChannelAvailable = false;
                            subscription.setEndpointName( endPoint.getName() );
                            for (Channel channel : endPoint.getChannels()) {
                                if (subscription.getChannel() == channel.getId()) {
                                    isChannelAvailable = true;
                                    subscription.setChannelName( channel.getDataFlow() );
                                }
                                if(isChannelAvailable)
                                    break;
                            }
                            if(!isChannelAvailable){
                                subscription.setChannelName( "UNKNOWN" );
                            }
                        }
                        if(isEndPointAvailable)
                            break;
                    }
                    if(!isEndPointAvailable){
                        subscription.setEndpointName( "UNKNOWN" );
                        subscription.setChannelName( "UNKNOWN" );
                    }
                }
                if (isOrgAvailable)
                    break;
            }
            if(!isOrgAvailable){
                 subscription.setOrganisationName( "UNKNOWN" );
                 subscription.setEndpointName( "UNKNOWN" );
                 subscription.setChannelName( "UNKNOWN" );
            }
        }

        return resultList;
    }
}
