/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2020.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.service.messaging.asset;

import eu.europa.ec.fisheries.wsdl.asset.module.FindAssetHistGuidByAssetGuidAndOccurrenceDateRequest;
import eu.europa.ec.fisheries.wsdl.asset.module.FindAssetHistGuidByAssetGuidAndOccurrenceDateResponse;
import eu.europa.ec.fisheries.wsdl.asset.module.FindVesselIdsByAssetHistGuidRequest;
import eu.europa.ec.fisheries.wsdl.asset.module.FindVesselIdsByAssetHistGuidResponse;
import eu.europa.ec.fisheries.wsdl.asset.module.FindVesselIdsByMultipleAssetHistGuidsRequest;
import eu.europa.ec.fisheries.wsdl.asset.module.FindVesselIdsByMultipleAssetHistGuidsResponse;

/**
 * Low-level client to interesting Asset module services.
 */
public interface AssetClient {
	/**
	 * Call {@code FIND_VESSEL_IDS_BY_ASSET_HIST_GUID}.
	 *
	 * @param request The asset module request
	 * @return The asset module response
	 */
	FindVesselIdsByAssetHistGuidResponse findVesselIdsByAssetHistGuid(FindVesselIdsByAssetHistGuidRequest request);

	/**
	 * Call {@code FIND_VESSEL_IDS_BY_MULTIPLE_ASSET_HIST_GUID}.
	 *
	 * @param request The asset module request
	 * @return The asset module response
	 */
	FindVesselIdsByMultipleAssetHistGuidsResponse findVesselIdsByMultipleAssetHistGuid(FindVesselIdsByMultipleAssetHistGuidsRequest request);

	/**
	 * Call {@code FIND_ASSET_HIST_GUID_BY_ASSET_GUID_AND_OCCURRENCE_DATE}.
	 *
	 * @param request The asset module request
	 * @return The asset module response
	 */
	FindAssetHistGuidByAssetGuidAndOccurrenceDateResponse findAssetHistGuidByAssetGuidAndOccurrenceDate(FindAssetHistGuidByAssetGuidAndOccurrenceDateRequest request);
}
