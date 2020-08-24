/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.service.scheduling;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Date;

import eu.europa.ec.fisheries.uvms.subscription.service.execution.SubscriptionExecutionService;
import eu.europa.ec.fisheries.uvms.subscription.service.trigger.ScheduledSubscriptionTriggeringService;
import eu.europa.fisheries.uvms.subscription.model.exceptions.ScheduledSubscriptionProcessingException;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of the {@link SubscriptionSchedulerService}.
 */
@ApplicationScoped
@Slf4j
class SubscriptionSchedulerServiceImpl implements SubscriptionSchedulerService {

    private final SubscriptionExecutionService subscriptionExecutionService;
    private final ScheduledSubscriptionTriggeringService scheduledSubscriptionTriggeringService;

    /**
     * Injection constructor.
     *
     * @param subscriptionExecutionService           The subscription execution service
     * @param scheduledSubscriptionTriggeringService The scheduled subscription triggering service
     */
    @Inject
    public SubscriptionSchedulerServiceImpl(SubscriptionExecutionService subscriptionExecutionService, ScheduledSubscriptionTriggeringService scheduledSubscriptionTriggeringService) {
        this.subscriptionExecutionService = subscriptionExecutionService;
        this.scheduledSubscriptionTriggeringService = scheduledSubscriptionTriggeringService;
    }

    @Override
    public void activatePendingSubscriptionExecutions(Date activationDate) {
        subscriptionExecutionService.findPendingSubscriptionExecutionIds(activationDate)
                .forEach(subscriptionExecutionService::enqueueForExecutionInNewTransaction);
    }

    @Override
    public void activateScheduledSubscriptions(Date activationDate) {
        try {
            scheduledSubscriptionTriggeringService.findScheduledSubscriptionIdsForTriggering(activationDate)
                    .forEach(scheduledSubscriptionTriggeringService::enqueueForTriggeringInNewTransaction);
        } catch (ScheduledSubscriptionProcessingException e) {
            log.error("Error processing scheduled subscription batch: " , e);
        }
    }
}
