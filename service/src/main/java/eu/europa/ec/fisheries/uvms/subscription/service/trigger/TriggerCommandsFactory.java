/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.service.trigger;

import java.util.Set;
import java.util.function.Function;

import eu.europa.ec.fisheries.uvms.subscription.service.bean.Command;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionDataEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.messaging.AssetPageRetrievalMessage;

/**
 * Factory for creating trigger and stop condition commands.
 */
public interface TriggerCommandsFactory {
	/**
	 * Create a command for activating the given triggered subscription, if not already triggered.
	 *
	 * @param triggeredSubscription A candidate triggered subscription
	 * @param extractTriggeredSubscriptionDataForDuplicates Function to extract the set of triggered subscription data for the given
	 *                              {@code TriggeredSubscriptionEntity} to check if there is another active instance of the same subscription
	 * @return The command
	 */
	Command createTriggerSubscriptionCommand(TriggeredSubscriptionEntity triggeredSubscription, Function<TriggeredSubscriptionEntity, Set<TriggeredSubscriptionDataEntity>> extractTriggeredSubscriptionDataForDuplicates);

	/**
	 * Make a command to stop all triggered subscriptions that match the criteria.
	 *
	 * @param stopConditionCriteria The criteria
	 * @return The command
	 */
	Command createStopSubscriptionCommand(StopConditionCriteria stopConditionCriteria);

	/**
	 * Make a command to trigger paged retrieval of a group of assets
	 *
	 * @param message The message object containing the info for the retrieval
	 * @return The command
	 */
	Command createAssetPageRetrievalCommand(AssetPageRetrievalMessage message);
}
