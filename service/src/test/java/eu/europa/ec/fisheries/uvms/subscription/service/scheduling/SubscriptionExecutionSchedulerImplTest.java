/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.service.scheduling;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Optional;

import eu.europa.ec.fisheries.uvms.subscription.helper.DateTimeServiceTestImpl;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionExecution;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionExecutionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionEntity;
import eu.europa.fisheries.uvms.subscription.model.enums.SubscriptionExecutionStatusType;
import eu.europa.fisheries.uvms.subscription.model.enums.SubscriptionTimeUnit;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for the {@link SubscriptionExecutionSchedulerImpl}.
 */
@EnableAutoWeld
@ExtendWith(MockitoExtension.class)
public class SubscriptionExecutionSchedulerImplTest {

	@Inject
	private SubscriptionExecutionSchedulerImpl sut;

	@Produces @ApplicationScoped
	private DateTimeServiceTestImpl dateTimeService = new DateTimeServiceTestImpl();

	@Test
	void testEmptyConstructor() {
		SubscriptionExecutionSchedulerImpl sut = new SubscriptionExecutionSchedulerImpl();
		assertNotNull(sut);
	}

	@Test
	void testWithInactive() {
		TriggeredSubscriptionEntity triggeredSubscription = new TriggeredSubscriptionEntity();
		triggeredSubscription.setActive(false);
		assertFalse(sut.scheduleNext(triggeredSubscription, null).isPresent());
		SubscriptionEntity subscription = new SubscriptionEntity();
		subscription.setActive(false);
		triggeredSubscription.setSubscription(subscription);
		triggeredSubscription.setActive(true);

		assertFalse(sut.scheduleNext(triggeredSubscription, null).isPresent());
		assertFalse(triggeredSubscription.getActive());
	}

	@Test
	void testImmediateForFirstExecution() {
		TriggeredSubscriptionEntity triggeredSubscription = makeTriggeredSubscriptionEntityForFirstExecution(true);
		Optional<SubscriptionExecutionEntity> result = sut.scheduleNext(triggeredSubscription, null);
		assertSubscriptionExecutionEntity(result, triggeredSubscription, dateTimeService.getNowAsDate());
		assertTrue(triggeredSubscription.getActive());
	}

	@Test
	void testWithFrequencyForFirstExecution() {
		TriggeredSubscriptionEntity triggeredSubscription = makeTriggeredSubscriptionEntityForFirstExecution(false);

		// timeExpression is later than now, so in this day
		triggeredSubscription.getSubscription().getExecution().setTimeExpression("13:00");
		Optional<SubscriptionExecutionEntity> result = sut.scheduleNext(triggeredSubscription, null);

		assertSubscriptionExecutionEntity(result, triggeredSubscription, LocalDateTime.parse("2020-05-05T13:00:00"));
		assertTrue(triggeredSubscription.getActive());

		// timeExpression is earlier than now, so next day
		triggeredSubscription.getSubscription().getExecution().setTimeExpression("11:00");
		result = sut.scheduleNext(triggeredSubscription, null);

		assertSubscriptionExecutionEntity(result, triggeredSubscription, LocalDateTime.parse("2020-05-06T11:00:00"));
		assertTrue(triggeredSubscription.getActive());
	}

	private TriggeredSubscriptionEntity makeTriggeredSubscriptionEntityForFirstExecution(boolean immediate) {
		SubscriptionExecution executionInput = new SubscriptionExecution();
		executionInput.setImmediate(immediate);
		executionInput.setFrequency(0);
		SubscriptionEntity subscription = new SubscriptionEntity();
		subscription.setActive(true);
		subscription.setExecution(executionInput);
		TriggeredSubscriptionEntity triggeredSubscription = new TriggeredSubscriptionEntity();
		triggeredSubscription.setActive(true);
		triggeredSubscription.setSubscription(subscription);
		LocalDateTime now = LocalDateTime.parse("2020-05-05T12:00:00");
		dateTimeService.setNow(now);
		return triggeredSubscription;
	}

	@Test
	void testImmediateForSubsequentExecution() {
		SubscriptionExecution executionInput = new SubscriptionExecution();
		executionInput.setImmediate(true);
		TriggeredSubscriptionEntity triggeredSubscription = setup(executionInput, "2020-05-05T12:00:00");
		SubscriptionExecutionEntity prevExecution = new SubscriptionExecutionEntity();
		prevExecution.setRequestedTime(Date.from(LocalDateTime.parse("2020-05-05T11:00:00").toInstant(ZoneOffset.UTC)));

		Optional<SubscriptionExecutionEntity> result = sut.scheduleNext(triggeredSubscription, prevExecution);

		assertSubscriptionExecutionEntity(result, triggeredSubscription, LocalDateTime.parse("2020-05-08T11:00:00"));
		assertTrue(triggeredSubscription.getActive());
	}

