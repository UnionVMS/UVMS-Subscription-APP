/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */

package eu.europe.ec.fisheries.uvms.subscription.mapper;

import java.util.Map;

import eu.europa.ec.fisheries.uvms.subscription.service.mapper.CustomMapper;
import eu.europa.ec.fisheries.wsdl.subscription.module.CriteriaType;
import eu.europa.ec.fisheries.wsdl.subscription.module.MessageType;
import eu.europa.ec.fisheries.wsdl.subscription.module.SubCriteriaType;
import eu.europa.ec.fisheries.wsdl.subscription.module.SubscriptionDataCriteria;
import eu.europa.ec.fisheries.wsdl.subscription.module.SubscriptionDataQuery;
import org.junit.Test;

public class CustomMapperTest {

    @Test //TODO
    public void testMapCriteriaToQueryParameters(){

        SubscriptionDataQuery dataQuery = new SubscriptionDataQuery();

        dataQuery.setMessageType(MessageType.FLUX_FA_QUERY_MESSAGE);

        SubscriptionDataCriteria criteria = new SubscriptionDataCriteria();
        criteria.setCriteria(CriteriaType.SENDER);
        criteria.setSubCriteria(SubCriteriaType.ORGANISATION);
        criteria.setValue("BEL");

        SubscriptionDataCriteria criteria2 = new SubscriptionDataCriteria();
        criteria2.setCriteria(CriteriaType.VALIDITY_PERIOD);
        criteria2.setSubCriteria(SubCriteriaType.START_DATE);
        criteria2.setValue("222222");

        dataQuery.getCriteria().add(criteria);
        dataQuery.getCriteria().add(criteria2);

        Map<String, Object> stringObjectMap = CustomMapper.mapCriteriaToQueryParameters(dataQuery);

        System.out.println(stringObjectMap);
    }
}
