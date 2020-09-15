/*
 *
 *  Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2020.
 *
 *  This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 *  and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 *  the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 *  details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package eu.europa.ec.fisheries.uvms.subscription.movement.mapper;

import eu.europa.ec.fisheries.schema.movement.v1.MovementMetaDataAreaType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementTypeType;
import eu.europa.ec.fisheries.uvms.commons.date.XMLDateUtils;
import eu.europa.ec.fisheries.wsdl.subscription.module.SubscriptionAreaType;
import eu.europa.ec.fisheries.wsdl.subscription.module.SubscriptionMovementMetaDataAreaType;

import java.util.List;
import java.util.stream.Collectors;

public class MovementModelMapper {
    public static List<SubscriptionAreaType> movementTypesToSubscriptionAreaTypes(List<MovementType> movementTypes){
        return movementTypes.stream().map(MovementModelMapper::movementTypeToSubscriptionAreaType).collect(Collectors.toList());
    }
    public static SubscriptionAreaType movementTypeToSubscriptionAreaType(MovementType movementType){
        if(movementType == null){
            return null;
        }
        SubscriptionAreaType subscriptionAreaType = new SubscriptionAreaType();
        subscriptionAreaType.setCrs(4326);
        subscriptionAreaType.setCorrelationId(movementType.getGuid());
        subscriptionAreaType.setLatitude(movementType.getPosition().getLatitude());
        subscriptionAreaType.setLongitude(movementType.getPosition().getLongitude());
        subscriptionAreaType.setUserAreaActiveDate(XMLDateUtils.dateToXmlGregorian(movementType.getPositionTime()));
        return subscriptionAreaType;
    }

    public static List<MovementMetaDataAreaType> mapSubscriptionAreasToMovementAreas(List<SubscriptionMovementMetaDataAreaType> subscriptionMovementMetaDataAreaTypes){
        return subscriptionMovementMetaDataAreaTypes.stream().map(MovementModelMapper::mapSubscriptionAreaToMovementArea).collect(Collectors.toList());
    }

    public static MovementMetaDataAreaType mapSubscriptionAreaToMovementArea(SubscriptionMovementMetaDataAreaType subscriptionMovementMetaDataAreaTypes) {
        if(subscriptionMovementMetaDataAreaTypes == null) {
            return null;
        }
        MovementMetaDataAreaType movementMetaDataAreaType = new MovementMetaDataAreaType();
        movementMetaDataAreaType.setAreaType(subscriptionMovementMetaDataAreaTypes.getAreaType());
        movementMetaDataAreaType.setCode(subscriptionMovementMetaDataAreaTypes.getCode());
        movementMetaDataAreaType.setRemoteId(subscriptionMovementMetaDataAreaTypes.getRemoteId());
        movementMetaDataAreaType.setName(subscriptionMovementMetaDataAreaTypes.getName());
        movementMetaDataAreaType.setTransitionType(MovementTypeType.POS);
        return movementMetaDataAreaType;
    }
}
