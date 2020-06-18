/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.service.trigger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Set;
import java.util.function.Function;

import eu.europa.ec.fisheries.uvms.subscription.service.bean.Command;
import eu.europa.ec.fisheries.uvms.subscription.service.bean.TriggeredSubscriptionService;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionDataEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.execution.SubscriptionExecutionService;
import eu.europa.ec.fisheries.uvms.subscription.service.messaging.AssetPageRetrievalMessage;
import eu.europa.ec.fisheries.uvms.subscription.service.messaging.SubscriptionManualProducerBean;
import eu.europa.ec.fisheries.uvms.subscription.service.messaging.SubscriptionSender;
import eu.europa.ec.fisheries.uvms.subscription.service.scheduling.SubscriptionExecutionScheduler;

/**
 * Implementation of the {@link TriggerCommandsFactory}.
 */
@ApplicationScoped
class TriggerCommandsFactoryImpl implements TriggerCommandsFactory {

	private TriggeredSubscriptionService triggeredSubscriptionService;
	private SubscriptionExecutionScheduler subscriptionExecutionScheduler;
	private SubscriptionExecutionService subscriptionExecutionService;
	private SubscriptionSender subscriptionSender;

	/**
	 * Constructor for injection.
	 *
	 * @param triggeredSubscriptionService   The triggered subscription service
	 * @param subscriptionExecutionScheduler The subscription execution scheduler
	 * @param subscriptionExecutionService   The subscription execution service
	 * @param subscriptionSender The facility used to re-enter messages in the subscription queue
	 */
	@Inject
	public TriggerCommandsFactoryImpl(TriggeredSubscriptionService triggeredSubscriptionService, SubscriptionExecutionScheduler subscriptionExecutionScheduler, SubscriptionExecutionService subscriptionExecutionService, SubscriptionSender subscriptionSender) {
		this.triggeredSubscriptionService = triggeredSubscriptionService;
		this.subscriptionExecutionScheduler = subscriptionExecutionScheduler;
		this.subscriptionExecutionService = subscriptionExecutionService;
		this.subscriptionSender = subscriptionSender;
	}

	/**
	 * Default constructor for frameworks.
	 */
	@SuppressWarnings("unused")
	TriggerCommandsFactoryImpl() {
		// NOOP
	}

	@Override
	public Command createTriggerSubscriptionCommand(TriggeredSubscriptionEntity triggeredSubscription, Function<TriggeredSubscriptionEntity, Set<TriggeredSubscriptionDataEntity>> extractTriggeredSubscriptionDataForDuplicates) {
		return new TriggerSubscriptionCommand(extractTriggeredSubscriptionDataForDuplicates, triggeredSubscriptionService, subscriptionExecutionScheduler, subscriptionExecutionService, triggeredSubscription);
	}

	@Override
	public Command createStopSubscriptionCommand(StopConditionCriteria stopConditionCriteria) {
		return new StopSubscriptionCommand(triggeredSubscriptionService, subscriptionExecutionService, stopConditionCriteria);
	}

	@Override
	public Command createAssetPageRetrievalCommand(AssetPageRetrievalMessage message) {
		return new AssetPageRetrievalCommand(message, subscriptionSender);
	}
}
