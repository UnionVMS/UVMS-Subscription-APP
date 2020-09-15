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

package eu.europa.ec.fisheries.uvms.subscription.service.filter;

import eu.europa.ec.fisheries.uvms.subscription.service.domain.AssetEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionOutput;

import java.util.List;

public interface AreaFiltererComponent {

    /**
     * Filters AssetAndSubscriptionData by its assetEntity.guid for the given collection, by {@code subscription.areas},
     * also will calculate a period to request (start and date) from the {@code subscription.output}.
     *
     * @param assets To be filtered
     * @param subscriptionEntity The SubscriptionEntity
     * @param areasGeometry The areas Geometry
     * @return The filtered List<AssetEntity>
     */
    List<AssetEntity> filterAssetsBySubscriptionAreas(List<AssetEntity> assets, SubscriptionEntity subscriptionEntity,String areasGeometry);

    /**
     * Retrieves a list of guids that according to movement module there had been at least one movement that satisfies
     * the requested criteria
     * @param inList The list that will be filtered, can be empty
     * @param output The SubscriptionOutput, will be used to calculated start and end dates
     * @param areasGeometry Areas geometry string (wkt)
     * @param fromPage Page number
     * @param limit Records limit
     * @return The filtered guid list
     */
    List<String> getConnectIdsByDateRangeAndGeometry(List<String> inList, SubscriptionOutput output, String areasGeometry, Integer fromPage, Integer limit);
}
