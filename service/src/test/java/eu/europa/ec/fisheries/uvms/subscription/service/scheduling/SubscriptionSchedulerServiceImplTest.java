/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.service.scheduling;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.stream.Stream;

import eu.europa.ec.fisheries.uvms.subscription.service.execution.SubscriptionExecutionService;
import eu.europa.ec.fisheries.uvms.subscription.service.trigger.ScheduledSubscriptionTriggeringService;
import eu.europa.fisheries.uvms.subscription.model.exceptions.ScheduledSubscriptionProcessingException;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for the {@link SubscriptionSchedulerServiceImpl}.
 */
@EnableAutoWeld
@ExtendWith(MockitoExtension.class)
public class SubscriptionSchedulerServiceImplTest {

	private static final Date ACTIVATION_DATE = Date.from(Instant.parse("2020-06-25T10:42:00Z"));

	@Produces @Mock
	private ScheduledSubscriptionTriggeringService scheduledSubscriptionTriggeringService;

	@Produces @Mock
	private SubscriptionExecutionService subscriptionExecutionService;

	@Inject
	private SubscriptionSchedulerServiceImpl sut;

	@Test
	void testActivatePendingSubscriptionExecutions() {
		Date date = new Date();
		Long executionId = 111L;
		when(subscriptionExecutionService.findPendingSubscriptionExecutionIds(date)).thenReturn(Stream.of(executionId));
		sut.activatePendingSubscriptionExecutions(date);
		verify(subscriptionExecutionService).enqueueForExecutionInNewTransaction(executionId);
		verifyNoMoreInteractions(subscriptionExecutionService);
	}


	@Test
	void testActivateScheduledSubscriptionsWithNoQualifyingSubscriptions() {
		sut.activateScheduledSubscriptions(ACTIVATION_DATE);

		ArgumentCaptor<Date> activationDateCaptor = ArgumentCaptor.forClass(Date.class);
		verify(scheduledSubscriptionTriggeringService, times(1)).findScheduledSubscriptionIdsForTriggering(activationDateCaptor.capture());
		assertEquals(ACTIVATION_DATE, activationDateCaptor.getValue());

		ArgumentCaptor<Long> subscriptionEntityIdCaptor = ArgumentCaptor.forClass(Long.class);
		verify(scheduledSubscriptionTriggeringService, times(0)).enqueueForTriggeringInNewTransaction(subscriptionEntityIdCaptor.capture());
		assertEquals(ACTIVATION_DATE, activationDateCaptor.getValue());
	}

	@Test
	void testActivateScheduledSubscriptionsWithQualifyingSubscriptions() {
		when(scheduledSubscriptionTriggeringService.findScheduledSubscriptionIdsForTriggering(ACTIVATION_DATE)).thenReturn(makeSubscriptionEntityIdsStream(1L, 2L));

		sut.activateScheduledSubscriptions(ACTIVATION_DATE);

		ArgumentCaptor<Date> activationDateCaptor = ArgumentCaptor.forClass(Date.class);
		verify(scheduledSubscriptionTriggeringService, times(1)).findScheduledSubscriptionIdsForTriggering(activationDateCaptor.capture());
		assertEquals(ACTIVATION_DATE, activationDateCaptor.getValue());

		ArgumentCaptor<Long> subscriptionIdCaptor = ArgumentCaptor.forClass(Long.class);
		verify(scheduledSubscriptionTriggeringService, times(2)).enqueueForTriggeringInNewTransaction(subscriptionIdCaptor.capture());
		assertEquals(1L, subscriptionIdCaptor.getAllValues().get(0));
		assertEquals(2L, subscriptionIdCaptor.getAllValues().get(1));
	}

	@Test
	void testActivateScheduledSubscriptionsWithQualifyingSubscriptionsAndError() {
		when(scheduledSubscriptionTriggeringService.findScheduledSubscriptionIdsForTriggering(ACTIVATION_DATE)).thenReturn(makeSubscriptionEntityIdsStream(1L, 2L));
		doThrow(ScheduledSubscriptionProcessingException.class).when(scheduledSubscriptionTriggeringService).enqueueForTriggeringInNewTransaction(1L);
		assertDoesNotThrow(() -> sut.activateScheduledSubscriptions(ACTIVATION_DATE));

		ArgumentCaptor<Date> activationDateCaptor = ArgumentCaptor.forClass(Date.class);
		verify(scheduledSubscriptionTriggeringService, times(1)).findScheduledSubscriptionIdsForTriggering(activationDateCaptor.capture());
		assertEquals(ACTIVATION_DATE, activationDateCaptor.getValue());

		ArgumentCaptor<Long> subscriptionIdCaptor = ArgumentCaptor.forClass(Long.class);
		verify(scheduledSubscriptionTriggeringService, times(1)).enqueueForTriggeringInNewTransaction(subscriptionIdCaptor.capture());
		assertEquals(1L, subscriptionIdCaptor.getAllValues().get(0));

	}

	private Stream<Long> makeSubscriptionEntityIdsStream(Long... subscriptionIds) {
		return Arrays.stream(subscriptionIds);
	}
}
