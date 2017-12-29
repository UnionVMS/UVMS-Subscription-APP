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
import eu.europa.ec.fisheries.wsdl.subscription.module.SubCriteriaType;
import eu.europa.ec.fisheries.wsdl.subscription.module.SubscriptionDataCriteria;
import eu.europa.ec.fisheries.wsdl.subscription.module.SubscriptionDataQuery;

public class CustomMapper {

    private CustomMapper(){

    }

    public static Map<String, Object> mapCriteriaToQueryParameters(SubscriptionDataQuery query){

        Map<String, Object> queryParameters = new HashMap<>();

        MessageType messageType = query.getMessageType();

        queryParameters.put("MESSAGE_TYPE", messageType.value());

        List<SubscriptionDataCriteria> criteria = query.getCriteria();

        for (SubscriptionDataCriteria criterion : criteria){

            CriteriaType criteriaType = criterion.getCriteria();
            String value = criterion.getValue();
            switch (criteriaType){
                case SENDER:

                    queryParameters.put("ORGANISATION", value);
                    break;

                case VESSEL:

                    break;

                case VALIDITY_PERIOD:
                    if (SubCriteriaType.START_DATE_TIME.equals(criterion.getSubCriteria())){
                        queryParameters.put("START_DATE", value);
                    }
                    else if (SubCriteriaType.END_DATE_TIME.equals(criterion.getSubCriteria())){
                        queryParameters.put("END_DATE", value);
                    }
                    break;

                case AREA:

                    break;
                    default:
            }
        }
        return queryParameters;
    }

}
