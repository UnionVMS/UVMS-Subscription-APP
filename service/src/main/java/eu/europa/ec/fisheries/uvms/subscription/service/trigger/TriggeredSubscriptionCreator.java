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
import java.util.stream.Stream;

import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionDataEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionEntity;

/**
 * Interface for services that check if an incoming data change message should trigger any subscriptions
 * and create the appropriate {@link TriggeredSubscriptionEntity} objects.
 * <p>
 * <em>NOTE:</em> Implementations of this class should be singletons/application scoped objects
 */
public interface TriggeredSubscriptionCreator {
	/**
	 * Get the name of the subscription source that this predicate can check.
	 */
	String getEligibleSubscriptionSource();

	/**
	 * Check if this message should trigger subscriptions and create their objects.
	 * This method only creates the triggered subscriptions in memory, does not persist them.
	 *
	 * @param representation The representation of the message as string, as it arrived in the messaging facilities
	 * @return A possibly empty but never null stream of not persisted triggered subscription objects
	 */
	Stream<TriggeredSubscriptionEntity> createTriggeredSubscriptions(String representation);

	/**
	 * Extract the set of triggered subscription data for the given {@code TriggeredSubscriptionEntity} that
	 * should be used to check if there exists another active instance of the same subscription.
	 * <p>
	 * For example, data for a subscription triggered by a movement are the vessel id and the occurrence time,
	 * i.e. {@code TriggeredSubscriptionEntity.data} has something like {@code {vesselId: 123, occurrenceTime: "2020-05-16 12:00"}}.
	 * We need to keep the vessel id as a criterion: if the same {@code SubscriptionEntity} has active triggered
	 * subscriptions for the same vessel id, then this is duplicate.
	 * On the other hand, the occurrence time is not important. So, this method should return only {@code {vesselId: 123}}.
	 *
	 * @param entity The triggered subscription to check if it is duplicate
	 * @return The data to use as criteria
	 */
	Set<TriggeredSubscriptionDataEntity> extractTriggeredSubscriptionDataForDuplicates(TriggeredSubscriptionEntity entity);
}
