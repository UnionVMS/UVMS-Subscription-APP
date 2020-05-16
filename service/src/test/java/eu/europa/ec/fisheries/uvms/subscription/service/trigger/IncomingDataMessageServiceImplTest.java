/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.service.trigger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import eu.europa.ec.fisheries.uvms.subscription.service.bean.TriggeredSubscriptionService;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionExecutionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionDataEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.execution.SubscriptionExecutionService;
import eu.europa.ec.fisheries.uvms.subscription.service.scheduling.SubscriptionExecutionScheduler;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for the {@link IncomingDataMessageServiceImpl}.
 */
@EnableAutoWeld
@ExtendWith(MockitoExtension.class)
public class IncomingDataMessageServiceImplTest {

	private static final String SUBSCRIPTION_SOURCE = "SUBSCRIPTION_SOURCE";
	private static final String REPRESENTATION = "REPRESENTATION";

	@Produces @Mock
	private TriggeredSubscriptionService triggeredSubscriptionService;

	@Produces @Mock
	private SubscriptionExecutionScheduler subscriptionExecutionScheduler;

	@Produces @Mock
	private SubscriptionExecutionService subscriptionExecutionService;

	@Inject
	private IncomingDataMessageServiceImpl sut;

	@Produces
	private final TriggeredSubscriptionCreator triggeredSubscriptionCreator;
	{
		triggeredSubscriptionCreator = mock(TriggeredSubscriptionCreator.class);
		when(triggeredSubscriptionCreator.getEligibleSubscriptionSource()).thenReturn(SUBSCRIPTION_SOURCE);
	}

	@Test
	void testEmptyConstructor() {
		IncomingDataMessageServiceImpl sut = new IncomingDataMessageServiceImpl();
		assertNotNull(sut);
	}

	@Test
	void testHandle() {
		TriggeredSubscriptionEntity dummy1 = new TriggeredSubscriptionEntity();
		TriggeredSubscriptionEntity dummy2 = new TriggeredSubscriptionEntity();
		when(triggeredSubscriptionCreator.createTriggeredSubscriptions(REPRESENTATION)).thenReturn(Arrays.stream(new TriggeredSubscriptionEntity[]{dummy1, dummy2}));
		when(triggeredSubscriptionService.save(any())).thenAnswer(iom -> iom.getArgument(0));
		SubscriptionExecutionEntity exec1 = new SubscriptionExecutionEntity();
		when(subscriptionExecutionScheduler.scheduleNext(same(dummy1))).thenReturn(Optional.of(exec1));
		when(subscriptionExecutionScheduler.scheduleNext(same(dummy2))).thenReturn(Optional.empty());

		sut.handle(SUBSCRIPTION_SOURCE, REPRESENTATION);

		ArgumentCaptor<TriggeredSubscriptionEntity> captor = ArgumentCaptor.forClass(TriggeredSubscriptionEntity.class);
		verify(triggeredSubscriptionService, times(2)).save(captor.capture());
		assertEquals(Arrays.asList(dummy1, dummy2), captor.getAllValues());
		verify(subscriptionExecutionService).save(exec1);
		verifyNoMoreInteractions(subscriptionExecutionService);
	}

	@Test
	void testHandleForDuplicate() {
		TriggeredSubscriptionEntity dummy = new TriggeredSubscriptionEntity();
		when(triggeredSubscriptionCreator.createTriggeredSubscriptions(REPRESENTATION)).thenReturn(Stream.of(dummy));
		when(triggeredSubscriptionService.isDuplicate(eq(dummy), any())).thenReturn(true);
		Set<TriggeredSubscriptionDataEntity> dummyCriteria = new HashSet<>();
		when(triggeredSubscriptionCreator.extractTriggeredSubscriptionDataForDuplicates(dummy)).thenReturn(dummyCriteria);

		sut.handle(SUBSCRIPTION_SOURCE, REPRESENTATION);

		verify(triggeredSubscriptionService, never()).save(any());
		verify(triggeredSubscriptionService).isDuplicate(dummy, dummyCriteria);
		verify(subscriptionExecutionScheduler, never()).scheduleNext(any());
	}

	@Test
	void testHandleThrowsForUnknownSource() {
		assertThrows(IllegalStateException.class, () -> sut.handle("unknown source", REPRESENTATION));
	}
}
