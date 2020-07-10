/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.service.trigger;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Stream;

import eu.europa.ec.fisheries.uvms.subscription.service.bean.TriggeredSubscriptionService;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionExecutionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionDataEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.execution.SubscriptionExecutionService;
import eu.europa.ec.fisheries.uvms.subscription.service.scheduling.SubscriptionExecutionScheduler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for the {@link TriggerSubscriptionFromSpecificMessageCommand}.
 */
@ExtendWith(MockitoExtension.class)
public class TriggerSubscriptionFromSpecificMessageCommandTest {

	private static final Set<TriggeredSubscriptionDataEntity> DATA_FOR_DUPLICATES = Collections.unmodifiableSet(new HashSet<>());
	private static final Function<TriggeredSubscriptionEntity, Set<TriggeredSubscriptionDataEntity>> EXTRACT_TRIGGERED_SUBSCRIPTION_DATA_FOR_DUPLICATES = x -> DATA_FOR_DUPLICATES;

	@Mock
	private TriggeredSubscriptionService triggeredSubscriptionService;

	@Mock
	private SubscriptionExecutionScheduler subscriptionExecutionScheduler;

	@Mock
	private SubscriptionExecutionService subscriptionExecutionService;

	@Test
	void testExecute() {
		TriggeredSubscriptionEntity triggeredSubscription = new TriggeredSubscriptionEntity();
		SubscriptionExecutionEntity subscriptionExecution = new SubscriptionExecutionEntity();
		BiPredicate<TriggeredSubscriptionEntity, TriggeredSubscriptionEntity> processTriggering = mockTheTriggeringProcessor();
		TriggerSubscriptionFromSpecificMessageCommand sut = setup(triggeredSubscription, subscriptionExecution, processTriggering);

		sut.execute();

		verify(triggeredSubscriptionService).findAlreadyActivated(triggeredSubscription, DATA_FOR_DUPLICATES);
		verify(triggeredSubscriptionService).save(triggeredSubscription);
		verify(subscriptionExecutionScheduler).scheduleNext(triggeredSubscription);
		verify(subscriptionExecutionService).activate(subscriptionExecution);
		verify(processTriggering, never()).test(any(),any());
	}

	@Test
	void testExecuteForDuplicate() {
		TriggeredSubscriptionEntity triggeredSubscription = new TriggeredSubscriptionEntity();
		SubscriptionExecutionEntity subscriptionExecution = new SubscriptionExecutionEntity();
		BiPredicate<TriggeredSubscriptionEntity, TriggeredSubscriptionEntity> processTriggering = mockTheTriggeringProcessor();
		when(processTriggering.test(any(),any())).thenReturn(true);
		// all TriggeredSubscriptionEntities returned by findAlreadyActivated must be processed, sending 2 to verify!
		TriggerSubscriptionFromSpecificMessageCommand sut = setup(triggeredSubscription, subscriptionExecution, processTriggering, new TriggeredSubscriptionEntity(), new TriggeredSubscriptionEntity());

		sut.execute();

		verify(triggeredSubscriptionService).findAlreadyActivated(triggeredSubscription, DATA_FOR_DUPLICATES);
		verifyNoMoreInteractions(triggeredSubscriptionService);
		verifyNoInteractions(subscriptionExecutionScheduler);
		verifyNoInteractions(subscriptionExecutionService);
		verify(processTriggering, times(2)).test(eq(triggeredSubscription), any());
	}

	@Test
	void testExecuteForExistingThatAreDroppedByThePredicate() {
		TriggeredSubscriptionEntity triggeredSubscription = new TriggeredSubscriptionEntity();
		SubscriptionExecutionEntity subscriptionExecution = new SubscriptionExecutionEntity();
		BiPredicate<TriggeredSubscriptionEntity, TriggeredSubscriptionEntity> processTriggering = mockTheTriggeringProcessor();
		when(processTriggering.test(any(),any())).thenReturn(false);
		// all TriggeredSubscriptionEntities returned by findAlreadyActivated must be processed, sending 2 to verify!
		TriggerSubscriptionFromSpecificMessageCommand sut = setup(triggeredSubscription, subscriptionExecution, processTriggering, new TriggeredSubscriptionEntity(), new TriggeredSubscriptionEntity());

		sut.execute();

		verify(triggeredSubscriptionService).findAlreadyActivated(triggeredSubscription, DATA_FOR_DUPLICATES);
		verify(triggeredSubscriptionService).save(triggeredSubscription);
		verify(subscriptionExecutionScheduler).scheduleNext(triggeredSubscription);
		verify(subscriptionExecutionService).activate(subscriptionExecution);
		verify(processTriggering, times(2)).test(eq(triggeredSubscription), any());
	}

	private TriggerSubscriptionFromSpecificMessageCommand setup(TriggeredSubscriptionEntity triggeredSubscription, SubscriptionExecutionEntity subscriptionExecution, BiPredicate<TriggeredSubscriptionEntity, TriggeredSubscriptionEntity> processTriggering, TriggeredSubscriptionEntity... alreadyActivated) {
		when(triggeredSubscriptionService.findAlreadyActivated(any(),any())).thenReturn(Optional.ofNullable(alreadyActivated).filter(a -> a.length > 0).map(Stream::of).orElseGet(Stream::empty));
		lenient().when(triggeredSubscriptionService.save(any())).thenAnswer(iom -> iom.getArgument(0));
		lenient().when(subscriptionExecutionScheduler.scheduleNext(any())).thenReturn(Optional.of(subscriptionExecution));
		return new TriggerSubscriptionFromSpecificMessageCommand(
				EXTRACT_TRIGGERED_SUBSCRIPTION_DATA_FOR_DUPLICATES,
				triggeredSubscriptionService,
				subscriptionExecutionScheduler,
				subscriptionExecutionService,
				triggeredSubscription,
				processTriggering
		);
	}

	private BiPredicate<TriggeredSubscriptionEntity, TriggeredSubscriptionEntity> mockTheTriggeringProcessor() {
		@SuppressWarnings("unchecked")
		BiPredicate<TriggeredSubscriptionEntity, TriggeredSubscriptionEntity> processTriggering = mock(BiPredicate.class);
		return processTriggering;
	}
}
