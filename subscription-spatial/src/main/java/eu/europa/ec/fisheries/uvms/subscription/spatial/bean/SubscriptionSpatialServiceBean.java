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

package eu.europa.ec.fisheries.uvms.subscription.spatial.bean;

import eu.europa.ec.fisheries.uvms.spatial.model.schemas.AreaExtendedIdentifierType;
import eu.europa.ec.fisheries.uvms.spatial.model.schemas.BatchSpatialEnrichmentRS;
import eu.europa.ec.fisheries.uvms.spatial.model.schemas.GetAreasGeometryUnionRS;
import eu.europa.ec.fisheries.uvms.spatial.model.schemas.SpatialEnrichmentRSListElement;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.AreaEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.bean.SubscriptionSpatialService;
import eu.europa.ec.fisheries.uvms.subscription.spatial.communication.SpatialSender;
import eu.europa.ec.fisheries.uvms.subscription.spatial.mapper.SpatialMapper;
import eu.europa.ec.fisheries.wsdl.subscription.module.SubscriptionAreaType;
import eu.europa.ec.fisheries.wsdl.subscription.module.SubscriptionMovementMetaDataAreaType;
import eu.europa.ec.fisheries.wsdl.subscription.module.SubscriptionMovementMetaDataAreaTypeResponseElement;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class SubscriptionSpatialServiceBean implements SubscriptionSpatialService {

    private SpatialSender spatialSender;
    /**
     * Constructor for frameworks.
     */
    @SuppressWarnings("unused")
    SubscriptionSpatialServiceBean(){
        //NOOP
    }
    @Inject
    public SubscriptionSpatialServiceBean(SpatialSender spatialSender) {
        this.spatialSender = spatialSender;
    }

    @Override
    public String getAreasGeometry(Collection<AreaEntity> areas) {
        GetAreasGeometryUnionRS response = spatialSender.getAreasGeometryUnion(areas);
        if(response != null) {
            return response.getGeometry();
        }
        return null;
    }

    @Override
    public List<SubscriptionMovementMetaDataAreaTypeResponseElement> getBatchUserAreasEnrichment(List<SubscriptionAreaType> subscriptionAreaTypes) {
        BatchSpatialEnrichmentRS response = spatialSender.getBatchUserAreasEnrichment(subscriptionAreaTypes);
        if(response == null){
            return Collections.emptyList();
        }
        return response.getEnrichmentRespLists().stream().map(re -> {
            SubscriptionMovementMetaDataAreaTypeResponseElement responseElement = new SubscriptionMovementMetaDataAreaTypeResponseElement();
            responseElement.getElements().addAll(SpatialMapper.mapSpatialAreasToSubscriptionAreas(re.getAreasByLocation().getAreas())); 
            responseElement.setCorrelationId(re.getGuid());
            return responseElement;
        }).collect(Collectors.toList());
    }

    @Override
    public List<SubscriptionMovementMetaDataAreaTypeResponseElement> getFishingActivitiesUserAreasEnrichmentByWkt(Map<String, XMLGregorianCalendar> wktDateMap) {
        BatchSpatialEnrichmentRS response = spatialSender.getUserAreasEnrichmentByWkt(wktDateMap);
        List<SubscriptionMovementMetaDataAreaTypeResponseElement> fishingEnrichmentList = new ArrayList<>();
        if(response != null) {
            List<SpatialEnrichmentRSListElement> rsListElements = response.getEnrichmentRespLists();
            for(SpatialEnrichmentRSListElement rsListElement : rsListElements) {
                List<AreaExtendedIdentifierType> areas = rsListElement.getAreasByLocation().getAreas();
                List<SubscriptionMovementMetaDataAreaType> fishingEnrichment = SpatialMapper.mapSpatialAreasToSubscriptionAreas(areas);
                SubscriptionMovementMetaDataAreaTypeResponseElement element = new SubscriptionMovementMetaDataAreaTypeResponseElement();
                if(!fishingEnrichment.isEmpty()){
                    element.getElements().addAll(fishingEnrichment);
                }
                fishingEnrichmentList.add(element);
            }
        }
        return fishingEnrichmentList;
    }
}
