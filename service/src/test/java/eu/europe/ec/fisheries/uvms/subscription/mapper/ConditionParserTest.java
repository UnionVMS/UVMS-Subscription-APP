/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */

package eu.europe.ec.fisheries.uvms.subscription.mapper;

import eu.europa.ec.fisheries.uvms.subscription.service.domain.CompositeType;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.ConditionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.ConditionType;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.RelationalOperatorType;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.mapper.SubscriptionParser;
import eu.europa.ec.fisheries.wsdl.subscription.module.SubCriteriaType;
import eu.europa.ec.fisheries.wsdl.subscription.module.ValueType;
import org.junit.Test;

public class ConditionParserTest {

    @Test
    public void testParseCondition(){

        SubscriptionEntity subscription = SubscriptionEntity.random();
        ConditionEntity conditionEntity = new ConditionEntity();
        conditionEntity.setStartOperator("(");
        //conditionEntity.setCriteriaType(CriteriaType.FLUX_REPORT_DOCUMENT);
        conditionEntity.setSubCriteriaType(SubCriteriaType.OWNER_PARTY);
        conditionEntity.setCondition(RelationalOperatorType.EQ);
        conditionEntity.setValueType(ValueType.FLUX_GP_PARTY);
        conditionEntity.setValue("BEL");
        conditionEntity.setEndOperator(")");
        conditionEntity.setCompositeType(CompositeType.OR);
        subscription.addCondition(conditionEntity);
        conditionEntity = new ConditionEntity();
        conditionEntity.setStartOperator("(");
        //conditionEntity.setCriteriaType(CriteriaType.FLUX_REPORT_DOCUMENT);
        conditionEntity.setSubCriteriaType(SubCriteriaType.OWNER_PARTY);
        conditionEntity.setCondition(RelationalOperatorType.NE);
        conditionEntity.setValueType(ValueType.FLUX_GP_PARTY);
        conditionEntity.setValue("FRA");
        conditionEntity.setEndOperator(")");
        conditionEntity.setCompositeType(CompositeType.NONE);
        subscription.addCondition(conditionEntity);

        System.out.println(SubscriptionParser.parseCondition(ConditionType.END, subscription));

    }
}
