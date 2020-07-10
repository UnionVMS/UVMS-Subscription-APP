/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2020.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.service.execution;

import static eu.europa.fisheries.uvms.subscription.model.enums.SubscriptionExecutionStatusType.PENDING;
import static eu.europa.fisheries.uvms.subscription.model.enums.SubscriptionExecutionStatusType.STOPPED;
import static eu.europa.fisheries.uvms.subscription.model.enums.TriggeredSubscriptionStatus.ACTIVE;
import static eu.europa.fisheries.uvms.subscription.model.enums.TriggeredSubscriptionStatus.INACTIVE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Stream;

import eu.europa.ec.fisheries.uvms.subscription.helper.DateTimeServiceTestImpl;
import eu.europa.ec.fisheries.uvms.subscription.service.dao.SubscriptionExecutionDao;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionExecutionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionDataEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.scheduling.SubscriptionExecutionScheduler;
import eu.europa.ec.fisheries.uvms.subscription.service.trigger.SubscriptionCommandFromMessageExtractor;
import eu.europa.fisheries.uvms.subscription.model.enums.SubscriptionExecutionStatusType;
import eu.europa.fisheries.uvms.subscription.model.enums.TriggeredSubscriptionStatus;
import eu.europa.fisheries.uvms.subscription.model.exceptions.EntityDoesNotExistException;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for the {@link SubscriptionExecutionServiceImpl}.
 */
@EnableAutoWeld
@ExtendWith(MockitoExtension.class)
public class SubscriptionExecutionServiceImplTest {

	private static final Long EXECUTION_ID = 111L;
	private static final String SUBSCRIPTION_SOURCE = "SUBSCRIPTION_SOURCE";
	private static final String DATA_KEY = "DATA_KEY";
	private static final String DATA_VALUE = "DATA_VALUE";

	@Produces @Mock
	private SubscriptionExecutionDao dao;

	@Produces @Mock
	private SubscriptionExecutionSender subscriptionExecutionSender;

	@Produces @Mock
	private SubscriptionExecutor mockSubscriptionExecutor;

	@Produces @Mock
	private SubscriptionExecutionScheduler subscriptionExecutionScheduler;

	@Produces
	private DateTimeServiceTestImpl dateTimeService = new DateTimeServiceTestImpl();

	@Produces
	private final SubscriptionCommandFromMessageExtractor subscriptionCommandFromMessageExtractor;
	{
		subscriptionCommandFromMessageExtractor = mock(SubscriptionCommandFromMessageExtractor.class);
		when(subscriptionCommandFromMessageExtractor.getEligibleSubscriptionSource()).thenReturn(SUBSCRIPTION_SOURCE);
		when(subscriptionCommandFromMessageExtractor.getDataForDuplicatesExtractor()).thenReturn(x -> Collections.singleton(new TriggeredSubscriptionDataEntity(null, DATA_KEY, DATA_VALUE)));
	}

	@Inject
	private SubscriptionExecutionServiceImpl sut;

	@Test
	void testEmptyConstructor() {
		SubscriptionExecutionService sut = new SubscriptionExecutionServiceImpl();
		assertNotNull(sut);
	}

	@Test
	void testActivate() {
		SubscriptionEntity subscription = new SubscriptionEntity();
		subscription.setActive(true);
		TriggeredSubscriptionEntity triggeredSubscription = new TriggeredSubscriptionEntity();
		triggeredSubscription.setSource(SUBSCRIPTION_SOURCE);
		SubscriptionExecutionEntity entity = new SubscriptionExecutionEntity();
		entity.setTriggeredSubscription(triggeredSubscription);
		TriggeredSubscriptionEntity triggeringOfPending = new TriggeredSubscriptionEntity();
		triggeringOfPending.setStatus(TriggeredSubscriptionStatus.STOPPED);
		SubscriptionExecutionEntity pendingExecution = new SubscriptionExecutionEntity();
		pendingExecution.setTriggeredSubscription(triggeringOfPending);
		pendingExecution.setStatus(PENDING);
		when(dao.findPendingBy(any(), any())).thenReturn(Stream.of(pendingExecution));

		sut.activate(entity);

		verify(dao).create(entity);
		assertEquals(STOPPED, pendingExecution.getStatus());
		assertEquals(INACTIVE, triggeringOfPending.getStatus());
		verify(subscriptionCommandFromMessageExtractor).preserveDataFromSupersededTriggering(triggeringOfPending, triggeredSubscription);
	}

