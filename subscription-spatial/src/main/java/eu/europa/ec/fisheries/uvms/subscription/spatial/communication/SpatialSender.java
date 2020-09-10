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
import eu.europa.ec.fisheries.wsdl.subscription.module.SubscriptionAreaSimpleType;

import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * Service for sending messages to Spatial module.
 */
public interface SpatialSender {

	/**
	 * Sends message to Spatial in order to enrich movements with user defined areas
	 * 
	 * @param subscriptionAreaSimpleTypes The SubscriptionAreaSimpleType list
	 * @return BatchSpatialEnrichmentRS enriched with user areas, if found
	 */
	BatchSpatialEnrichmentRS getBatchUserAreasEnrichment(List<SubscriptionAreaSimpleType> subscriptionAreaSimpleTypes);

	/**
	 * Sends message to Spatial in order to enrich movements with user defined areas
	 *
	 * @param wktDateMap The wkt-date map, represents wkt and its user area active date.
	 * @return BatchSpatialEnrichmentRS enriched with user areas, if found
	 */
	BatchSpatialEnrichmentRS getUserAreasEnrichmentByWkt(Map<String, XMLGregorianCalendar> wktDateMap);
}
