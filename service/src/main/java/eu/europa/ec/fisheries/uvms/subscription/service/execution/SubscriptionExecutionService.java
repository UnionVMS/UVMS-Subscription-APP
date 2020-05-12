/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.service.execution;

import java.util.Date;
import java.util.stream.Stream;

import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionExecutionEntity;

/**
 * Service to execute a subscription.
 */
public interface SubscriptionExecutionService {
	/**
	 * Save the given entity to persistent store.
	 *
	 * @param entity The entity to save
	 * @return The saved entity
	 */
	SubscriptionExecutionEntity save(SubscriptionExecutionEntity entity);

	/**
	 * Find the ids of any executions that must be activated by the given date.
	 *
	 * @param activationDate The activation date
	 * @return A stream of ids of executions that must be activated by the given date
	 */
	Stream<Long> findPendingSubscriptionExecutionIds(Date activationDate);

	/**
	 * Enqueue the given entity for execution and mark it as queued, in a separate transaction.
	 *
	 * @param executionId The id of the entity to enqueue
	 */
	void enqueueForExecutionInNewTransaction(Long executionId);

	/**
	 * Execute the execution with the given id, if it is still elligible for execution.
	 *
	 * @param id The execution id
	 */
	void execute(Long id);
}
