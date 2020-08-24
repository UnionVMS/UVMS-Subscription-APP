/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.service.messaging;

import static eu.europa.ec.fisheries.uvms.subscription.service.messaging.SubscriptionMessagingConstants.EXECUTION_QUEUE_MAPPED_NAME;
import static eu.europa.ec.fisheries.uvms.subscription.service.messaging.SubscriptionMessagingConstants.EXECUTION_QUEUE_NAME;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import eu.europa.ec.fisheries.uvms.commons.message.api.MessageConstants;
import eu.europa.ec.fisheries.uvms.subscription.service.execution.SubscriptionExecutionService;
import lombok.extern.slf4j.Slf4j;

/**
 * Subscription facility that listens to subscription execution commands from JMS
 * and takes care of any protocol-related tasks before forwarding to the
 * {@link SubscriptionExecutionService} that takes care of the actual execution logic.
 */
@MessageDriven(mappedName = EXECUTION_QUEUE_MAPPED_NAME, activationConfig = {
		@ActivationConfigProperty(propertyName = MessageConstants.MESSAGING_TYPE_STR, propertyValue = MessageConstants.CONNECTION_TYPE),
		@ActivationConfigProperty(propertyName = MessageConstants.DESTINATION_TYPE_STR, propertyValue = MessageConstants.DESTINATION_TYPE_QUEUE),
		@ActivationConfigProperty(propertyName = MessageConstants.DESTINATION_STR, propertyValue = EXECUTION_QUEUE_NAME)
})
@Slf4j
public class SubscriptionExecutionMessageConsumerBean implements MessageListener {

	@Inject
	private SubscriptionExecutionService subscriptionExecutionService;

	@Override
	public void onMessage(Message message) {
		try {
			String executionIdAsString = ((TextMessage) message).getText();
			Long executionId = Long.valueOf(executionIdAsString);
			subscriptionExecutionService.execute(executionId);
		} catch (JMSException e) {
			throw new RuntimeException("error while handling subscriptions execution message",e);
		}
	}
}
