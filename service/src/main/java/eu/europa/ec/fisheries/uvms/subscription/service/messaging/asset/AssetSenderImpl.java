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
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import eu.europa.ec.fisheries.wsdl.asset.module.AssetGroupsForAssetRequest;
import eu.europa.ec.fisheries.wsdl.asset.module.AssetIdsForGroupRequest;
import eu.europa.ec.fisheries.wsdl.asset.module.AssetModuleMethod;
import eu.europa.ec.fisheries.wsdl.asset.module.FindAssetHistGuidByAssetGuidAndOccurrenceDateRequest;
import eu.europa.ec.fisheries.wsdl.asset.module.FindAssetHistGuidByAssetGuidAndOccurrenceDateResponse;
import eu.europa.ec.fisheries.wsdl.asset.module.FindVesselIdsByAssetHistGuidRequest;
import eu.europa.ec.fisheries.wsdl.asset.module.FindVesselIdsByAssetHistGuidResponse;
import eu.europa.ec.fisheries.wsdl.asset.module.FindVesselIdsByMultipleAssetHistGuidsRequest;
import eu.europa.ec.fisheries.wsdl.asset.module.FindVesselIdsByMultipleAssetHistGuidsResponse;
import eu.europa.ec.fisheries.wsdl.asset.types.AssetGroupsForAssetQueryElement;
import eu.europa.ec.fisheries.wsdl.asset.types.AssetGroupsForAssetResponseElement;
import eu.europa.ec.fisheries.wsdl.asset.types.AssetHistGuidIdWithVesselIdentifiers;
import eu.europa.ec.fisheries.wsdl.asset.types.AssetIdsForGroupGuidQueryElement;
import eu.europa.ec.fisheries.wsdl.asset.types.AssetIdsForGroupGuidResponseElement;
import eu.europa.ec.fisheries.wsdl.asset.types.AssetListPagination;
import eu.europa.ec.fisheries.wsdl.asset.types.VesselIdentifiersHolder;
import eu.europa.ec.fisheries.wsdl.asset.types.VesselIdentifiersWithConnectIdHolder;

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

    @Override
    public String findAssetHistoryGuid(String assetGuid, Date occurrenceDate) {
        FindAssetHistGuidByAssetGuidAndOccurrenceDateRequest request = new FindAssetHistGuidByAssetGuidAndOccurrenceDateRequest();
        request.setMethod(AssetModuleMethod.FIND_ASSET_HIST_GUID_BY_ASSET_GUID_AND_OCCURRENCE_DATE);
        request.setAssetGuid(assetGuid);
        request.setOccurrenceDate(occurrenceDate);
        FindAssetHistGuidByAssetGuidAndOccurrenceDateResponse response = assetClient.findAssetHistGuidByAssetGuidAndOccurrenceDate(request);
        return response.getAssetHistGuid();
    }


    @Override
    public List<String> findAssetGroupsForAsset(String assetGuid, Date occurrenceDate) {
        AssetGroupsForAssetRequest request = new AssetGroupsForAssetRequest();
        request.setMethod(AssetModuleMethod.ASSET_GROUPS_FOR_ASSET);
        AssetGroupsForAssetQueryElement query = new AssetGroupsForAssetQueryElement();
        String refUuid = UUID.randomUUID().toString();
        query.setRefUuid(refUuid);
        query.setConnectId(assetGuid);
        query.setOccurrenceDate(occurrenceDate);
        request.getAssetGroupsForAssetQueryElement().add(query);
        List<AssetGroupsForAssetResponseElement> response = assetClient.findAssetGroupsForAsset(request).getAssetGroupsForAssetResponseElementList();
        AssetGroupsForAssetResponseElement assetGroupsForAssetResponseElement = response.stream().filter(a -> refUuid.equals(a.getRefUuid())).findAny().orElse(null);
        return assetGroupsForAssetResponseElement != null ? assetGroupsForAssetResponseElement.getGroupUuid() : Collections.emptyList();
    }

    @Override
    public List<VesselIdentifiersWithConnectIdHolder> findAssetIdentifiersByAssetGroupGuid(String assetGroupGuid, Date occurrenceDate, Long pageNumber, Long pageSize) {
        AssetIdsForGroupGuidQueryElement queryElement = new AssetIdsForGroupGuidQueryElement();
        AssetListPagination assetListPagination = new AssetListPagination();
        assetListPagination.setPage(pageNumber.intValue());
        assetListPagination.setListSize(pageSize.intValue());
        queryElement.setPagination(assetListPagination);
        queryElement.setAssetGuid(assetGroupGuid);
        queryElement.setOccurrenceDate(occurrenceDate);
        AssetIdsForGroupRequest assetIdsForGroupRequest = new AssetIdsForGroupRequest();
        assetIdsForGroupRequest.setMethod(AssetModuleMethod.ASSET_IDS_FOR_GROUP_GUID);
        assetIdsForGroupRequest.setAssetIdsForGroupGuidQueryElement(queryElement);
        AssetIdsForGroupGuidResponseElement assetIdsForGroupGuidResponseElement = assetClient.findAssetIdentifiersForGroupGuid(assetIdsForGroupRequest);
        return assetIdsForGroupGuidResponseElement != null ? assetIdsForGroupGuidResponseElement.getVesselIdentifiers() : Collections.emptyList();
    }
}
