/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.service.dao;

import static com.ninja_squad.dbsetup.Operations.sequenceOf;
import static eu.europa.fisheries.uvms.subscription.model.enums.SubscriptionExecutionStatusType.PENDING;
import static eu.europa.fisheries.uvms.subscription.model.enums.SubscriptionExecutionStatusType.EXECUTED;
import static eu.europa.fisheries.uvms.subscription.model.enums.SubscriptionExecutionStatusType.QUEUED;
import static eu.europa.fisheries.uvms.subscription.model.enums.TriggeredSubscriptionStatus.ACTIVE;
import static eu.europa.fisheries.uvms.subscription.model.enums.TriggeredSubscriptionStatus.STOPPED;
import static java.time.temporal.ChronoUnit.HOURS;
import static java.time.temporal.ChronoUnit.MINUTES;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.ninja_squad.dbsetup.DbSetup;
import com.ninja_squad.dbsetup.destination.DataSourceDestination;
import com.ninja_squad.dbsetup.operation.Operation;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionExecutionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionDataEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionEntity;
import eu.europa.fisheries.uvms.subscription.model.enums.SubscriptionExecutionStatusType;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link SubscriptionExecutionDaoImpl}.
 */
@EnableAutoWeld
class SubscriptionExecutionDaoImplTest extends BaseSubscriptionInMemoryTest {

	public static final String UNIMPORTANT_KEY = "UNIMPORTANT_KEY";
	public static final String IMPORTANT_KEY = "IMPORTANT_KEY";
	public static final String UNIMPORTANT_VALUE = "UNIMPORTANT_VALUE";
	public static final String IMPORTANT_VALUE = "IMPORTANT_VALUE";
	@Inject
	private SubscriptionExecutionDaoImpl sut;

	@Produces
	EntityManager getEntityManager() {
		return em;
	}

	@BeforeEach
	public void prepare(){
		Operation operation = sequenceOf(
				DELETE_ALL, INSERT_SUBSCRIPTION
		);

		DbSetup dbSetup = new DbSetup(new DataSourceDestination(ds), operation);
		dbSetupTracker.launchIfNecessary(dbSetup);
	}

	@Test
	void testEmptyConstructor() {
		SubscriptionExecutionDaoImpl sut = new SubscriptionExecutionDaoImpl();
		assertNotNull(sut);
	}

	@Test
	void testCreate() {
		em.getTransaction().begin();
		TriggeredSubscriptionEntity triggeredSubscription = setupTriggeredSubscription();
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, 1);
		SubscriptionExecutionEntity execution = makeExecution(triggeredSubscription, calendar.getTime(), PENDING);

		sut.create(execution);

