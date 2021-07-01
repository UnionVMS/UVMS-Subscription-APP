/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.movement.communication;

import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._20.VesselPositionEvent;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for sending messages to Movement.
 */
public interface MovementSender {

	/**
	 * Retrieves a list of guids that according to movement module there had been at least one movement that satisfies
	 * the requested criteria
	 * @param inList The list that will be filtered, can be empty
	 * @param startDate The start date
	 * @param endDate The end date
	 * @param areasGeometryUnion Areas geometry union string (wkt)
	 * @param fromPage Page number
	 * @param limit Records limit
	 * @return The filtered guid list
	 */
	List<String> sendGetConnectIdsByDateAndGeometryRequest(
            List<String> inList,
            Date startDate,
            Date endDate,
			String areasGeometryUnion,
			Integer fromPage,
			Integer limit);

	/**
	 * Call @{@code FORWARD_POSITION}
	 * @param vesselIdentifiers the vessel identifiers
	 * @param vesselRegistrationState the country that the vessel is registered to
	 * @param movementGuidList the guids of each movement
	 * @param receiver The receiver
	 * @param dataflow The dataflow
	 * @return an OK or NOTOK string when success or failure respectively
	 */
    List<String> forwardPosition(Map<String, String> vesselIdentifiers,
						   String vesselRegistrationState,
						   List<String> movementGuidList,
						   HashMap<String,VesselPositionEvent> vesselTransportMeans,
						   String receiver,
						   String dataflow);
}
