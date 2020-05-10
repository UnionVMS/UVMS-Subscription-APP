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
 * Subscription facility that accepts incoming data change messages from JMS and takes care of any protocol-related tasks.
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
			log.error("error while handling subscriptions execution message", e);
			throw new RuntimeException(e);
		}
	}
}
