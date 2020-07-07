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
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.SetJoin;
import javax.persistence.criteria.Subquery;
import java.util.Date;
import java.util.Set;
import java.util.stream.Stream;

import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionExecutionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionExecutionEntity_;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionDataEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionDataEntity_;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionEntity_;
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

	@Override
	public Stream<SubscriptionExecutionEntity> findPendingBy(SubscriptionEntity subscription, Set<TriggeredSubscriptionDataEntity> dataCriteria) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<SubscriptionExecutionEntity> query = cb.createQuery(SubscriptionExecutionEntity.class);
		Root<SubscriptionExecutionEntity> execution = query.from(SubscriptionExecutionEntity.class);

		Subquery<Number> triggeringsSubquery = query.subquery(Number.class);
		Root<SubscriptionExecutionEntity> correlatedRoot = triggeringsSubquery.correlate(execution);
		Join<SubscriptionExecutionEntity, TriggeredSubscriptionEntity> triggeredSubscriptionRoot = correlatedRoot.join(SubscriptionExecutionEntity_.triggeredSubscription);

		Subquery<Number> dataSubquery = triggeringsSubquery.subquery(Number.class);
		Join<SubscriptionExecutionEntity, TriggeredSubscriptionEntity> triggeredSubscriptionCorrelatedRoot = dataSubquery.correlate(triggeredSubscriptionRoot);
		SetJoin<TriggeredSubscriptionEntity, TriggeredSubscriptionDataEntity> dataRoot = triggeredSubscriptionCorrelatedRoot.join(TriggeredSubscriptionEntity_.data);
		dataSubquery.select(cb.literal(1));
		Predicate[] predicates = dataCriteria.stream()
				.map(d -> cb.and(cb.equal(dataRoot.get(TriggeredSubscriptionDataEntity_.key), d.getKey()), cb.equal(dataRoot.get(TriggeredSubscriptionDataEntity_.value), d.getValue())))
				.toArray(Predicate[]::new);
		dataSubquery.where(cb.or(predicates));

		triggeringsSubquery.select(cb.literal(1))
				.where(
						cb.equal(triggeredSubscriptionRoot.get(TriggeredSubscriptionEntity_.subscription), subscription),
						cb.exists(dataSubquery)
				);

		query.select(execution).where(
				cb.equal(execution.get(SubscriptionExecutionEntity_.status), SubscriptionExecutionStatusType.PENDING),
				cb.exists(triggeringsSubquery)
		);
		return em.createQuery(query).getResultList().stream(); // TODO Just stream, when we finally sort out the mess with dependencies that is bringing JPA 1.x
	}
}
