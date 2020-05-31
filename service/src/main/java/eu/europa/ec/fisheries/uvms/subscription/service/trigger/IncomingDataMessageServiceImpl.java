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

import eu.europa.ec.fisheries.uvms.subscription.service.bean.Command;

/**
 * Implementation of the {@link IncomingDataMessageService}.
 */
@ApplicationScoped
@Transactional
class IncomingDataMessageServiceImpl implements IncomingDataMessageService {

	private Map<String,SubscriptionCommandFromMessageExtractor> extractors;

	/**
	 * Injection constructor.
	 *
	 * @param extractors   The services that can translate the representation of an incoming message to subscription-specific commands to execute
	 */
	@Inject
	public IncomingDataMessageServiceImpl(Instance<SubscriptionCommandFromMessageExtractor> extractors) {
		this.extractors = extractors.stream().collect(Collectors.toMap(
				SubscriptionCommandFromMessageExtractor::getEligibleSubscriptionSource,
				Function.identity()
		));
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
		SubscriptionCommandFromMessageExtractor extractor = Optional.ofNullable(extractors.get(subscriptionSource))
				.orElseThrow(() -> new IllegalStateException("unknown subscription source: " + subscriptionSource));
		extractor.extractCommands(representation).forEach(Command::execute);
	}
}
