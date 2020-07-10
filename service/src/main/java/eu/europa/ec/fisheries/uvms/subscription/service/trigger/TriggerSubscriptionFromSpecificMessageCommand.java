/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.service.trigger;

import javax.enterprise.inject.Vetoed;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;

import eu.europa.ec.fisheries.uvms.subscription.service.bean.Command;
import eu.europa.ec.fisheries.uvms.subscription.service.bean.TriggeredSubscriptionService;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionDataEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.execution.SubscriptionExecutionService;
import eu.europa.ec.fisheries.uvms.subscription.service.scheduling.SubscriptionExecutionScheduler;

/**
 * Command to trigger a subscription from a message that must enrich already triggered subscriptions.
 * <p>
 * Example:
 * Assume a subscription that forwards reports is triggered for a message, to be executed later this day.
 * It will store the ids of the reports to forward in the triggered subscription data.
 * If another report message arrives that triggers the same subscription, it will be dropped from the
 * {@code notAlreadyTriggered} logic.
 * <p>
 * The previous outcome is wrong however, because the system should forward the reports of the second message as well.
 * This class will retrieve already activated triggerings and, if any, will add the report ids to the data,
 * while still preventing the new triggering from being saved to the DB.
 */
@Vetoed
class TriggerSubscriptionFromSpecificMessageCommand implements Command {

	private final Function<TriggeredSubscriptionEntity, Set<TriggeredSubscriptionDataEntity>> extractTriggeredSubscriptionDataForDuplicates;
	private final TriggeredSubscriptionService triggeredSubscriptionService;
	private final SubscriptionExecutionScheduler subscriptionExecutionScheduler;
	private final SubscriptionExecutionService subscriptionExecutionService;
	private final TriggeredSubscriptionEntity triggeredSubscription;
	private final BiPredicate<TriggeredSubscriptionEntity, TriggeredSubscriptionEntity> processTriggering;

	/**
	 * Construct an instance using both the required collaborators and the instance-specific data.
	 *
	 * @param extractTriggeredSubscriptionDataForDuplicates Function to extract the duplicate/already active triggering criteria
	 * @param triggeredSubscriptionService                  The triggered subscription service
	 * @param subscriptionExecutionScheduler                The execution scheduling service
	 * @param subscriptionExecutionService                  The execution service
	 * @param triggeredSubscription                         The candidate triggered subscription to be saved in the DB and scheduled for execution,
	 *                                                      if no other active triggering of its subscription exists in the DB
	 * @param processTriggering                             The function to add message-specific data to already activated triggerings;
	 *                                                      it has the chance to decide if that triggering should not be considered as a
	 *                                                      duplicate by returing {@code false}; first argument is the candidate {@code TriggeredSubscriptionEntity}
	 *                                                      and the second argument is the existing
	 */
	public TriggerSubscriptionFromSpecificMessageCommand(
			Function<TriggeredSubscriptionEntity, Set<TriggeredSubscriptionDataEntity>> extractTriggeredSubscriptionDataForDuplicates,
			TriggeredSubscriptionService triggeredSubscriptionService,
			SubscriptionExecutionScheduler subscriptionExecutionScheduler,
			SubscriptionExecutionService subscriptionExecutionService,
			TriggeredSubscriptionEntity triggeredSubscription,
			BiPredicate<TriggeredSubscriptionEntity, TriggeredSubscriptionEntity> processTriggering
	) {
		this.extractTriggeredSubscriptionDataForDuplicates = extractTriggeredSubscriptionDataForDuplicates;
		this.triggeredSubscriptionService = triggeredSubscriptionService;
		this.subscriptionExecutionScheduler = subscriptionExecutionScheduler;
		this.subscriptionExecutionService = subscriptionExecutionService;
		this.triggeredSubscription = triggeredSubscription;
		this.processTriggering = processTriggering;
	}

	@Override
	public void execute() {
		Optional.of(triggeredSubscription)
				.filter(this::notAlreadyTriggered)
				.map(triggeredSubscriptionService::save)
				.flatMap(subscriptionExecutionScheduler::scheduleNext)
				.ifPresent(subscriptionExecutionService::activate);
	}

	private boolean notAlreadyTriggered(TriggeredSubscriptionEntity triggeredSubscriptionCandidate) {
		return Optional.of(triggeredSubscriptionCandidate)
				.map(extractTriggeredSubscriptionDataForDuplicates)
				.map(criteria -> triggeredSubscriptionService.findAlreadyActivated(triggeredSubscription, criteria))
				.map(alreadyActivated -> alreadyActivated.filter(existingTriggering -> processTriggering.test(triggeredSubscriptionCandidate, existingTriggering)).count())
				.map(count -> count == 0)
				.orElse(false);
	}
}
