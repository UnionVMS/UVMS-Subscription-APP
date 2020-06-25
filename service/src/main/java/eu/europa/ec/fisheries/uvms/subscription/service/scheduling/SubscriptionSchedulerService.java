/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.service.scheduling;

import java.util.Date;

/**
 * Service that orchestrates the tasks required to periodically check for activated
 * subscription executions.
 * <p>
 * This component abstracts the real periodic work for activating subscriptions from
 * the mechanism that actually drives this periodicity in the current deployment environment,
 * e.g. an EJB timer service for traditional JEE/EJB deployments.
 */
public interface SubscriptionSchedulerService {
	/**
	 * Activate all pending subscription executions that should activate for the given date.
	 */
	void activatePendingSubscriptionExecutions(Date activationDate);

	/**
	 * Activate scheduled subscriptions that should be triggered for the given date.
	 */
	void activateScheduledSubscriptions(Date activationDate);
}
