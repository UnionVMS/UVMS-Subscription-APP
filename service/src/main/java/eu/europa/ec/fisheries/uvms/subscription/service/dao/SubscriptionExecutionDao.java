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
import java.util.stream.Stream;

import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionExecutionEntity;

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
	 * Find all executions with status {@code PENDING} and {@code requestTime <= requestTimeCutoff}.
	 *
	 * @param requestTimeCutoff The {@code requestTime} parameter
	 * @return The executions that match the criteria
	 */
	Stream<SubscriptionExecutionEntity> findPendingWithRequestDateBefore(Date requestTimeCutoff);
}