	@Test
	void testFindPendingSubscriptionExecutions() {
		Date date = new Date();
		Stream<Long> resultFromDao = Stream.of(111L);
		when(dao.findIdsOfPendingWithRequestDateBefore(date)).thenReturn(resultFromDao);
		Stream<Long> result = sut.findPendingSubscriptionExecutionIds(date);
		assertSame(resultFromDao, result);
	}

	@Test
	void testEnqueueForExecutionInNewTransaction() {
		Long entityId = 111L;
		SubscriptionExecutionEntity entity = new SubscriptionExecutionEntity();
		entity.setId(entityId);
		LocalDateTime now = LocalDateTime.parse("2020-05-05T12:00:00");
		dateTimeService.setNow(now);
		when(dao.findById(entityId)).thenReturn(entity);
		sut.enqueueForExecutionInNewTransaction(entityId);
		verify(subscriptionExecutionSender).enqueue(entity);
		assertEquals(SubscriptionExecutionStatusType.QUEUED, entity.getStatus());
		assertEquals(Date.from(now.toInstant(ZoneOffset.UTC)), entity.getQueuedTime());
	}

	@Test
	void testEnqueueForExecutionInNewTransactionThrowsWhenNotFound() {
		Long entityId = 111L;
		LocalDateTime now = LocalDateTime.parse("2020-05-05T12:00:00");
		dateTimeService.setNow(now);
		when(dao.findById(entityId)).thenReturn(null);
		assertThrows(EntityDoesNotExistException.class, () -> sut.enqueueForExecutionInNewTransaction(entityId));
	}

	@Test
	void testExecute() {
		SubscriptionEntity subscription = new SubscriptionEntity();
		subscription.setActive(true);
		TriggeredSubscriptionEntity triggeredSubscription = new TriggeredSubscriptionEntity();
		triggeredSubscription.setStatus(ACTIVE);
		triggeredSubscription.setSubscription(subscription);
		SubscriptionExecutionEntity execution = new SubscriptionExecutionEntity();
		execution.setStatus(SubscriptionExecutionStatusType.QUEUED);
		execution.setTriggeredSubscription(triggeredSubscription);
		when(dao.findById(EXECUTION_ID)).thenReturn(execution);
		LocalDateTime now = LocalDateTime.parse("2020-05-05T12:00:00");
		dateTimeService.setNow(now);
		SubscriptionExecutionEntity nextExecution = new SubscriptionExecutionEntity();
		when(subscriptionExecutionScheduler.scheduleNext(triggeredSubscription, execution)).thenReturn(Optional.of(nextExecution));

		sut.execute(EXECUTION_ID);

		verify(mockSubscriptionExecutor).execute(execution);
		assertEquals(SubscriptionExecutionStatusType.EXECUTED, execution.getStatus());
		assertEquals(Date.from(now.toInstant(ZoneOffset.UTC)), execution.getExecutionTime());
		verify(dao).create(nextExecution);
	}