	@Test
	void testNotImmediateForSubsequentExecution() {
		SubscriptionExecution executionInput = new SubscriptionExecution();
		executionInput.setImmediate(false);
		TriggeredSubscriptionEntity triggeredSubscription = setup(executionInput, "2020-05-05T12:00:00");
		SubscriptionExecutionEntity prevExecution = new SubscriptionExecutionEntity();
		prevExecution.setRequestedTime(Date.from(LocalDateTime.parse("2020-05-05T11:00:00").toInstant(ZoneOffset.UTC)));

		Optional<SubscriptionExecutionEntity> result = sut.scheduleNext(triggeredSubscription, prevExecution);

		assertSubscriptionExecutionEntity(result, triggeredSubscription, LocalDateTime.parse("2020-05-08T11:00:00"));
		assertTrue(triggeredSubscription.getActive());
	}

	@Test
	void testZeroFrequencyForSubsequentExecution() {
		SubscriptionExecution executionInput = new SubscriptionExecution();
		executionInput.setImmediate(false);
		TriggeredSubscriptionEntity triggeredSubscription = setup(executionInput, "2020-05-05T12:00:00");
		executionInput.setFrequency(0);
		SubscriptionExecutionEntity prevExecution = new SubscriptionExecutionEntity();
		prevExecution.setRequestedTime(Date.from(LocalDateTime.parse("2020-05-05T11:00:00").toInstant(ZoneOffset.UTC)));

		assertFalse(sut.scheduleNext(triggeredSubscription, prevExecution).isPresent());
		assertFalse(triggeredSubscription.getActive());
	}

	@Test
	void testDeadlineStopCondition() {
		SubscriptionExecution executionInput = new SubscriptionExecution();
		executionInput.setImmediate(true);
		TriggeredSubscriptionEntity triggeredSubscription = setup(executionInput, "2020-05-05T12:00:00");
		SubscriptionExecutionEntity prevExecution = new SubscriptionExecutionEntity();
		prevExecution.setRequestedTime(Date.from(LocalDateTime.parse("2020-05-05T11:00:00").toInstant(ZoneOffset.UTC)));
		triggeredSubscription.setEffectiveFrom(Date.from(LocalDateTime.parse("2020-05-01T11:00:00").toInstant(ZoneOffset.UTC)));
		triggeredSubscription.getSubscription().setDeadline(4);
		triggeredSubscription.getSubscription().setDeadlineUnit(SubscriptionTimeUnit.DAYS);

		Optional<SubscriptionExecutionEntity> result = sut.scheduleNext(triggeredSubscription, prevExecution);

		assertFalse(result.isPresent());
		assertFalse(triggeredSubscription.getActive());
	}

	@Test
	void testSubscriptionExecutesOnceEvenIfDeadlineIsZero() {
		TriggeredSubscriptionEntity triggeredSubscription = makeTriggeredSubscriptionEntityForFirstExecution(true);
		triggeredSubscription.setEffectiveFrom(Date.from(LocalDateTime.parse("2020-05-01T11:00:00").toInstant(ZoneOffset.UTC)));
		triggeredSubscription.getSubscription().setDeadline(0);
		triggeredSubscription.getSubscription().setDeadlineUnit(SubscriptionTimeUnit.DAYS);

		Optional<SubscriptionExecutionEntity> result = sut.scheduleNext(triggeredSubscription, null);

		assertSubscriptionExecutionEntity(result, triggeredSubscription, dateTimeService.getNowAsDate());
		assertTrue(triggeredSubscription.getActive());
	}

	private TriggeredSubscriptionEntity setup(SubscriptionExecution executionInput, String now) {
		TriggeredSubscriptionEntity triggeredSubscription = new TriggeredSubscriptionEntity();
		executionInput.setFrequency(3);
		executionInput.setFrequencyUnit(SubscriptionTimeUnit.DAYS);
		executionInput.setTimeExpression("10:30");
		SubscriptionEntity subscription = new SubscriptionEntity();
		subscription.setActive(true);
		subscription.setExecution(executionInput);
		triggeredSubscription.setActive(true);
		triggeredSubscription.setSubscription(subscription);
		dateTimeService.setNow(LocalDateTime.parse(now));
		return triggeredSubscription;
	}

	private void assertSubscriptionExecutionEntity(Optional<SubscriptionExecutionEntity> result, TriggeredSubscriptionEntity triggeredSubscription, LocalDateTime requestedTime) {
		assertSubscriptionExecutionEntity(result, triggeredSubscription, Date.from(requestedTime.toInstant(ZoneOffset.UTC)));
	}

	private void assertSubscriptionExecutionEntity(Optional<SubscriptionExecutionEntity> result, TriggeredSubscriptionEntity triggeredSubscription, Date requestedTime) {
		assertTrue(result.isPresent());
		assertSame(triggeredSubscription, result.get().getTriggeredSubscription());
		assertEquals(SubscriptionExecutionStatusType.PENDING, result.get().getStatus());
		assertEquals(dateTimeService.getNowAsDate(), result.get().getCreationDate());
		assertEquals(requestedTime, result.get().getRequestedTime());
	}
}
