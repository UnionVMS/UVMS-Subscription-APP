/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.service.trigger;

import java.util.Date;
import java.util.stream.Stream;

import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;

/**
 * Service to trigger a scheduled subscription.
 */
public interface ScheduledSubscriptionTriggeringService {

    /**
     * Find the subscription entities that must be triggered according to the given date.
     *
     * @param activationDate The activation date
     * @return A stream of scheduled subscription ids that must be triggered according to the given date
     */
    Stream<Long> findScheduledSubscriptionIdsForTriggering(Date activationDate);

    /**
     * Enqueue the given subscription entity for triggering, in a separate transaction.
     *
     * @param subscriptionId The subscription id to be fetched and enqueued
     */
    void enqueueForTriggeringInNewTransaction(Long subscriptionId);
}
