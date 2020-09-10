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

package eu.europa.ec.fisheries.uvms.subscription.activity.mapper;

import eu.europa.ec.fisheries.uvms.activity.model.schemas.Area;
import eu.europa.ec.fisheries.uvms.activity.model.schemas.MovementSimpleType;
import eu.europa.ec.fisheries.uvms.spatial.model.schemas.AreaExtendedIdentifierType;
import eu.europa.ec.fisheries.wsdl.subscription.module.SubscriptionAreaSimpleType;

import java.util.List;
import java.util.stream.Collectors;

public class ActivityModelMapper {

    public static List<SubscriptionAreaSimpleType> movementSimpleTypesToSubscriptionAreaSimpleTypes(List<MovementSimpleType> movementSimpleTypes){
        return movementSimpleTypes.stream().map(ActivityModelMapper::movementSimpleTypeToSubscriptionAreaSimpleTypes).collect(Collectors.toList());
    }
    public static SubscriptionAreaSimpleType movementSimpleTypeToSubscriptionAreaSimpleTypes(MovementSimpleType movementSimpleType){
        if(movementSimpleType == null){
            return null;
        }
        SubscriptionAreaSimpleType subscriptionAreaSimpleType = new SubscriptionAreaSimpleType();
        subscriptionAreaSimpleType.setCrs(4326);
        subscriptionAreaSimpleType.setGuid(movementSimpleType.getMoveGuid());
        subscriptionAreaSimpleType.setLatitude(movementSimpleType.getLatitude());
        subscriptionAreaSimpleType.setLongitude(movementSimpleType.getLongitude());
        return subscriptionAreaSimpleType;
    }
    public static List<Area> mapSpatialAreaToActivityAreas(List<AreaExtendedIdentifierType> spatialAreas){
        return spatialAreas.stream().map(ActivityModelMapper::mapSpatialAreaToActivityArea).collect(Collectors.toList());
    }

    public static Area mapSpatialAreaToActivityArea(AreaExtendedIdentifierType spatialArea) {
        if(spatialArea == null) {
            return null;
        }
        Area area = new Area();
        area.setAreaType(spatialArea.getAreaType().name());
        area.setRemoteId(spatialArea.getId());
        area.setName(spatialArea.getName());
        return area;
    }
}
