/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.service.bean;

import java.util.Set;
import java.util.stream.Stream;

import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionDataEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.trigger.StopConditionCriteria;

/**
 * Service for triggered subscriptions.
 */
public interface TriggeredSubscriptionService {
	/**
	 * Save the given entity to persistent store.
	 *
	 * @param triggeredSubscription The entity to save
	 * @return The saved entity
	 */
	TriggeredSubscriptionEntity save(TriggeredSubscriptionEntity triggeredSubscription);

	/**
	 * Check if the subscription of the given {@code TriggeredSubscriptionEntity} already has another active
	 * triggered subscription, taking into account the given data for duplicates.
	 *
	 * @param entity            The candidate triggered subscription to check if there are already duplicates
	 * @param dataForDuplicates The data to use as criteria
	 * @return Whether the given entity is duplicate
	 */
	boolean isDuplicate(TriggeredSubscriptionEntity entity, Set<TriggeredSubscriptionDataEntity> dataForDuplicates);

	/**
	 * Find triggered subscriptions that fulfill the given criteria.
	 *
	 * @param criteria The criteria
	 * @return A stream of triggered subscriptions that should be deactivated
	 */
	Stream<TriggeredSubscriptionEntity> findByStopConditionCriteria(StopConditionCriteria criteria);
}
