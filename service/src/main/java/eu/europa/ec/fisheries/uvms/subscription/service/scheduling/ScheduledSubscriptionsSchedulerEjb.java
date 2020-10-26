/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.service.scheduling;

import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import eu.europa.ec.fisheries.uvms.subscription.service.util.DateTimeService;
import lombok.extern.slf4j.Slf4j;

/**
 * This EJB is responsible for activating the process that searches for pending
 * subscription executions and queuing them.
 * <p>
 * Check out <a href="http://www.mastertheboss.com/jboss-server/wildfly-8/creating-clustered-ejb-3-timers">this</a>
 * or <a href-"https://github.com/wildfly/quickstart/tree/master/ha-singleton-deployment">this</a> resource
 * (<a href="https://github.com/wildfly/quickstart/tree/master/messaging-clustering-singleton">this</a> might
 * be useful too because according to EJB3.2, section 13.2 "Timers can be created for [...] message-driven beans")
 * if you need to deploy multiple instances of this service.
 *
 * Apparently the trick like described <a href="https://www.baeldung.com/scheduling-in-java-enterprise-edition">here</a>
 * are not needed: WildFly 15 will not trigger the timer again if the previous triggering has not finished.
 * Check with the following sample code:<br/>
 * <pre>{@code
 *     @Schedule(hour = "*", minute = "*", second = "* /2", info = "activateScheduledSubscriptions", persistent = true)
 *     public void activateScheduledSubscriptions() {
 *       log.info("****************** WORK STARTING *********************");
 *       try {
 *         Thread.sleep(20000);
 *       } catch (InterruptedException e) {}
 *       log.info("****************** WORK FINISHED *********************");
 *     }
 * }</pre>
 */
//@Singleton
//@Startup
@Slf4j
public class ScheduledSubscriptionsSchedulerEjb {

//	@Inject
	private DateTimeService dateTimeService;

//	@Inject
	private SubscriptionSchedulerService subscriptionSchedulerService;

//	@Schedule(hour = "*", minute = "*/1", info = "activateScheduledSubscriptions")
	public void activatePendingSubscriptionExecutions() {
		log.debug("Scheduled Subscription SchedulerEjb started");
		subscriptionSchedulerService.activateScheduledSubscriptions(dateTimeService.getNowAsDate());
		log.debug("Scheduled Subscription SchedulerEjb finished");
	}
}
