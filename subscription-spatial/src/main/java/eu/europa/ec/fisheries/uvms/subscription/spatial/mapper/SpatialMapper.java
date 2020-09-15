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

package eu.europa.ec.fisheries.uvms.subscription.spatial.mapper;

import eu.europa.ec.fisheries.uvms.spatial.model.mapper.SpatialModuleRequestMapper;
import eu.europa.ec.fisheries.uvms.spatial.model.schemas.AreaExtendedIdentifierType;
import eu.europa.ec.fisheries.uvms.spatial.model.schemas.AreaIdentifierType;
import eu.europa.ec.fisheries.uvms.spatial.model.schemas.AreaType;
import eu.europa.ec.fisheries.uvms.spatial.model.schemas.AreaTypeElement;
import eu.europa.ec.fisheries.uvms.spatial.model.schemas.BatchSpatialEnrichmentRQ;
import eu.europa.ec.fisheries.uvms.spatial.model.schemas.BatchSpatialUserAreaEnrichmentRQ;
import eu.europa.ec.fisheries.uvms.spatial.model.schemas.GetAreasGeometryUnionRQ;
import eu.europa.ec.fisheries.uvms.spatial.model.schemas.PointType;
import eu.europa.ec.fisheries.uvms.spatial.model.schemas.SpatialEnrichmentRQListElement;
import eu.europa.ec.fisheries.uvms.spatial.model.schemas.SpatialModuleMethod;
import eu.europa.ec.fisheries.uvms.spatial.model.schemas.UnitType;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.AreaEntity;
import eu.europa.ec.fisheries.wsdl.subscription.module.SubscriptionAreaType;
import eu.europa.ec.fisheries.wsdl.subscription.module.SubscriptionMovementMetaDataAreaType;

import javax.xml.datatype.XMLGregorianCalendar;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SpatialMapper {
    
    public static BatchSpatialEnrichmentRQ batchSpatialEnrichmentRQ(List<SubscriptionAreaType> subscriptionAreaTypes){
        List<SpatialEnrichmentRQListElement> batchReqElements = new ArrayList<>();
        for (SubscriptionAreaType subscriptionAreaType : subscriptionAreaTypes) {
            PointType point = new PointType();
            point.setCrs(subscriptionAreaType.getCrs());
            point.setLatitude(subscriptionAreaType.getLatitude());
            point.setLongitude(subscriptionAreaType.getLongitude());
            SpatialEnrichmentRQListElement spatialEnrichmentRQListElement = SpatialModuleRequestMapper.mapToCreateSpatialEnrichmentRQElement(point, UnitType.NAUTICAL_MILES, null, null);
            spatialEnrichmentRQListElement.setUserAreaActiveDate(subscriptionAreaType.getUserAreaActiveDate());
            spatialEnrichmentRQListElement.setGuid(subscriptionAreaType.getCorrelationId());
            batchReqElements.add(spatialEnrichmentRQListElement);
        }
        BatchSpatialEnrichmentRQ request = new BatchSpatialEnrichmentRQ();
        request.setEnrichmentLists(batchReqElements);
        request.setMethod(SpatialModuleMethod.GET_USER_AREA_ENRICHMENT_BATCH);
        return request;
    }
    
    public static BatchSpatialUserAreaEnrichmentRQ spatialEnrichmentRQByWkt(Map<String, XMLGregorianCalendar> wktDateMap){
        List<AreaTypeElement> list = new ArrayList<>();
        for (Map.Entry<String,XMLGregorianCalendar> wkt : wktDateMap.entrySet()) {
            AreaTypeElement areaSimpleType = new AreaTypeElement();
            areaSimpleType.setWkt(wkt.getKey());
            areaSimpleType.setUserAreaActiveDate(wkt.getValue());
            list.add(areaSimpleType);
        }
        BatchSpatialUserAreaEnrichmentRQ request = new BatchSpatialUserAreaEnrichmentRQ();
        request.setAreaTypes(list);
        request.setMethod(SpatialModuleMethod.GET_USER_AREA_ENRICHMENT_BY_WKT);
        return request;
    }
    
    public static GetAreasGeometryUnionRQ createGetAreasGeometryUnionRQ(Collection<AreaEntity> areaEntities){
        GetAreasGeometryUnionRQ request = new GetAreasGeometryUnionRQ();
        request.setMethod(SpatialModuleMethod.GET_AREAS_GEOMETRY_UNION);
        List<AreaIdentifierType> areaIdentifierTypes = request.getAreas();
        for(AreaEntity areaEntity : areaEntities) {
            AreaIdentifierType areaIdentifierType = new AreaIdentifierType();
            areaIdentifierType.setAreaType(AreaType.valueOf(areaEntity.getAreaType().name()));
            areaIdentifierType.setId(String.valueOf(areaEntity.getGid()));
            areaIdentifierTypes.add(areaIdentifierType);
        }
        return request;
    }

    public static List<SubscriptionMovementMetaDataAreaType> mapSpatialAreasToSubscriptionAreas(List<AreaExtendedIdentifierType> spatialAreas){
        return spatialAreas.stream().map(SpatialMapper::mapSpatialAreaToSubscriptionArea).collect(Collectors.toList());
    }

    public static SubscriptionMovementMetaDataAreaType mapSpatialAreaToSubscriptionArea(AreaExtendedIdentifierType spatialArea) {
        if(spatialArea == null) {
            return null;
        }
        SubscriptionMovementMetaDataAreaType movementMetaDataAreaType = new SubscriptionMovementMetaDataAreaType();
        movementMetaDataAreaType.setAreaType(spatialArea.getAreaType().name());
        movementMetaDataAreaType.setCode(spatialArea.getCode());
        movementMetaDataAreaType.setRemoteId(spatialArea.getId());
        movementMetaDataAreaType.setName(spatialArea.getName());
        movementMetaDataAreaType.setTransitionType("POS");
        return movementMetaDataAreaType;
    }
}
