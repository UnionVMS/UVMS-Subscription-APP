/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.service.execution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Stream;

import eu.europa.ec.fisheries.uvms.subscription.helper.DateTimeServiceTestImpl;
import eu.europa.ec.fisheries.uvms.subscription.service.dao.SubscriptionExecutionDao;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionExecutionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.scheduling.SubscriptionExecutionScheduler;
import eu.europa.fisheries.uvms.subscription.model.enums.SubscriptionExecutionStatusType;
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

	@Inject
	private SubscriptionExecutionServiceImpl sut;

	@Test
	void testEmptyConstructor() {
		SubscriptionExecutionService sut = new SubscriptionExecutionServiceImpl();
		assertNotNull(sut);
	}

	@Test
	void testSave() {
		SubscriptionExecutionEntity entity = new SubscriptionExecutionEntity();
		sut.save(entity);
		verify(dao).create(entity);
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
		triggeredSubscription.setActive(true);
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
		triggeredSubscription.setActive(true);
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
	void testExecuteNotActiveTriggeredSubscription() {
		SubscriptionEntity subscription = new SubscriptionEntity();
		subscription.setActive(true);
		TriggeredSubscriptionEntity triggeredSubscription = new TriggeredSubscriptionEntity();
		triggeredSubscription.setActive(false);
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
		triggeredSubscription.setActive(true);
		triggeredSubscription.setSubscription(subscription);
		SubscriptionExecutionEntity execution = new SubscriptionExecutionEntity();
		execution.setStatus(SubscriptionExecutionStatusType.QUEUED);
		execution.setTriggeredSubscription(triggeredSubscription);
		when(dao.findById(EXECUTION_ID)).thenReturn(execution);

		sut.execute(EXECUTION_ID);

		assertEquals(SubscriptionExecutionStatusType.QUEUED, execution.getStatus());
		assertNull(execution.getExecutionTime());
	}
}
