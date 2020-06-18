/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2020.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.service.messaging.asset;

import java.util.Date;
import java.util.List;

import eu.europa.ec.fisheries.wsdl.asset.types.AssetHistGuidIdWithVesselIdentifiers;
import eu.europa.ec.fisheries.wsdl.asset.types.VesselIdentifiersHolder;
import eu.europa.ec.fisheries.wsdl.asset.types.VesselIdentifiersWithConnectIdHolder;


/**
 * Client to interesting Asset services.
 */
public interface AssetSender {
    /**
     * Retrieve asset identifiers from asset module.
     *
     * @param assetHistGuid asset history guid
     * @return object containing vessel identifiers of asset
     */
    VesselIdentifiersHolder findVesselIdentifiers(String assetHistGuid);

    /**
     * Retrieve asset identifiers for multiple assets from asset module.
     *
     * @param assetHistGuids asset history guids
     * @return objects containing vessel identifiers of assets
     */
    List<AssetHistGuidIdWithVesselIdentifiers> findMultipleVesselIdentifiers(List<String> assetHistGuids);

    /**
     * Find the asset history UUID ({@code connectId}) of the given asset at the given date.
     *
     * @param assetGuid      The asset UUID
     * @param occurrenceDate The date
     * @return The asset history UUID
     */
    String findAssetHistoryGuid(String assetGuid, Date occurrenceDate);

    /**
     * Find the asset groups that the given asset belongs to.
     *
     * @param assetGuid      The asset UUID
     * @param occurrenceDate The date
     * @return Guids of found asset groups
     */
    List<String> findAssetGroupsForAsset(String assetGuid, Date occurrenceDate);

    /**
     * Find asset identifiers for assets included in a Group
     *
     * @param assetGroupGuid The Group guid
     * @param occurrenceDate The date
     * @param pageNumber     The page
     * @param pageSize       The page size
     * @return A list with the vessel identifiers of the assets
     */
    List<VesselIdentifiersWithConnectIdHolder> findAssetIdentifiersByAssetGroupGuid(String assetGroupGuid, Date occurrenceDate, Long pageNumber, Long pageSize);
}
