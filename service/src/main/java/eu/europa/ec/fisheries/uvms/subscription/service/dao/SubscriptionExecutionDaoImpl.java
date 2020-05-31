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
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.Date;
import java.util.stream.Stream;

import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionExecutionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionExecutionEntity_;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionEntity;
import eu.europa.fisheries.uvms.subscription.model.enums.SubscriptionExecutionStatusType;

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

	@Override
	public SubscriptionExecutionEntity findById(Long id) {
		return em.find(SubscriptionExecutionEntity.class, id);
	}

	@Override
	public Stream<Long> findIdsOfPendingWithRequestDateBefore(Date requestTimeCutoff) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Long> query = cb.createQuery(Long.class);
		Root<SubscriptionExecutionEntity> execution = query.from(SubscriptionExecutionEntity.class);
		query.select(execution.get(SubscriptionExecutionEntity_.id)).where(
				cb.equal(execution.get(SubscriptionExecutionEntity_.status), SubscriptionExecutionStatusType.PENDING),
				cb.lessThanOrEqualTo(execution.get(SubscriptionExecutionEntity_.requestedTime), requestTimeCutoff)
		);
		return em.createQuery(query).getResultList().stream(); // TODO Just stream, when we finally sort out the mess with dependencies that is bringing JPA 1.x
	}

	@Override
	public Stream<SubscriptionExecutionEntity> findByTriggeredSubscriptionAndStatus(TriggeredSubscriptionEntity triggeredSubscription, SubscriptionExecutionStatusType status) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<SubscriptionExecutionEntity> query = cb.createQuery(SubscriptionExecutionEntity.class);
		Root<SubscriptionExecutionEntity> execution = query.from(SubscriptionExecutionEntity.class);
		query.where(
				cb.equal(execution.get(SubscriptionExecutionEntity_.triggeredSubscription), triggeredSubscription),
				cb.equal(execution.get(SubscriptionExecutionEntity_.status), status)
		);
		return em.createQuery(query).getResultList().stream(); // TODO Just stream, when we finally sort out the mess with dependencies that is bringing JPA 1.x
	}
}
