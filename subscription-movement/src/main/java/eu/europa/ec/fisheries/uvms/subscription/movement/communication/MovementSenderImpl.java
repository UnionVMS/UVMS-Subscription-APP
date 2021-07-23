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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import eu.europa.ec.fisheries.schema.movement.area.v1.GuidListForAreaFilteringQuery;
import eu.europa.ec.fisheries.schema.movement.asset.v1.VesselIdentifyingProperties;
import eu.europa.ec.fisheries.schema.movement.module.v1.ForwardPositionRequest;
import eu.europa.ec.fisheries.schema.movement.module.v1.ForwardPositionResponse;
import eu.europa.ec.fisheries.schema.movement.module.v1.GetConnectIdsByDateAndGeometryRequest;
import eu.europa.ec.fisheries.schema.movement.module.v1.GetConnectIdsByDateAndGeometryResponse;
import eu.europa.ec.fisheries.schema.movement.module.v1.MovementModuleMethod;
import eu.europa.ec.fisheries.schema.movement.v1.CodeType;
import eu.europa.ec.fisheries.schema.movement.v1.DateTimeType;
import eu.europa.ec.fisheries.schema.movement.v1.MeasureType;
import eu.europa.ec.fisheries.schema.movement.v1.VesselPositionEventType;
import eu.europa.fisheries.uvms.subscription.model.enums.SubscriptionVesselIdentifier;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._20.VesselGeographicalCoordinate;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._20.VesselPositionEvent;

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
    public List<String> sendGetConnectIdsByDateAndGeometryRequest(List<String> inList, Date startDate, Date endDate, String areasGeometryUnion,
                                                                  Integer page,
                                                                  Integer limit) {
        GetConnectIdsByDateAndGeometryRequest request = new GetConnectIdsByDateAndGeometryRequest();
        request.setMethod(MovementModuleMethod.FIND_CONNECT_IDS_BY_DATE_AND_GEOMETRY);
        GuidListForAreaFilteringQuery query = new GuidListForAreaFilteringQuery();
        if(inList != null && !inList.isEmpty()){
            query.getGuidList().addAll(inList);
        }
        query.setStartDate(startDate);
        query.setEndDate(endDate);
        query.setAreasGeometryUnion(areasGeometryUnion);
        query.setLimit(limit);
        query.setPage(page);
        request.setQuery(query);
        GetConnectIdsByDateAndGeometryResponse response = movementClient.sendRequest(request,GetConnectIdsByDateAndGeometryResponse.class);
        return response != null ? response.getConnectIds() : Collections.emptyList();
    }

    @Override
    public List<String> forwardPosition(Map<String, String> vesselIdentifiers, String vesselFlagState, List<String> movementGuidList, HashMap<String, VesselPositionEvent> vesselTransportMeans, String receiver, String dataflow) {
        ForwardPositionRequest request = new ForwardPositionRequest();
        request.setMethod(MovementModuleMethod.FORWARD_POSITION);
        request.setVesselIdentifyingProperties(populateVesselIdentifyingProperties(vesselIdentifiers, vesselFlagState));
        request.setDataflow(dataflow);
        request.setReceiver(receiver);

        List<String> responseList = new ArrayList<>();
        HashMap<String,List<String>> guidMap = new HashMap<>();
        String movementGuid;
        String messageGuid;
        for(String guidList :movementGuidList){
            String[] guidParts = guidList.split("_");
            movementGuid = guidParts[0];
            messageGuid =  guidParts[1];
            List<String> movementGuids = guidMap.get(messageGuid);
            if(movementGuids != null && !movementGuids.isEmpty()){
                movementGuids.add(movementGuid);
                guidMap.put(messageGuid,movementGuids);
            } else{
                ArrayList<String> list = new ArrayList<>();
                list.add(movementGuid);
                guidMap.put(messageGuid,list);
            }
        }

        for(Map.Entry<String, List<String>> entry : guidMap.entrySet()){
           List<VesselPositionEvent> vesselPositionEventList = new ArrayList<>();
            for(String movGuid: entry.getValue()){
                VesselPositionEvent vesselPositionEvent = vesselTransportMeans.get(movGuid);
                vesselPositionEventList.add(vesselPositionEvent);
            }
            request.getMovementGuids().addAll(entry.getValue());
            request.getSpecifiedVesselPositionEvent().addAll(mapVesselPostionEventListToResponseType(vesselPositionEventList));
            ForwardPositionResponse response = movementClient.sendRequest(request,ForwardPositionResponse.class);
            responseList.add(response.getMessageId());
            request.getMovementGuids().clear();
            request.getSpecifiedVesselPositionEvent().clear();
        }

        return responseList;
    }

    private List<VesselPositionEventType> mapVesselPostionEventListToResponseType(List<VesselPositionEvent> vesselPositionEventList){

        List<VesselPositionEventType> responseList = new ArrayList<>();
        for(VesselPositionEvent vesselPositionEvent:vesselPositionEventList){
            VesselPositionEventType vesselPositionEventType = new VesselPositionEventType();
            vesselPositionEventType.setActivityTypeCode(mapToCodeType(vesselPositionEvent.getActivityTypeCode()));
            vesselPositionEventType.setCourseValueMeasure(mapMeasureType(vesselPositionEvent.getCourseValueMeasure()));
            vesselPositionEventType.setObtainedOccurrenceDateTime(mapDateTimeType(vesselPositionEvent.getObtainedOccurrenceDateTime()));
            vesselPositionEventType.setSpecifiedVesselGeographicalCoordinate(mapVesselGeographicalCoordinate(vesselPositionEvent.getSpecifiedVesselGeographicalCoordinate()));
            vesselPositionEventType.setSpeedValueMeasure(mapMeasureType(vesselPositionEvent.getSpeedValueMeasure()));
            vesselPositionEventType.setTypeCode(mapToCodeType(vesselPositionEvent.getTypeCode()));
            responseList.add(vesselPositionEventType);
        }
        return responseList;
    }

    eu.europa.ec.fisheries.schema.movement.v1.VesselGeographicalCoordinateType mapVesselGeographicalCoordinate(un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._20.VesselGeographicalCoordinate vesselGeographicalCoordinate){
        eu.europa.ec.fisheries.schema.movement.v1.VesselGeographicalCoordinateType responseVesselGeographicalCoordinate = new eu.europa.ec.fisheries.schema.movement.v1.VesselGeographicalCoordinateType();
        responseVesselGeographicalCoordinate.setLongitudeMeasure(mapMeasureType(vesselGeographicalCoordinate.getLongitudeMeasure()));
        responseVesselGeographicalCoordinate.setLatitudeMeasure(mapMeasureType(vesselGeographicalCoordinate.getLatitudeMeasure()));
        return  responseVesselGeographicalCoordinate;

    }

    DateTimeType mapDateTimeType(un.unece.uncefact.data.standard.unqualifieddatatype._20.DateTimeType dateTimeType){
        if(dateTimeType == null){
            return null;
        }
        DateTimeType responseDateTimeType = new DateTimeType();
        responseDateTimeType.setDateTime(dateTimeType.getDateTime().toGregorianCalendar().getTime());
        return responseDateTimeType;
    }

    MeasureType mapMeasureType(un.unece.uncefact.data.standard.unqualifieddatatype._20.MeasureType measureType){
        if(measureType == null ){
            return null;
        }
        MeasureType responseMeasureType = new MeasureType();
        responseMeasureType.setUnitCode(measureType.getUnitCode());
        responseMeasureType.setValue(measureType.getValue());
        return responseMeasureType;
    }

    CodeType mapToCodeType(un.unece.uncefact.data.standard.unqualifieddatatype._20.CodeType codeType){
        if( codeType == null){
            return null;
        }
        CodeType  responseCodeType = new CodeType();
        responseCodeType.setValue(codeType.getValue());
        responseCodeType.setListID(codeType.getListID());
        return responseCodeType;
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
}
