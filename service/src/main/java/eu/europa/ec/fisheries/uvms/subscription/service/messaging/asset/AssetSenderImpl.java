/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2020.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.service.messaging.asset;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;

import eu.europa.ec.fisheries.wsdl.asset.module.AssetModuleMethod;
import eu.europa.ec.fisheries.wsdl.asset.module.FindVesselIdsByAssetHistGuidRequest;
import eu.europa.ec.fisheries.wsdl.asset.module.FindVesselIdsByAssetHistGuidResponse;
import eu.europa.ec.fisheries.wsdl.asset.module.FindVesselIdsByMultipleAssetHistGuidsRequest;
import eu.europa.ec.fisheries.wsdl.asset.module.FindVesselIdsByMultipleAssetHistGuidsResponse;
import eu.europa.ec.fisheries.wsdl.asset.types.AssetHistGuidIdWithVesselIdentifiers;
import eu.europa.ec.fisheries.wsdl.asset.types.VesselIdentifiersHolder;

/**
 * Implementation of {@link AssetSender}
 */
@ApplicationScoped
public class AssetSenderImpl implements AssetSender {

    private AssetClient assetClient;

    /**
     * Injection constructor.
     *
     * @param assetClient The asset client
     */
    @Inject
    public AssetSenderImpl(AssetClient assetClient) {
        this.assetClient = assetClient;
    }

    /**
     * Constructor for frameworks.
     */
    @SuppressWarnings("unused")
    AssetSenderImpl() {
        // NOOP
    }

    @Override
    public VesselIdentifiersHolder findVesselIdentifiers(String assetHistGuid) {
        FindVesselIdsByAssetHistGuidRequest request = new FindVesselIdsByAssetHistGuidRequest();
        request.setMethod(AssetModuleMethod.FIND_VESSEL_IDS_BY_ASSET_HIST_GUID);
        request.setAssetHistoryGuid(assetHistGuid);
        FindVesselIdsByAssetHistGuidResponse response = assetClient.findVesselIdsByAssetHistGuid(request);
        return response.getIdentifiers();
    }

    @Override
    public List<AssetHistGuidIdWithVesselIdentifiers> findMultipleVesselIdentifiers(List<String> assetHistGuids) {
        FindVesselIdsByMultipleAssetHistGuidsRequest request = new FindVesselIdsByMultipleAssetHistGuidsRequest();
        request.setMethod(AssetModuleMethod.FIND_VESSEL_IDS_BY_MULTIPLE_ASSET_HIST_GUID);
        request.getAssetHistoryGuids().addAll(assetHistGuids);
        FindVesselIdsByMultipleAssetHistGuidsResponse response = assetClient.findVesselIdsByMultipleAssetHistGuid(request);
        return response.getIdentifiers();
    }
}
