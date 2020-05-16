/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.service.dao;

import static eu.europa.fisheries.uvms.subscription.model.enums.SubscriptionExecutionStatusType.PENDING;
import static eu.europa.fisheries.uvms.subscription.model.enums.SubscriptionExecutionStatusType.QUEUED;
import static java.lang.Boolean.TRUE;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionExecutionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionExecutionEntity_;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionDataEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionDataEntity_;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionEntity_;
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

	@Override
	public boolean activeExists(SubscriptionEntity subscription, Set<TriggeredSubscriptionDataEntity> dataCriteria) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<TriggeredSubscriptionEntity> query = cb.createQuery(TriggeredSubscriptionEntity.class);
		Root<TriggeredSubscriptionEntity> root = query.from(TriggeredSubscriptionEntity.class);

		List<Predicate> predicates = new ArrayList<>();
		predicates.add(cb.equal(root.get(TriggeredSubscriptionEntity_.subscription), subscription));
		predicates.add(cb.equal(root.get(TriggeredSubscriptionEntity_.active), TRUE));

		Subquery<Long> executionSubquery = query.subquery(Long.class);
		executionSubquery.select(cb.literal(1L));
		Root<SubscriptionExecutionEntity> executionRoot = executionSubquery.from(SubscriptionExecutionEntity.class);
		executionSubquery.where(
				cb.in(executionRoot.get(SubscriptionExecutionEntity_.status)).value(PENDING).value(QUEUED),
				cb.equal(executionRoot.get(SubscriptionExecutionEntity_.triggeredSubscription), root)
		);
		predicates.add(cb.exists(executionSubquery));

		dataCriteria.stream()
				.map(d -> {
					Subquery<Long> dataSubquery = query.subquery(Long.class);
					dataSubquery.select(cb.literal(1L));
					Root<TriggeredSubscriptionDataEntity> dataRoot = dataSubquery.from(TriggeredSubscriptionDataEntity.class);
					dataSubquery.where(
							cb.equal(dataRoot.get(TriggeredSubscriptionDataEntity_.triggeredSubscription), root),
							cb.equal(dataRoot.get(TriggeredSubscriptionDataEntity_.key), d.getKey()),
							cb.equal(dataRoot.get(TriggeredSubscriptionDataEntity_.value), d.getValue())
					);
					return cb.exists(dataSubquery);
				})
				.forEach(predicates::add);

		query.select(root).where(predicates.toArray(new Predicate[0]));

		return !em.createQuery(query).setMaxResults(1).getResultList().isEmpty();
	}
}
