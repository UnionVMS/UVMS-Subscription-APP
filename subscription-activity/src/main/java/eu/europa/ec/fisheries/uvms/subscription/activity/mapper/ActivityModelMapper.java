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
import eu.europa.ec.fisheries.wsdl.subscription.module.SubscriptionMovementMetaDataAreaType;

import java.util.List;
import java.util.stream.Collectors;

public class ActivityModelMapper {

    public static List<Area> mapSubscriptionAreasToActivityAreas(List<SubscriptionMovementMetaDataAreaType> subscriptionMovementMetaDataAreaTypes){
        return subscriptionMovementMetaDataAreaTypes.stream().map(ActivityModelMapper::mapSubscriptionAreaToActivityArea).collect(Collectors.toList());
    }

    public static Area mapSubscriptionAreaToActivityArea(SubscriptionMovementMetaDataAreaType subscriptionMovementMetaDataAreaType) {
        if(subscriptionMovementMetaDataAreaType == null) {
            return null;
        }
        Area area = new Area();
        area.setAreaType(subscriptionMovementMetaDataAreaType.getAreaType());
        area.setRemoteId(subscriptionMovementMetaDataAreaType.getRemoteId());
        area.setName(subscriptionMovementMetaDataAreaType.getName());
        return area;
    }
}
