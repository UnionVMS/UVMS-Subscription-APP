package eu.europa.ec.fisheries.uvms.subscription.service.messaging;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.xml.bind.JAXBException;

import eu.europa.ec.fisheries.schema.movement.module.v1.CreateMovementBatchResponse;
import eu.europa.ec.fisheries.uvms.commons.message.api.MessageConstants;
import eu.europa.ec.fisheries.uvms.commons.message.api.MessageException;
import eu.europa.ec.fisheries.uvms.commons.message.impl.JAXBUtils;
import lombok.extern.slf4j.Slf4j;

@MessageDriven(mappedName = MessageConstants.QUEUE_SUBSCRIPTION_DATA, activationConfig = {
		@ActivationConfigProperty(propertyName = MessageConstants.MESSAGING_TYPE_STR, propertyValue = MessageConstants.CONNECTION_TYPE),
		@ActivationConfigProperty(propertyName = MessageConstants.DESTINATION_TYPE_STR, propertyValue = MessageConstants.DESTINATION_TYPE_QUEUE),
		@ActivationConfigProperty(propertyName = MessageConstants.DESTINATION_STR, propertyValue = MessageConstants.QUEUE_NAME_SUBSCRIPTION_DATA)
})
@Slf4j
public class SubscriptionDataMessageConsumerBean implements MessageListener {

	@EJB
	private SubscriptionProducerBean subscriptionProducer;

	@Override
	public void onMessage(Message message) {
		String jmsCorrelationID = null;
		String jmsMessageID = null;
		Destination jmsReplyTo = null;
		try {
			jmsReplyTo = message.getJMSReplyTo();
			jmsCorrelationID = message.getJMSCorrelationID();
			jmsMessageID = message.getJMSMessageID();
			TextMessage textMessage = (TextMessage) message;
			String subscriptionName = textMessage.getStringProperty(MessageConstants.JMS_SUBSCRIPTION_SOURCE_PROPERTY);
			switch (subscriptionName) {
				case "movement":
					CreateMovementBatchResponse response = JAXBUtils.unMarshallMessage(textMessage.getText(), CreateMovementBatchResponse.class);
log.info(textMessage.getText());
					break;
				default:
					log.error("unknown " + MessageConstants.JMS_SUBSCRIPTION_SOURCE_PROPERTY + ": " + subscriptionName);
			}
		} catch (JMSException e) {
			log.error("error while handling subscriptions data message", e);
			throw new RuntimeException(e);
		} catch (JAXBException e) {
			try {
				subscriptionProducer.sendMessageWithSpecificIds(e.getLocalizedMessage(), jmsReplyTo,null, jmsMessageID, jmsCorrelationID);
			} catch (MessageException e1) {
				log.error("Unrecoverable error while in an JMSException | JAXBException!", e1);
			}
		}
	}
}
