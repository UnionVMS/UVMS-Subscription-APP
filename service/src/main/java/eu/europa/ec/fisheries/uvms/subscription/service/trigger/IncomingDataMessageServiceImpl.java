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

/**
 * Implementation of the {@link IncomingDataMessageService}.
 */
@ApplicationScoped
@Transactional
class IncomingDataMessageServiceImpl implements IncomingDataMessageService {

	private final TriggeredSubscriptionService triggeredSubscriptionService;
	private final Map<String, TriggeredSubscriptionCreator> subscriptionCreators;

	/**
	 * Injection constructor.
	 *
	 * @param triggeredSubscriptionService The service
	 */
	@Inject
	public IncomingDataMessageServiceImpl(TriggeredSubscriptionService triggeredSubscriptionService, Instance<TriggeredSubscriptionCreator> triggeredSubscriptionCreators) {
		this.triggeredSubscriptionService = triggeredSubscriptionService;
		subscriptionCreators = triggeredSubscriptionCreators.stream().collect(Collectors.toMap(
				TriggeredSubscriptionCreator::getEligibleSubscriptionSource,
				Function.identity()
		));
	}

	@Override
	public void handle(String subscriptionSource, String representation) {
		TriggeredSubscriptionCreator triggeredSubscriptionCreator = Optional.ofNullable(subscriptionCreators.get(subscriptionSource))
				.orElseThrow(() -> new IllegalStateException("unknown subscription source: " + subscriptionSource));
		triggeredSubscriptionCreator.createTriggeredSubscriptions(representation)
				.forEach(triggeredSubscriptionService::save);
	}
}
