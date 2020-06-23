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
import org.apache.commons.lang3.StringUtils;

import eu.europa.ec.fisheries.uvms.subscription.service.bean.Command;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.SubscriptionSearchCriteria.SenderCriterion;
import eu.europa.ec.fisheries.uvms.subscription.service.messaging.usm.UsmSender;

/**
 * Implementation of the {@link IncomingDataMessageService}.
 */
@ApplicationScoped
@Transactional
class IncomingDataMessageServiceImpl implements IncomingDataMessageService {

	/**
	 * Pass this as criterion when there is no sender info (e.g. when handling manual or scheduled subscriptions)
	 * so that subscriptions with senders do not match.
	 */
	private static final SenderCriterion BAD_SENDER = new SenderCriterion(-1L, -1L, -1L);

	private UsmSender usmSender;
	private Map<String,SubscriptionCommandFromMessageExtractor> extractors;

	/**
	 * Injection constructor.
	 *
	 * @param usmSender    The facility to communicate with the User module
	 * @param extractors   The services that can translate the representation of an incoming message to subscription-specific commands to execute
	 */
	@Inject
	public IncomingDataMessageServiceImpl(UsmSender usmSender, Instance<SubscriptionCommandFromMessageExtractor> extractors) {
		this.usmSender = usmSender;
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
	public void handle(String subscriptionSource, String representation, SenderInformation senderInformation) {
		SubscriptionCommandFromMessageExtractor extractor = Optional.ofNullable(extractors.get(subscriptionSource))
				.orElseThrow(() -> new IllegalStateException("unknown subscription source: " + subscriptionSource));
		SenderCriterion senderCriterion = extractSenderCriterion(senderInformation);
		extractor.extractCommands(representation, senderCriterion).forEach(Command::execute);
	}

	private SenderCriterion extractSenderCriterion(SenderInformation senderInformation) {
		return Optional.ofNullable(senderInformation)
				.filter(si -> StringUtils.isNotBlank(si.getDataflow()) && StringUtils.isNotBlank(si.getSenderOrReceiver()))
				.map(si -> usmSender.findOrganizationByDataFlowAndEndpointName(si.getDataflow(), si.getSenderOrReceiver()))
				.map(sender -> new SenderCriterion(sender.getOrganisationId(), sender.getEndpointId(), sender.getChannelId()))
				.orElse(BAD_SENDER);
	}
}
