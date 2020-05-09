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
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import eu.europa.ec.fisheries.uvms.subscription.service.bean.TriggeredSubscriptionService;
import eu.europa.ec.fisheries.uvms.subscription.service.execution.SubscriptionExecutionService;
import eu.europa.ec.fisheries.uvms.subscription.service.scheduling.SubscriptionExecutionScheduler;

/**
 * Implementation of the {@link IncomingDataMessageService}.
 */
@ApplicationScoped
@Transactional
class IncomingDataMessageServiceImpl implements IncomingDataMessageService {

	private TriggeredSubscriptionService triggeredSubscriptionService;
	private Map<String, TriggeredSubscriptionCreator> subscriptionCreators;
	private SubscriptionExecutionScheduler subscriptionExecutionScheduler;
	private SubscriptionExecutionService subscriptionExecutionService;

	/**
	 * Injection constructor.
	 *
	 * @param triggeredSubscriptionService   The service
	 * @param triggeredSubscriptionCreators  All the subscription creators
	 * @param subscriptionExecutionScheduler The scheduler
	 * @param subscriptionExecutionService   The subscription execution service
	 */
	@Inject
	public IncomingDataMessageServiceImpl(TriggeredSubscriptionService triggeredSubscriptionService, Instance<TriggeredSubscriptionCreator> triggeredSubscriptionCreators, SubscriptionExecutionScheduler subscriptionExecutionScheduler, SubscriptionExecutionService subscriptionExecutionService) {
		this.triggeredSubscriptionService = triggeredSubscriptionService;
		subscriptionCreators = triggeredSubscriptionCreators.stream().collect(Collectors.toMap(
				TriggeredSubscriptionCreator::getEligibleSubscriptionSource,
				Function.identity()
		));
		this.subscriptionExecutionScheduler = subscriptionExecutionScheduler;
		this.subscriptionExecutionService = subscriptionExecutionService;
	}

	/**
	 * Constructor for frameworks.
	 */
	@SuppressWarnings("unused")
	IncomingDataMessageServiceImpl() {
		// NOOP
	}

	@Override
	public void handle(String subscriptionSource, String representation) {
		TriggeredSubscriptionCreator triggeredSubscriptionCreator = Optional.ofNullable(subscriptionCreators.get(subscriptionSource))
				.orElseThrow(() -> new IllegalStateException("unknown subscription source: " + subscriptionSource));
		triggeredSubscriptionCreator.createTriggeredSubscriptions(representation)
				.map(triggeredSubscriptionService::save)
				.map(subscriptionExecutionScheduler::scheduleNext)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.forEach(subscriptionExecutionService::save);
	}
}
