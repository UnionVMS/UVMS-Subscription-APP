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

import eu.europa.ec.fisheries.uvms.subscription.service.bean.Command;
import eu.europa.ec.fisheries.uvms.subscription.service.bean.TriggeredSubscriptionService;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.execution.SubscriptionExecutionService;

/**
 * Command to find triggered subscriptions and stop them.
 */
@Vetoed
class StopSubscriptionCommand implements Command {

	private final TriggeredSubscriptionService triggeredSubscriptionService;
	private final SubscriptionExecutionService subscriptionExecutionService;
	private final StopConditionCriteria stopConditionCriteria;

	public StopSubscriptionCommand(TriggeredSubscriptionService triggeredSubscriptionService, SubscriptionExecutionService subscriptionExecutionService, StopConditionCriteria stopConditionCriteria) {
		this.triggeredSubscriptionService = triggeredSubscriptionService;
		this.subscriptionExecutionService = subscriptionExecutionService;
		this.stopConditionCriteria = stopConditionCriteria;
	}

	@Override
	public void execute() {
		triggeredSubscriptionService.findByStopConditionCriteria(stopConditionCriteria)
				.peek(subscriptionExecutionService::stopPendingExecutions)
				.forEach(this::deactivate);
	}

	private void deactivate(TriggeredSubscriptionEntity triggeredSubscription) {
		triggeredSubscription.setActive(false);
	}
}
