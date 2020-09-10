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
import eu.europa.ec.fisheries.uvms.spatial.model.schemas.AreaExtendedIdentifierType;
import eu.europa.ec.fisheries.wsdl.subscription.module.SubscriptionAreaSimpleType;

import java.util.List;
import java.util.stream.Collectors;

public class MovementModelMapper {
    public static List<SubscriptionAreaSimpleType> movementTypesToSubscriptionAreaSimpleTypes(List<MovementType> movementTypes){
        return movementTypes.stream().map(MovementModelMapper::movementTypeToSubscriptionAreaSimpleType).collect(Collectors.toList());
    }
    public static SubscriptionAreaSimpleType movementTypeToSubscriptionAreaSimpleType(MovementType movementType){
        if(movementType == null){
            return null;
        }
        SubscriptionAreaSimpleType subscriptionAreaSimpleType = new SubscriptionAreaSimpleType();
        subscriptionAreaSimpleType.setCrs(4326);
        subscriptionAreaSimpleType.setGuid(movementType.getGuid());
        subscriptionAreaSimpleType.setLatitude(movementType.getPosition().getLatitude());
        subscriptionAreaSimpleType.setLongitude(movementType.getPosition().getLongitude());
        subscriptionAreaSimpleType.setUserAreaActiveDate(XMLDateUtils.dateToXmlGregorian(movementType.getPositionTime()));
        return subscriptionAreaSimpleType;
    }

    public static List<MovementMetaDataAreaType> mapSpatialAreaToMovementAreas(List<AreaExtendedIdentifierType> spatialAreas){
        return spatialAreas.stream().map(MovementModelMapper::mapSpatialAreaToMoveArea).collect(Collectors.toList());
    }

    public static MovementMetaDataAreaType mapSpatialAreaToMoveArea(AreaExtendedIdentifierType spatialArea) {
        if(spatialArea == null) {
            return null;
        }
        MovementMetaDataAreaType movementMetaDataAreaType = new MovementMetaDataAreaType();
        movementMetaDataAreaType.setAreaType(spatialArea.getAreaType().name());
        movementMetaDataAreaType.setCode(spatialArea.getCode());
        movementMetaDataAreaType.setRemoteId(spatialArea.getId());
        movementMetaDataAreaType.setName(spatialArea.getName());
        movementMetaDataAreaType.setTransitionType(MovementTypeType.POS);
        return movementMetaDataAreaType;
    }
}