		em.getTransaction().commit();
		em.clear();
		TypedQuery<SubscriptionExecutionEntity> q = em.createQuery("SELECT e FROM SubscriptionExecutionEntity e", SubscriptionExecutionEntity.class);
		List<SubscriptionExecutionEntity> results = q.getResultList();
		assertEquals(1, results.size());
		assertEquals(1L, results.get(0).getTriggeredSubscription().getSubscription().getId());
	}

	@Test
	void testFindById() {
		em.getTransaction().begin();
		TriggeredSubscriptionEntity triggeredSubscription = setupTriggeredSubscription();
		LocalDateTime datetime = LocalDateTime.parse("2020-05-05T12:00:00");
		sut.create(makeExecution(triggeredSubscription, datetime.minus(5L, MINUTES), PENDING));
		sut.create(makeExecution(triggeredSubscription, datetime.minus(5L, HOURS), PENDING));
		Long id3 = sut.create(makeExecution(triggeredSubscription, datetime.minus(5L, MINUTES), EXECUTED)).getId();
		sut.create(makeExecution(triggeredSubscription, datetime.plus(5L, MINUTES), PENDING));
		em.getTransaction().commit();

		SubscriptionExecutionEntity result = sut.findById(id3);

		assertNotNull(result);
		assertEquals(EXECUTED, result.getStatus());
	}

	@Test
	void testFindPendingWithRequestDateBefore() {
		em.getTransaction().begin();
		TriggeredSubscriptionEntity triggeredSubscription = setupTriggeredSubscription();
		LocalDateTime datetime = LocalDateTime.parse("2020-05-05T12:00:00");
		Long id1 = sut.create(makeExecution(triggeredSubscription, datetime.minus(5L, MINUTES), PENDING)).getId();
		Long id2 = sut.create(makeExecution(triggeredSubscription, datetime.minus(5L, HOURS), PENDING)).getId();
		@SuppressWarnings("unused")
		Long id3 = sut.create(makeExecution(triggeredSubscription, datetime.minus(5L, MINUTES), EXECUTED)).getId();
		Long id4 = sut.create(makeExecution(triggeredSubscription, datetime.plus(5L, MINUTES), PENDING)).getId();
		em.getTransaction().commit();

		Set<Long> results = sut.findIdsOfPendingWithRequestDateBefore(Date.from(datetime.toInstant(ZoneOffset.UTC))).collect(Collectors.toSet());
		assertEquals(new HashSet<>(Arrays.asList(id1, id2)), results);

		results = sut.findIdsOfPendingWithRequestDateBefore(Date.from(datetime.plus(5L, MINUTES).toInstant(ZoneOffset.UTC))).collect(Collectors.toSet());
		assertEquals(new HashSet<>(Arrays.asList(id1, id2, id4)), results);
	}

	@Test
	void testFindByTriggeredSubscriptionAndStatus() {
		em.getTransaction().begin();
		TriggeredSubscriptionEntity triggeredSubscription = setupTriggeredSubscription();
		LocalDateTime datetime = LocalDateTime.parse("2020-05-05T12:00:00");
		Long id1 = sut.create(makeExecution(triggeredSubscription, datetime.plus(10L, MINUTES), PENDING)).getId();
		Long id2 = sut.create(makeExecution(triggeredSubscription, datetime, QUEUED)).getId();
		Long id3 = sut.create(makeExecution(triggeredSubscription, datetime.minus(10L, MINUTES), EXECUTED)).getId();
		em.getTransaction().commit();

		Set<SubscriptionExecutionEntity> resultPending = sut.findByTriggeredSubscriptionAndStatus(triggeredSubscription, PENDING).collect(Collectors.toSet());
		assertEquals(1, resultPending.size());
		assertEquals(id1, resultPending.iterator().next().getId());

		Set<SubscriptionExecutionEntity> resultQueued = sut.findByTriggeredSubscriptionAndStatus(triggeredSubscription, QUEUED).collect(Collectors.toSet());
		assertEquals(1, resultQueued.size());
		assertEquals(id2, resultQueued.iterator().next().getId());

		Set<SubscriptionExecutionEntity> resultExecuted = sut.findByTriggeredSubscriptionAndStatus(triggeredSubscription, EXECUTED).collect(Collectors.toSet());
		assertEquals(1, resultExecuted.size());
		assertEquals(id3, resultExecuted.iterator().next().getId());
	}

	@Test
	void testFindPendingBy() {
		em.getTransaction().begin();
		TriggeredSubscriptionEntity triggeredSubscription1 = setupTriggeredSubscription(1L);
		triggeredSubscription1.setStatus(STOPPED);
		triggeredSubscription1.getData().add(new TriggeredSubscriptionDataEntity(triggeredSubscription1, UNIMPORTANT_KEY, UNIMPORTANT_VALUE));
		triggeredSubscription1.getData().add(new TriggeredSubscriptionDataEntity(triggeredSubscription1, IMPORTANT_KEY, IMPORTANT_VALUE));
		@SuppressWarnings("unused")
		Long t1e1id = sut.create(makeExecution(triggeredSubscription1, new Date(), EXECUTED)).getId();
		Long t1e2id = sut.create(makeExecution(triggeredSubscription1, new Date(), PENDING)).getId();
		TriggeredSubscriptionEntity triggeredSubscription2 = setupTriggeredSubscription(1L);
		triggeredSubscription2.setStatus(STOPPED);
		triggeredSubscription2.getData().add(new TriggeredSubscriptionDataEntity(triggeredSubscription2, UNIMPORTANT_KEY, UNIMPORTANT_VALUE));
		triggeredSubscription2.getData().add(new TriggeredSubscriptionDataEntity(triggeredSubscription2, IMPORTANT_KEY, "OTHER_IMPORTANT_VALUE"));
		@SuppressWarnings("unused")
		Long t2e1id = sut.create(makeExecution(triggeredSubscription2, new Date(), EXECUTED)).getId();
		@SuppressWarnings("unused")
		Long t2e2id = sut.create(makeExecution(triggeredSubscription2, new Date(), PENDING)).getId();
		TriggeredSubscriptionEntity triggeredSubscription3 = setupTriggeredSubscription(2L);
		triggeredSubscription3.setStatus(STOPPED);
		triggeredSubscription3.getData().add(new TriggeredSubscriptionDataEntity(triggeredSubscription3, UNIMPORTANT_KEY, UNIMPORTANT_VALUE));
		triggeredSubscription3.getData().add(new TriggeredSubscriptionDataEntity(triggeredSubscription3, IMPORTANT_KEY, IMPORTANT_VALUE));
		@SuppressWarnings("unused")
		Long t3e1id = sut.create(makeExecution(triggeredSubscription3, new Date(), EXECUTED)).getId();
		@SuppressWarnings("unused")
		Long t3e2id = sut.create(makeExecution(triggeredSubscription3, new Date(), PENDING)).getId();
		em.getTransaction().commit();

		List<SubscriptionExecutionEntity> result = sut.findPendingBy(em.find(SubscriptionEntity.class, 1L), Collections.singleton(new TriggeredSubscriptionDataEntity(null, IMPORTANT_KEY, IMPORTANT_VALUE))).collect(Collectors.toList());

		assertEquals(Collections.singleton(t1e2id), result.stream().map(SubscriptionExecutionEntity::getId).collect(Collectors.toSet()));
	}

	private TriggeredSubscriptionEntity setupTriggeredSubscription() {
		return setupTriggeredSubscription(1L);
	}

	private TriggeredSubscriptionEntity setupTriggeredSubscription(long subscriptionId) {
		SubscriptionEntity subscription = em.find(SubscriptionEntity.class, subscriptionId);
		TriggeredSubscriptionEntity triggeredSubscription = new TriggeredSubscriptionEntity();
		triggeredSubscription.setStatus(ACTIVE);
		triggeredSubscription.setCreationDate(new Date());
		triggeredSubscription.setSource("test");
		triggeredSubscription.setSubscription(subscription);
		em.persist(triggeredSubscription);
		return triggeredSubscription;
	}

	private SubscriptionExecutionEntity makeExecution(TriggeredSubscriptionEntity triggeredSubscription, LocalDateTime requestedTime, SubscriptionExecutionStatusType status) {
		return makeExecution(triggeredSubscription, Date.from(requestedTime.toInstant(ZoneOffset.UTC)), status);
	}

	private SubscriptionExecutionEntity makeExecution(TriggeredSubscriptionEntity triggeredSubscription, Date requestedTime, SubscriptionExecutionStatusType status) {
		SubscriptionExecutionEntity execution = new SubscriptionExecutionEntity();
		execution.setCreationDate(new Date());
		execution.setRequestedTime(requestedTime);
		execution.setStatus(status);
		execution.setTriggeredSubscription(triggeredSubscription);
		return execution;
	}
}
