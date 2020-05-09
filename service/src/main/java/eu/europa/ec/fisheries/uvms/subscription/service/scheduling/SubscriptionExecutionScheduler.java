/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.service.scheduling;

import java.util.Optional;

import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionExecutionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionEntity;

/**
 * Service that calculates the next execution of a triggered subscription
 * in the form of a {@link SubscriptionExecutionEntity}.
 */
public interface SubscriptionExecutionScheduler {
	/**
	 * Calculate the next execution of the given {@link TriggeredSubscriptionEntity}, if any, based on the
	 * given last execution.
	 * <p>
	 * If there should be no more executions of the given subscription, it returns an empty optional.
	 * It is NOT the responsibility of this component to change the input values, e.g. set
	 * {@code TriggeredSubscriptionEntity.active} to {@code false}.
	 * It is NOT the responsibility of this component to save the {@code SubscriptionExecutionEntity}
	 * to persistent storage.
	 *
	 * @param triggeredSubscription The triggered subscription to calculate next execution
	 * @param lastExecution         The last execution, may be {@code null}
	 * @return An optional {@code SubscriptionExecutionEntity}, representing the next execution of the subscription
	 */
	Optional<SubscriptionExecutionEntity> scheduleNext(TriggeredSubscriptionEntity triggeredSubscription, SubscriptionExecutionEntity lastExecution);

	/**
	 * Convenience method to call {@link #scheduleNext(TriggeredSubscriptionEntity, SubscriptionExecutionEntity)}
	 * with {@code lastExecution} set to {@code null}.
	 *
	 * @param triggeredSubscription The triggered subscription to calculate next execution
	 * @return An optional {@code SubscriptionExecutionEntity}, representing the next execution of the subscription
	 */
	default Optional<SubscriptionExecutionEntity> scheduleNext(TriggeredSubscriptionEntity triggeredSubscription) {
		return scheduleNext(triggeredSubscription, null);
	}
}
