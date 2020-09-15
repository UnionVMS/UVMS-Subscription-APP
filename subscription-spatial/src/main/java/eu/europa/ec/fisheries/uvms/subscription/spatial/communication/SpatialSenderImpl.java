/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.spatial.communication;

import eu.europa.ec.fisheries.uvms.spatial.model.schemas.BatchSpatialEnrichmentRS;
import eu.europa.ec.fisheries.uvms.spatial.model.schemas.GetAreasGeometryUnionRS;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.AreaEntity;
import eu.europa.ec.fisheries.uvms.subscription.spatial.mapper.SpatialMapper;
import eu.europa.ec.fisheries.wsdl.subscription.module.SubscriptionAreaType;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Implementation of {@link SpatialSender}.
 */
@ApplicationScoped
class SpatialSenderImpl implements SpatialSender {

	private SpatialClient spatialClient;

	/**
	 * Constructor for frameworks.
	 */
	@SuppressWarnings("unused")
	SpatialSenderImpl(){
		//NOOP
	}

	/**
	 * Injection constructor
	 *
	 * @param spatialClient The low-level client to the services of the spatial module
	 */
	@Inject
	public SpatialSenderImpl(SpatialClient spatialClient) {
		this.spatialClient = spatialClient;
	}

	@Override
	public BatchSpatialEnrichmentRS getBatchUserAreasEnrichment(List<SubscriptionAreaType> subscriptionAreaTypes) {
		return spatialClient.sendRequest(SpatialMapper.batchSpatialEnrichmentRQ(subscriptionAreaTypes), BatchSpatialEnrichmentRS.class);
	}

	@Override
	public BatchSpatialEnrichmentRS getUserAreasEnrichmentByWkt(Map<String, XMLGregorianCalendar> wktDateMap) {
		return spatialClient.sendRequest(SpatialMapper.spatialEnrichmentRQByWkt(wktDateMap), BatchSpatialEnrichmentRS.class);
	}

	@Override
	public GetAreasGeometryUnionRS getAreasGeometryUnion(Collection<AreaEntity> areaEntities) {
		return spatialClient.sendRequest(SpatialMapper.createGetAreasGeometryUnionRQ(areaEntities), GetAreasGeometryUnionRS.class);
	}

}
