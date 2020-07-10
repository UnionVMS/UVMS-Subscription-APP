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
import static eu.europa.fisheries.uvms.subscription.model.enums.TriggeredSubscriptionStatus.ACTIVE;

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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import eu.europa.ec.fisheries.uvms.subscription.service.domain.AreaEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.AreaEntity_;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity_;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionExecutionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionExecutionEntity_;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionFishingActivity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionFishingActivity_;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionDataEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionDataEntity_;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionEntity_;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.ActivityCriterion;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.AreaCriterion;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.TriggeredSubscriptionSearchCriteria;
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
		return !em.createQuery(makeQueryForActivatedTriggerings(subscription, dataCriteria)).setMaxResults(1).getResultList().isEmpty();
	}

	private CriteriaQuery<TriggeredSubscriptionEntity> makeQueryForActivatedTriggerings(SubscriptionEntity subscription, Set<TriggeredSubscriptionDataEntity> dataCriteria) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<TriggeredSubscriptionEntity> query = cb.createQuery(TriggeredSubscriptionEntity.class);
		Root<TriggeredSubscriptionEntity> root = query.from(TriggeredSubscriptionEntity.class);

		Subquery<Long> executionSubquery = query.subquery(Long.class);
		executionSubquery.select(cb.literal(1L));
		Root<SubscriptionExecutionEntity> executionRoot = executionSubquery.from(SubscriptionExecutionEntity.class);
		executionSubquery.where(
				cb.in(executionRoot.get(SubscriptionExecutionEntity_.status)).value(PENDING).value(QUEUED),
				cb.equal(executionRoot.get(SubscriptionExecutionEntity_.triggeredSubscription), root)
		);

		query.select(root).where(
				cb.equal(root.get(TriggeredSubscriptionEntity_.subscription), subscription),
				cb.equal(root.get(TriggeredSubscriptionEntity_.status), ACTIVE),
				cb.exists(executionSubquery),
				makeDataSubquery(query, root, cb, dataCriteria.stream().collect(Collectors.toMap(
						TriggeredSubscriptionDataEntity::getKey,
						TriggeredSubscriptionDataEntity::getValue
				)))
		);

		return query;
	}

	@Override
	public Stream<TriggeredSubscriptionEntity> findAlreadyActivated(SubscriptionEntity subscription, Set<TriggeredSubscriptionDataEntity> dataCriteria) {
		return em.createQuery(makeQueryForActivatedTriggerings(subscription, dataCriteria)).getResultList().stream(); // TODO Just stream, when we finally sort out the mess with dependencies that is bringing JPA 1.x
	}

	@Override
	public Stream<TriggeredSubscriptionEntity> find(TriggeredSubscriptionSearchCriteria criteria) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<TriggeredSubscriptionEntity> query = cb.createQuery(TriggeredSubscriptionEntity.class);
		Root<TriggeredSubscriptionEntity> root = query.from(TriggeredSubscriptionEntity.class);

		List<Predicate> predicates = new ArrayList<>();
		if (criteria.getWithStatus() != null) {
			predicates.add(root.get(TriggeredSubscriptionEntity_.status).in(criteria.getWithStatus()));
		}
		if (criteria.getNotInAreas() != null) {
			predicates.addAll(makeNotInAreasSubquery(query, root, cb, criteria.getNotInAreas()));
			if (criteria.getSubscriptionQuitArea() != null) {
				predicates.add(cb.equal(root.get(TriggeredSubscriptionEntity_.subscription).get(SubscriptionEntity_.stopWhenQuitArea), criteria.getSubscriptionQuitArea()));
			}
		}
		if (criteria.getTriggeredSubscriptionData() != null) {
			predicates.add(makeDataSubquery(query, root, cb, criteria.getTriggeredSubscriptionData()));
		}
		if(criteria.getWithAnyStopActivities() != null) {
			predicates.addAll(makeStopActivitiesSubquery(query, root, cb, criteria.getWithAnyStopActivities()));
		}
		query.select(root).where(predicates.toArray(new Predicate[0]));
		return em.createQuery(query).getResultList().stream(); // TODO Just stream, when we finally sort out the mess with dependencies that is bringing JPA 1.x
	}

	private List<Predicate> makeNotInAreasSubquery(CriteriaQuery<TriggeredSubscriptionEntity> query, Root<TriggeredSubscriptionEntity> root, CriteriaBuilder cb, Collection<AreaCriterion> areas) {
		List<Predicate> outcome = new ArrayList<>();
		outcome.add(cb.equal(root.get(TriggeredSubscriptionEntity_.subscription).get(SubscriptionEntity_.hasAreas), true));
		Subquery<Long> subquery = query.subquery(Long.class);
		Root<AreaEntity> area = subquery.from(AreaEntity.class);
		subquery.select(area.get(AreaEntity_.id));
		Predicate[] predicates = areas.stream()
				.map(a -> cb.and(cb.equal(area.get(AreaEntity_.gid), a.getGid()), cb.equal(area.get(AreaEntity_.areaType), a.getType())))
				.toArray(Predicate[]::new);
		subquery.where(cb.and(cb.equal(area.get(AreaEntity_.subscription), root.get(TriggeredSubscriptionEntity_.subscription)), cb.or(predicates)));
		outcome.add(cb.not(cb.exists(subquery)));
		return outcome;
	}

	private Predicate makeDataSubquery(CriteriaQuery<TriggeredSubscriptionEntity> query, Root<TriggeredSubscriptionEntity> root, CriteriaBuilder cb, Map<String,String> data) {
		Subquery<Number> subquery = query.subquery(Number.class);
		Root<TriggeredSubscriptionEntity> correlatedRoot = subquery.correlate(root);
		SetJoin<TriggeredSubscriptionEntity, TriggeredSubscriptionDataEntity> dataRoot = correlatedRoot.join(TriggeredSubscriptionEntity_.data);
		subquery.select(cb.literal(1));
		Predicate[] predicates = data.entrySet().stream()
				.map(d -> cb.and(cb.equal(dataRoot.get(TriggeredSubscriptionDataEntity_.key), d.getKey()), cb.equal(dataRoot.get(TriggeredSubscriptionDataEntity_.value), d.getValue())))
				.toArray(Predicate[]::new);
		subquery.where(cb.or(predicates));
		return cb.exists(subquery);
	}

	private List<Predicate> makeStopActivitiesSubquery(CriteriaQuery<?> query, Root<TriggeredSubscriptionEntity> root, CriteriaBuilder cb, Collection<ActivityCriterion> activities) {
		List<Predicate> outcome = new ArrayList<>();
		outcome.add(cb.isTrue(root.get(TriggeredSubscriptionEntity_.subscription).get(SubscriptionEntity_.hasStopActivities)));
		Subquery<Long> subquery = query.subquery(Long.class);
		subquery.from(SubscriptionEntity.class);
		Join<TriggeredSubscriptionEntity, SubscriptionEntity> subscription = root.join(TriggeredSubscriptionEntity_.subscription);
		SetJoin<SubscriptionEntity, SubscriptionFishingActivity> startActivityJoin = subscription.join(SubscriptionEntity_.stopActivities);
		subquery.select(cb.literal(1L));
		Predicate[] predicates = activities.stream()
				.map(a -> cb.and(cb.equal(startActivityJoin.get(SubscriptionFishingActivity_.type), a.getType()), cb.equal(startActivityJoin.get(SubscriptionFishingActivity_.value), a.getValue())))
				.toArray(Predicate[]::new);
		subquery.where(cb.and(cb.equal(subscription, root.get(TriggeredSubscriptionEntity_.subscription)), cb.or(predicates)));
		outcome.add(cb.exists(subquery));
		return outcome;
	}

}