	@Test
	void testExecuteStoppedTriggeredSubscription() {
		SubscriptionEntity subscription = new SubscriptionEntity();
		subscription.setActive(true);
		TriggeredSubscriptionEntity triggeredSubscription = new TriggeredSubscriptionEntity();
		triggeredSubscription.setStatus(TriggeredSubscriptionStatus.STOPPED);
		triggeredSubscription.setSubscription(subscription);
		SubscriptionExecutionEntity execution = new SubscriptionExecutionEntity();
		execution.setStatus(SubscriptionExecutionStatusType.QUEUED);
		execution.setTriggeredSubscription(triggeredSubscription);
		when(dao.findById(EXECUTION_ID)).thenReturn(execution);
		LocalDateTime now = LocalDateTime.parse("2020-05-05T12:00:00");
		dateTimeService.setNow(now);
		SubscriptionExecutionEntity nextExecution = new SubscriptionExecutionEntity();
		when(subscriptionExecutionScheduler.scheduleNext(triggeredSubscription, execution)).thenReturn(Optional.of(nextExecution));

		sut.execute(EXECUTION_ID);

		verify(mockSubscriptionExecutor).execute(execution);
		assertEquals(SubscriptionExecutionStatusType.EXECUTED, execution.getStatus());
		assertEquals(Date.from(now.toInstant(ZoneOffset.UTC)), execution.getExecutionTime());
		verify(dao).create(nextExecution);
	}

	@Test
	void testExecuteNotQueued() {
		SubscriptionEntity subscription = new SubscriptionEntity();
		subscription.setActive(true);
		TriggeredSubscriptionEntity triggeredSubscription = new TriggeredSubscriptionEntity();
		triggeredSubscription.setStatus(ACTIVE);
		triggeredSubscription.setSubscription(subscription);
		SubscriptionExecutionEntity execution = new SubscriptionExecutionEntity();
		execution.setStatus(SubscriptionExecutionStatusType.PENDING);
		execution.setTriggeredSubscription(triggeredSubscription);
		when(dao.findById(EXECUTION_ID)).thenReturn(execution);

		sut.execute(EXECUTION_ID);

		assertEquals(SubscriptionExecutionStatusType.PENDING, execution.getStatus());
		assertNull(execution.getExecutionTime());
	}

	@Test
	void testExecuteInactiveTriggeredSubscription() {
		SubscriptionEntity subscription = new SubscriptionEntity();
		subscription.setActive(true);
		TriggeredSubscriptionEntity triggeredSubscription = new TriggeredSubscriptionEntity();
		triggeredSubscription.setStatus(INACTIVE);
		triggeredSubscription.setSubscription(subscription);
		SubscriptionExecutionEntity execution = new SubscriptionExecutionEntity();
		execution.setStatus(SubscriptionExecutionStatusType.QUEUED);
		execution.setTriggeredSubscription(triggeredSubscription);
		when(dao.findById(EXECUTION_ID)).thenReturn(execution);

		sut.execute(EXECUTION_ID);

		assertEquals(SubscriptionExecutionStatusType.QUEUED, execution.getStatus());
		assertNull(execution.getExecutionTime());
	}

	@Test
	void testExecuteNotActiveSubscription() {
		SubscriptionEntity subscription = new SubscriptionEntity();
		subscription.setActive(false);
		TriggeredSubscriptionEntity triggeredSubscription = new TriggeredSubscriptionEntity();
		triggeredSubscription.setStatus(ACTIVE);
		triggeredSubscription.setSubscription(subscription);
		SubscriptionExecutionEntity execution = new SubscriptionExecutionEntity();
		execution.setStatus(SubscriptionExecutionStatusType.QUEUED);
		execution.setTriggeredSubscription(triggeredSubscription);
		when(dao.findById(EXECUTION_ID)).thenReturn(execution);

		sut.execute(EXECUTION_ID);

		assertEquals(SubscriptionExecutionStatusType.QUEUED, execution.getStatus());
		assertNull(execution.getExecutionTime());
	}

	@Test
	void testStopPendingExecutions() {
		TriggeredSubscriptionEntity triggeredSubscription = new TriggeredSubscriptionEntity();
		SubscriptionExecutionEntity execution = new SubscriptionExecutionEntity();
		execution.setStatus(PENDING);
		when(dao.findByTriggeredSubscriptionAndStatus(triggeredSubscription,PENDING)).thenReturn(Stream.of(execution));
		sut.stopPendingExecutions(triggeredSubscription);
		assertEquals(STOPPED, execution.getStatus());
	}
}
