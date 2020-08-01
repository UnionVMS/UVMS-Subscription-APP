/*
﻿Developed with the contribution of the European Commission - Directorate General for Maritime Affairs and Fisheries
© European Union, 2015-2016.

This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can
redistribute it and/or modify it under the terms of the GNU General Public License as published by the
Free Software Foundation, either version 3 of the License, or any later version. The IFDM Suite is distributed in
the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details. You should have received a
copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
*/
package eu.europa.ec.fisheries.uvms.subscription.rules.execution;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import java.util.Collections;
import java.util.Date;

import eu.europa.ec.fisheries.uvms.subscription.rules.communication.RulesSender;
import eu.europa.ec.fisheries.uvms.subscription.service.bean.SubscriptionActivityService;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionExecution;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionExecutionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionOutput;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionDataEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.messaging.asset.AssetSender;
import eu.europa.ec.fisheries.wsdl.asset.types.VesselIdentifiersHolder;
import eu.europa.fisheries.uvms.subscription.model.enums.TriggerType;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for the {@link AlarmsSubscriptionExecutor}.
 */
@EnableAutoWeld
@ExtendWith(MockitoExtension.class)
public class AlarmsSubscriptionExecutorTest {

	private static final String CONNECT_ID = "connectid";
	private static final String ASSET_GUID = "asset guid";
	private static final String MOVEMENT_GUID = "asset guid";
	private static final String SUBSCRIPTION_NAME = "subscription name";
	private static final Date EFFECTIVE_FROM = new Date();

	@Produces @Mock
	private RulesSender rulesSender;

	@Produces @Mock
	private AssetSender assetSender;

	@Produces @Mock
	private SubscriptionActivityService subscriptionActivityService;

	@Inject
	private AlarmsSubscriptionExecutor sut;

	@Test
	void testEmptyConstructor() {
		assertDoesNotThrow(() -> new AlarmsSubscriptionExecutor());
	}

	@Test
	void testExecuteForNonAlertOutput() {
		SubscriptionExecutionEntity execution = makeExecution(false);
		sut.execute(execution);
		verifyNoMoreInteractions(rulesSender, assetSender);
	}

	@Test
	void testExecuteForPosition() {
		SubscriptionExecutionEntity execution = makeExecution(true);
		VesselIdentifiersHolder vesselIdentifiers = new VesselIdentifiersHolder();
		vesselIdentifiers.setAssetGuid(ASSET_GUID);
		when(assetSender.findVesselIdentifiers(CONNECT_ID)).thenReturn(vesselIdentifiers);
		execution.getTriggeredSubscription().getData().add(new TriggeredSubscriptionDataEntity(null, "movementGuidIndex_0", MOVEMENT_GUID));
		sut.execute(execution);
		verify(rulesSender).createAlertsAsync(eq(SUBSCRIPTION_NAME), eq(EFFECTIVE_FROM), eq(vesselIdentifiers), eq(Collections.singletonList(MOVEMENT_GUID)));
	}

	private SubscriptionExecutionEntity makeExecution(boolean isAlert) {
		SubscriptionExecutionEntity execution = new SubscriptionExecutionEntity();
		TriggeredSubscriptionEntity triggeredSubscription = new TriggeredSubscriptionEntity();
		execution.setTriggeredSubscription(triggeredSubscription);
		SubscriptionEntity subscription = new SubscriptionEntity();
		SubscriptionOutput output = new SubscriptionOutput();
		output.setAlert(isAlert);
		subscription.setOutput(output);
		subscription.setName(SUBSCRIPTION_NAME);
		triggeredSubscription.setSubscription(subscription);
		triggeredSubscription.getData().add(new TriggeredSubscriptionDataEntity(triggeredSubscription, "connectId", CONNECT_ID));
		triggeredSubscription.setEffectiveFrom(EFFECTIVE_FROM);
		SubscriptionExecution subscriptionExecution = new SubscriptionExecution();
		subscriptionExecution.setTriggerType(TriggerType.INC_POSITION);
		subscription.setExecution(subscriptionExecution);
		return execution;
	}
}
