/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.service.dao;

import java.util.Set;
import java.util.stream.Stream;

import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionDataEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.TriggeredSubscriptionSearchCriteria;

/**
 * DAO for {@link TriggeredSubscriptionEntity}.
 */
public interface TriggeredSubscriptionDao {
	/**
	 * Persist the given entity.
	 *
	 * @param entity The triggered subscription to persist
	 * @return The persisted entity, with id filled
	 */
	TriggeredSubscriptionEntity create(TriggeredSubscriptionEntity entity);

	/**
	 * Find the triggered subscription with the given id.
	 *
	 * @param id The id
	 * @return The found triggered subscription, throws {@code EntityDoesNotExistException} if not found
	 */
	TriggeredSubscriptionEntity getById(Long id);

	/**
	 * Check if the database contains an active triggered instance of the given subscription,
	 * having the given data.
	 *
	 * @param subscription The subscription
	 * @param dataCriteria The data
	 * @return Whether a duplicate exists
	 */
	boolean activeExists(SubscriptionEntity subscription, Set<TriggeredSubscriptionDataEntity> dataCriteria);

	/**
	 * Search for triggered subscriptions with the given criteria.
	 *
	 * @param criteria The criteria
	 * @return The found entities
	 */
	Stream<TriggeredSubscriptionEntity> find(TriggeredSubscriptionSearchCriteria criteria);
}
