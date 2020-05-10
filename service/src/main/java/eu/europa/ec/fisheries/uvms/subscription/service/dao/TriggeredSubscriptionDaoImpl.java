/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.service.dao;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionEntity;
import eu.europa.fisheries.uvms.subscription.model.exceptions.EntityDoesNotExistException;

/**
 * Implementation of {@link TriggeredSubscriptionDao} using JPA.
 */
@ApplicationScoped
class TriggeredSubscriptionDaoImpl implements TriggeredSubscriptionDao {

	private EntityManager em;

	/**
	 * Constructor for injection.
	 *
	 * @param em The entity manager
	 */
	@Inject
	public TriggeredSubscriptionDaoImpl(EntityManager em) {
		this.em = em;
	}

	/**
	 * Constructor for frameworks.
	 */
	@SuppressWarnings("unused")
	TriggeredSubscriptionDaoImpl() {
		// NOOP
	}

	@Override
	public TriggeredSubscriptionEntity create(TriggeredSubscriptionEntity entity) {
		em.persist(entity);
		return entity;
	}

	@Override
	public TriggeredSubscriptionEntity getById(Long id) {
		TriggeredSubscriptionEntity result = em.find(TriggeredSubscriptionEntity.class, id);
		if (result == null) {
			throw new EntityDoesNotExistException("TriggeredSubscription with id " + id);
		}
		return result;
	}
}