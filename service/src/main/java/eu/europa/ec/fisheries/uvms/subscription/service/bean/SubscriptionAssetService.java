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

package eu.europa.ec.fisheries.uvms.subscription.service.bean;

import eu.europa.ec.fisheries.uvms.subscription.service.domain.AreaEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.AssetEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionOutput;

import java.util.Collection;
import java.util.List;

public interface SubscriptionAssetService {

    /**
     * Returns an AssetEntity list which retrieved from Asset module by asset guid list. This will will be previously 
     * fetched from movement module, for movements that satisfy location criteria received initially from spatial for 
     * given AreaEntity collection.
     * @param areasGeometry The AreaEntity collection
     * @param output SubscriptionOutput
     * @param page Starting page
     * @param limit Records limit
     * @return List of AssetEntities
     */
    List<AssetEntity> getAssetEntitiesPaginated(String areasGeometry, SubscriptionOutput output, Integer page, Integer limit);
}
