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

import eu.europa.ec.fisheries.wsdl.subscription.module.CriteriaType;
import eu.europa.ec.fisheries.wsdl.subscription.module.MessageType;
import eu.europa.ec.fisheries.wsdl.subscription.module.SubscriptionDataCriteria;
import eu.europa.ec.fisheries.wsdl.subscription.module.SubscriptionDataQuery;
import eu.europa.ec.fisheries.wsdl.subscription.module.ValueType;

/**
 * TODO create test
 */
public class CustomMapper {

    public Map<String, Object> mapCriteriaToQueryParameters(SubscriptionDataQuery query){

        Map<String, Object> queryParameters = new HashMap<>();

        MessageType messageType = query.getMessageType();

        createMessageTypeParameters(messageType, queryParameters);

        List<SubscriptionDataCriteria> criteria = query.getCriteria();

        for (SubscriptionDataCriteria criterion : criteria){

            CriteriaType criteriaType = criterion.getCriteria();
            switch (criteriaType){
                case SENDER:
                    createSenderParameters(criterion, queryParameters);
                    break;
                case VESSEL:
                    createVesselParameters(criterion, queryParameters);
                    break;
                case VALIDITY_PERIOD:
                    createValidityPeriodParameters(criterion, queryParameters);
                    break;
                case AREA:
                    createAreaPeriodParameters(criterion, queryParameters);
                    break;
                    default:
            }
        }
        return queryParameters;
    }

    private void createMessageTypeParameters(MessageType messageType, Map<String, Object> queryParameters) {
        queryParameters.put("MESSAGE_TYPE", messageType.value());
    }

    private void createAreaPeriodParameters(SubscriptionDataCriteria criteria, Map<String, Object> queryParameters) {

    }

    private void createValidityPeriodParameters(SubscriptionDataCriteria criteria, Map<String, Object> queryParameters) {

    }

    private void createVesselParameters(SubscriptionDataCriteria criteria, Map<String, Object> queryParameters) {

    }

    private void createSenderParameters(SubscriptionDataCriteria criteria, Map<String, Object> queryParamers) {

        CriteriaType criteriaType = criteria.getCriteria();
        ValueType valueType = criteria.getValueType();
        String value = criteria.getValue();
        queryParamers.put("ORGANISATION", value);
    }

}
