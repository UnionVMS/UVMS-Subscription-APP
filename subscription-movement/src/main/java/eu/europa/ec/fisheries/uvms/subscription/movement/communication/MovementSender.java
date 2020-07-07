/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.movement.communication;

import eu.europa.ec.fisheries.uvms.subscription.service.domain.AreaEntity;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Service for sending messages to Movement.
 */
public interface MovementSender {

	/**
	 * Retrieves a list of guids that according to movement module there had been at least one movement that satisfies
	 * the requested criteria
	 * @param guidList The list that will be filterer
	 * @param startDate The start date
	 * @param endDate The end date
	 * @param areas Areas that occurred a movement
	 * @return The filtered guid list
	 */
	List<String> sendFilterGuidListForAreasRequest(
            List<String> guidList,
            Date startDate,
            Date endDate,
			Collection<AreaEntity> areas);

	/**
	 *
	 * @param vesselIdentifiers the vessel identifiers
	 * @param vesselRegistrationState the country that the vessel is registered to
	 * @param movementGuidList the guids of each movement
	 * @param receiver The receiver
	 * @param dataflow The dataflow
	 * @return an OK or NOTOK string when success or failure respectively
	 */
    String forwardPosition(Map<String, String> vesselIdentifiers,
						   String vesselRegistrationState,
						   List<String> movementGuidList,
						   String receiver,
						   String dataflow);
}
