/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.movement.communication;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import eu.europa.ec.fisheries.schema.movement.area.v1.AreaType;
import eu.europa.ec.fisheries.schema.movement.area.v1.GuidListForAreaFilteringQuery;
import eu.europa.ec.fisheries.schema.movement.asset.v1.VesselIdentifyingProperties;
import eu.europa.ec.fisheries.schema.movement.module.v1.FilterGuidListByAreaAndDateRequest;
import eu.europa.ec.fisheries.schema.movement.module.v1.FilterGuidListByAreaAndDateResponse;
import eu.europa.ec.fisheries.schema.movement.module.v1.ForwardPositionRequest;
import eu.europa.ec.fisheries.schema.movement.module.v1.ForwardPositionResponse;
import eu.europa.ec.fisheries.schema.movement.module.v1.MovementModuleMethod;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.AreaEntity;
import eu.europa.fisheries.uvms.subscription.model.enums.SubscriptionVesselIdentifier;

/**
 * Implementation of {@link MovementSender}.
 */
@ApplicationScoped
class MovementSenderImpl implements MovementSender {

    private MovementClient movementClient;

    /**
     * Injection constructor
     *
     * @param movementClient The low-level client to the services of the activity module
     */
    @Inject
    public MovementSenderImpl(MovementClient movementClient) {
        this.movementClient = movementClient;
    }

    /**
     * Constructor for frameworks.
     */
    @SuppressWarnings("unused")
    MovementSenderImpl() {
        // NOOP
    }

    @Override
    public List<String> sendFilterGuidListForAreasRequest(List<String> guidList, Date startDate, Date endDate, Collection<AreaEntity> areas) {

        FilterGuidListByAreaAndDateRequest request = new FilterGuidListByAreaAndDateRequest();
        request.setMethod(MovementModuleMethod.FILTER_GUID_LIST_FOR_DATE_BY_AREA);
        GuidListForAreaFilteringQuery value = new GuidListForAreaFilteringQuery();
        value.getGuidList().addAll(guidList);
        value.setStartDate(startDate);
        value.setEndDate(endDate);
        value.getAreas().addAll(areas.stream().map(MovementSenderImpl::toType).collect(Collectors.toList()));
        request.setQuery(value);
        FilterGuidListByAreaAndDateResponse response = movementClient.filterGuidListForDateByArea(request);
        return response != null ? response.getFilteredList() : new ArrayList<>();
    }

    @Override
    public String forwardPosition(Map<String, String> vesselIdentifiers, String vesselFlagState, List<String> movementGuidList, String receiver, String dataflow) {
        ForwardPositionRequest request = new ForwardPositionRequest();
        request.setMethod(MovementModuleMethod.FORWARD_POSITION);
        request.setVesselIdentifyingProperties(populateVesselIdentifyingProperties(vesselIdentifiers, vesselFlagState));
        request.setDataflow(dataflow);
        request.setReceiver(receiver);
        movementGuidList.forEach(movementGuid -> request.getMovementGuids().add(movementGuid));

        ForwardPositionResponse response = movementClient.forwardPosition(request);

        return response != null ? response.getMessageId() : "";
    }

    private VesselIdentifyingProperties populateVesselIdentifyingProperties(Map<String, String> vesselIdentifiers, String vesselFlagState) {
        VesselIdentifyingProperties vesselIdentifyingProperties = new VesselIdentifyingProperties();
        vesselIdentifyingProperties.setCfr(vesselIdentifiers.get(SubscriptionVesselIdentifier.CFR.name()));
        vesselIdentifyingProperties.setIrcs(vesselIdentifiers.get(SubscriptionVesselIdentifier.IRCS.name()));
        vesselIdentifyingProperties.setIccat(vesselIdentifiers.get(SubscriptionVesselIdentifier.ICCAT.name()));
        vesselIdentifyingProperties.setUvi(vesselIdentifiers.get(SubscriptionVesselIdentifier.UVI.name()));
        vesselIdentifyingProperties.setExtMarking(vesselIdentifiers.get(SubscriptionVesselIdentifier.EXT_MARK.name()));
        vesselIdentifyingProperties.setFlagState(vesselFlagState);
        return vesselIdentifyingProperties;
    }

    private static AreaType toType(AreaEntity areaEntity) {
        AreaType areaType = new AreaType();
        areaType.setAreaId(areaEntity.getGid());
        areaType.setAreaName(areaEntity.getAreaType().name());
        return areaType;
    }
}
