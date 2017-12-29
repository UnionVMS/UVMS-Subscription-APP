/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */

package eu.europa.ec.fisheries.uvms.subscription.service.mapper;

import java.util.Set;

import eu.europa.ec.fisheries.uvms.subscription.service.domain.ConditionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.ConditionType;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.RelationalOperatorType;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;

public class SubscriptionParser {

    private SubscriptionParser(){

    }

    public static String parseCondition(ConditionType type, SubscriptionEntity subscription) {

        Set<ConditionEntity> startCondition = subscription.getConditions();

        // TODO order by start end and order
        //CollectionUtils.filter(startCondition, new org.apache.commons.collections.Predicate() {
        //    @Override public boolean evaluate(Object o) {
        //        return ConditionType.START == ((ConditionEntity) o).getConditionType();
        //    }
        //});

        StringBuilder sb = new StringBuilder();

        for (ConditionEntity condition : startCondition) {

            if (condition.getStartOperator() != null) {
                sb.append(condition.getStartOperator());
            }

            if (condition.getSubCriteriaType() != null) {
                switch (condition.getSubCriteriaType()) {
                    case CHANNEL:
                        // If list and NE
                        if (condition.getCondition().equals(RelationalOperatorType.NE)) {
                            sb.append("!");
                        }
                        sb.append("channel");
                        break;

                    case ORGANISATION:
                        break;

                    // AREA
                    case END_POINT:
                        // If list and NE
                        if (condition.getCondition().equals(RelationalOperatorType.NE)) {
                            //sb.append("!");
                        }
                        sb.append("endPoint");
                        break;
                    case OWNER_PARTY:
                        // If list and NE
                        if (condition.getCondition().equals(RelationalOperatorType.NE)) {
                            //sb.append("!");
                        }
                        sb.append("ownerParty");
                        break;


                    default:
                        break;
                }
            }

            switch (condition.getCondition()) {
                case EQ:
                    // Different EQ if a list
                    //if (isListCriteria(condition.getSubCriteriaType())) {
                    //    sb.append(".contains(");
                    //} else {
                        sb.append(" == ");
                    //}
                    break;
                case NE:
                    // Different NE if a list
                    //if (isListCriteria(condition.getSubCriteriaType())) {
                    //    sb.append(".contains(");
                    //} else {
                        sb.append(" != ");
                    //}
                    break;
                case GT:
                    sb.append(" > ");
                    break;
                case GE:
                    sb.append(" >= ");
                    break;
                case LT:
                    sb.append(" < ");
                    break;
                case LE:
                    sb.append(" <= ");
                    break;
                default: // undefined
                    break;

            }

            sb.append("\"").append(condition.getValue()).append("\"");

            if (condition.getEndOperator() != null) {
                sb.append(condition.getEndOperator());
            }

            switch (condition.getCompositeType()) {
                case AND:
                    sb.append(" && ");
                    break;
                case OR:
                    sb.append(" || ");
                    break;
                case NONE:
                    break;
                default: // undefined
                    break;
            }
        }

        return sb.toString();
    }

}