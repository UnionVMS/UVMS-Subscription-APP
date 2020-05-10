/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.service.messaging;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import eu.europa.ec.fisheries.uvms.commons.message.api.MessageConstants;
import eu.europa.ec.fisheries.uvms.commons.message.api.MessageException;
import eu.europa.ec.fisheries.uvms.subscription.service.trigger.IncomingDataMessageService;
import eu.europa.fisheries.uvms.subscription.model.exceptions.ApplicationException;
import lombok.extern.slf4j.Slf4j;

/**
 * Subscription facility that accepts incoming data change messages from JMS and takes care of any protocol-related tasks.
 */
@MessageDriven(mappedName = MessageConstants.QUEUE_SUBSCRIPTION_DATA, activationConfig = {
		@ActivationConfigProperty(propertyName = MessageConstants.MESSAGING_TYPE_STR, propertyValue = MessageConstants.CONNECTION_TYPE),
		@ActivationConfigProperty(propertyName = MessageConstants.DESTINATION_TYPE_STR, propertyValue = MessageConstants.DESTINATION_TYPE_QUEUE),
		@ActivationConfigProperty(propertyName = MessageConstants.DESTINATION_STR, propertyValue = MessageConstants.QUEUE_NAME_SUBSCRIPTION_DATA)
})
@Slf4j
public class SubscriptionDataMessageConsumerBean implements MessageListener {

	@EJB
	private SubscriptionProducerBean subscriptionProducer;

	@Inject
	private IncomingDataMessageService incomingDataMessageService;

	@Override
	public void onMessage(Message message) {
		String jmsCorrelationID = null;
		String jmsMessageID = null;
		Destination jmsReplyTo = null;
		try {
			jmsReplyTo = message.getJMSReplyTo();
			jmsCorrelationID = message.getJMSCorrelationID();
			jmsMessageID = message.getJMSMessageID();
			String subscriptionSource = message.getStringProperty(MessageConstants.JMS_SUBSCRIPTION_SOURCE_PROPERTY);
			incomingDataMessageService.handle(subscriptionSource, ((TextMessage) message).getText());
		} catch (JMSException e) {
			log.error("error while handling subscriptions data message", e);
			throw new RuntimeException(e);
		} catch (ApplicationException e) {
			try {
				subscriptionProducer.sendMessageWithSpecificIds(e.getLocalizedMessage(), jmsReplyTo,null, jmsMessageID, jmsCorrelationID);
			} catch (MessageException e1) {
				log.error("Unrecoverable error while in an JMSException | JAXBException!", e1);
			}
		}
	}
}