/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */

package eu.europe.ec.fisheries.uvms.subscription.helper;

import eu.europa.ec.fisheries.wsdl.subscription.module.CriteriaType;
import eu.europa.ec.fisheries.wsdl.subscription.module.MessageType;
import eu.europa.ec.fisheries.wsdl.subscription.module.SubCriteriaType;
import eu.europa.ec.fisheries.wsdl.subscription.module.SubscriptionDataCriteria;
import eu.europa.ec.fisheries.wsdl.subscription.module.SubscriptionDataQuery;
import eu.europa.ec.fisheries.wsdl.subscription.module.ValueType;


public class SubscriptionTestHelper {

    private SubscriptionTestHelper(){

    }

    public static SubscriptionDataQuery getSubscriptionDataQueryFaQuery_1(){
        SubscriptionDataQuery dataQuery = new SubscriptionDataQuery();

        dataQuery.setMessageType(MessageType.FLUX_FA_QUERY_MESSAGE);

        SubscriptionDataCriteria organisation = new SubscriptionDataCriteria();
        organisation.setCriteria(CriteriaType.SENDER);
        organisation.setSubCriteria(SubCriteriaType.ORGANISATION);
        organisation.setValue("1");

        SubscriptionDataCriteria startDate = new SubscriptionDataCriteria();
        startDate.setCriteria(CriteriaType.VALIDITY_PERIOD);
        startDate.setSubCriteria(SubCriteriaType.START_DATE);
        startDate.setValueType(ValueType.YYYY_MM_DD_T_HH_MM_SS_SSSZ);
        startDate.setValue("2017-07-01T02:00:00.000+02:00");

        dataQuery.getCriteria().add(organisation);
        //dataQuery.getCriteria().add(startDate);FIXME

        return dataQuery;
    }

    public static SubscriptionDataQuery getSubscriptionDataQueryFaQuery_2(){
        SubscriptionDataQuery dataQuery = new SubscriptionDataQuery();

        dataQuery.setMessageType(MessageType.FLUX_FA_QUERY_MESSAGE);

        SubscriptionDataCriteria organisation = new SubscriptionDataCriteria();
        organisation.setCriteria(CriteriaType.SENDER);
        organisation.setSubCriteria(SubCriteriaType.ORGANISATION);
        organisation.setValue("3");

        SubscriptionDataCriteria startDate = new SubscriptionDataCriteria();
        startDate.setCriteria(CriteriaType.VALIDITY_PERIOD);
        startDate.setSubCriteria(SubCriteriaType.START_DATE);
        startDate.setValueType(ValueType.YYYY_MM_DD_T_HH_MM_SS_SSSZ);
        startDate.setValue("2017-07-01T02:00:00.000+02:00");

        SubscriptionDataCriteria endDate = new SubscriptionDataCriteria();
        endDate.setCriteria(CriteriaType.VALIDITY_PERIOD);
        endDate.setValueType(ValueType.YYYY_MM_DD_T_HH_MM_SS_SSSZ);
        endDate.setSubCriteria(SubCriteriaType.END_DATE);
        endDate.setValue("2018-07-01T02:00:00.000+02:00");

        dataQuery.getCriteria().add(organisation);
        //dataQuery.getCriteria().add(startDate); FIXME
        //dataQuery.getCriteria().add(endDate);FIXME

        return dataQuery;
    }

    public static SubscriptionDataQuery getSubscriptionDataQueryFaQuery_3(){
        SubscriptionDataQuery dataQuery = new SubscriptionDataQuery();

        dataQuery.setMessageType(MessageType.FLUX_FA_QUERY_MESSAGE);

        SubscriptionDataCriteria organisation = new SubscriptionDataCriteria();
        organisation.setCriteria(CriteriaType.SENDER);
        organisation.setSubCriteria(SubCriteriaType.ORGANISATION);
        organisation.setValue("4");

        SubscriptionDataCriteria startDate = new SubscriptionDataCriteria();
        startDate.setCriteria(CriteriaType.VALIDITY_PERIOD);
        startDate.setSubCriteria(SubCriteriaType.START_DATE);
        startDate.setValueType(ValueType.YYYY_MM_DD_T_HH_MM_SS_SSSZ);
        startDate.setValue("2017-07-01T02:00:00.000+02:00");

        SubscriptionDataCriteria endDate = new SubscriptionDataCriteria();
        endDate.setCriteria(CriteriaType.VALIDITY_PERIOD);
        endDate.setValueType(ValueType.YYYY_MM_DD_T_HH_MM_SS_SSSZ);
        endDate.setSubCriteria(SubCriteriaType.END_DATE);
        endDate.setValue("2018-07-01T02:00:00.000+02:00");

        dataQuery.getCriteria().add(organisation);
        dataQuery.getCriteria().add(startDate);
        dataQuery.getCriteria().add(endDate);

        return dataQuery;
    }





}
