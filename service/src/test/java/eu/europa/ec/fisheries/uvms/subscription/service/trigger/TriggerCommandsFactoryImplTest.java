/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.service.trigger;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import java.util.Collections;
import java.util.Set;
import java.util.function.Function;

import eu.europa.ec.fisheries.uvms.subscription.service.bean.Command;
import eu.europa.ec.fisheries.uvms.subscription.service.bean.TriggeredSubscriptionService;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionDataEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.execution.SubscriptionExecutionService;
import eu.europa.ec.fisheries.uvms.subscription.service.scheduling.SubscriptionExecutionScheduler;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for the {@link TriggerCommandsFactoryImpl}.
 */
@EnableAutoWeld
@ExtendWith(MockitoExtension.class)
public class TriggerCommandsFactoryImplTest {

	@Produces @Mock
	private TriggeredSubscriptionService triggeredSubscriptionService;

	@Produces @Mock
	private SubscriptionExecutionScheduler subscriptionExecutionScheduler;

	@Produces @Mock
	private SubscriptionExecutionService subscriptionExecutionService;

	@Inject
	private TriggerCommandsFactoryImpl sut;

	@Test
	void testEmptyConstructor() {
		Object x = new TriggerCommandsFactoryImpl();
		assertNotNull(x);
	}

	@Test
	void testCreateTriggerSubscriptionCommand() {
		TriggeredSubscriptionEntity triggeredSubscription = new TriggeredSubscriptionEntity();
		Function<TriggeredSubscriptionEntity, Set<TriggeredSubscriptionDataEntity>> extractTriggeredSubscriptionDataForDuplicates = t -> Collections.emptySet();
		Command result = sut.createTriggerSubscriptionCommand(triggeredSubscription, extractTriggeredSubscriptionDataForDuplicates);
		assertTrue(result instanceof TriggerSubscriptionCommand);
		result.execute();
	}

	@Test
	void testCreateStopSubscriptionCommand() {
		StopConditionCriteria stopConditionCriteria = new StopConditionCriteria();
		Command result = sut.createStopSubscriptionCommand(stopConditionCriteria);
		assertTrue(result instanceof StopSubscriptionCommand);
		result.execute();
	}
}
