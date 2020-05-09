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

import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionExecutionEntity;

/**
 * JPA implementation of the {@link SubscriptionExecutionDao}.
 */
@ApplicationScoped
class SubscriptionExecutionDaoImpl implements SubscriptionExecutionDao {

	private EntityManager em;

	/**
	 * Injection constructor.
	 *
	 * @param em The entity manager
	 */
	@Inject
	public SubscriptionExecutionDaoImpl(EntityManager em) {
		this.em = em;
	}

	/**
	 * Constructor for frameworks.
	 */
	@SuppressWarnings("unused")
	SubscriptionExecutionDaoImpl() {
		// NOOP
	}

	@Override
	public SubscriptionExecutionEntity create(SubscriptionExecutionEntity entity) {
		em.persist(entity);
		return entity;
	}
}
