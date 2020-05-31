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
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

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
 * Tests for the {@link TriggerSubscriptionCommand}.
 */
@ExtendWith(MockitoExtension.class)
public class TriggerSubscriptionCommandTest {

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
		TriggerSubscriptionCommand sut = setup(triggeredSubscription, subscriptionExecution, false);

		sut.execute();

		verify(triggeredSubscriptionService).isDuplicate(triggeredSubscription, DATA_FOR_DUPLICATES);
		verify(triggeredSubscriptionService).save(triggeredSubscription);
		verify(subscriptionExecutionScheduler).scheduleNext(triggeredSubscription);
		verify(subscriptionExecutionService).save(subscriptionExecution);
	}

	@Test
	void testExecuteForDuplicate() {
		TriggeredSubscriptionEntity triggeredSubscription = new TriggeredSubscriptionEntity();
		SubscriptionExecutionEntity subscriptionExecution = new SubscriptionExecutionEntity();
		TriggerSubscriptionCommand sut = setup(triggeredSubscription, subscriptionExecution, true);

		sut.execute();

		verify(triggeredSubscriptionService).isDuplicate(triggeredSubscription, DATA_FOR_DUPLICATES);
		verifyNoMoreInteractions(triggeredSubscriptionService);
		verifyNoInteractions(subscriptionExecutionScheduler);
		verifyNoInteractions(subscriptionExecutionService);
	}

	private TriggerSubscriptionCommand setup(TriggeredSubscriptionEntity triggeredSubscription, SubscriptionExecutionEntity subscriptionExecution, boolean duplicate) {
		when(triggeredSubscriptionService.isDuplicate(any(),any())).thenReturn(duplicate);
		lenient().when(triggeredSubscriptionService.save(any())).thenAnswer(iom -> iom.getArgument(0));
		lenient().when(subscriptionExecutionScheduler.scheduleNext(any())).thenReturn(Optional.of(subscriptionExecution));
		return new TriggerSubscriptionCommand(
				EXTRACT_TRIGGERED_SUBSCRIPTION_DATA_FOR_DUPLICATES,
				triggeredSubscriptionService,
				subscriptionExecutionScheduler,
				subscriptionExecutionService,
				triggeredSubscription
		);
	}
}
