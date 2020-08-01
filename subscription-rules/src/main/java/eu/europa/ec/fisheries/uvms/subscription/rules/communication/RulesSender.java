/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.rules.communication;

import java.util.Date;
import java.util.List;
import java.util.Map;

import eu.europa.ec.fisheries.wsdl.asset.types.VesselIdentifiersHolder;
import eu.europa.fisheries.uvms.subscription.model.enums.TriggerType;

/**
 * Service for sending messages to Rules module.
 */
public interface RulesSender {

	/**
	 * Send message to Rule to create an alarm report.
	 *
	 * @param subscriptionName  The name of the {@code SubscriptionEntity}
	 * @param openDate The rule open date
	 * @param vesselIdentifiers The vessel ids, including the asset guid
	 * @param movementGuids The movement GUIDs for which to create tickets
	 */
	void createAlertsAsync(String subscriptionName, Date openDate, VesselIdentifiersHolder vesselIdentifiers, List<String> movementGuids);
}
