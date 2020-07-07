/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.service.dao;

import java.util.Date;
import java.util.Set;
import java.util.stream.Stream;

import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionExecutionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionDataEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionEntity;
import eu.europa.fisheries.uvms.subscription.model.enums.SubscriptionExecutionStatusType;

/**
 * DAO for the {@link SubscriptionExecutionEntity}.
 */
public interface SubscriptionExecutionDao {
	/**
	 * Persist the given entity.
	 *
	 * @param entity The subscription execution to persist
	 * @return The persisted entity, with id filled
	 */
	SubscriptionExecutionEntity create(SubscriptionExecutionEntity entity);

	/**
	 * Find an execution by id.
	 *
	 * @param id The id
	 * @return The execution, or {@code null} if not found
	 */
	SubscriptionExecutionEntity findById(Long id);

	/**
	 * Find ids of all executions with status {@code PENDING} and {@code requestTime <= requestTimeCutoff}.
	 *
	 * @param requestTimeCutoff The {@code requestTime} parameter
	 * @return The ids of the executions that match the criteria
	 */
	Stream<Long> findIdsOfPendingWithRequestDateBefore(Date requestTimeCutoff);

	/**
	 * Find executions of the given triggered subscription having the given status.
	 *
	 * @param triggeredSubscription The triggered subscription
	 * @param status                The status
	 * @return The executions
	 */
	Stream<SubscriptionExecutionEntity> findByTriggeredSubscriptionAndStatus(TriggeredSubscriptionEntity triggeredSubscription, SubscriptionExecutionStatusType status);

	/**
	 * Find pending executions of the given subscription and having the given data.
	 *
	 * @param subscription The subscription
	 * @param dataCriteria The {@code TriggeredSubscriptionDataEntity} to match with
	 * @return A possibly empty stream of matching executions
	 */
	Stream<SubscriptionExecutionEntity> findPendingBy(SubscriptionEntity subscription, Set<TriggeredSubscriptionDataEntity> dataCriteria);
}
