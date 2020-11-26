/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */

/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
*/

package eu.europa.ec.fisheries.uvms.subscription.service.messaging;

import eu.europa.ec.fisheries.schema.movement.source.v1.MovementToSubscriptionRequest;
import eu.europa.ec.fisheries.uvms.activity.model.exception.ActivityModelMarshallException;
import eu.europa.ec.fisheries.uvms.activity.model.mapper.JAXBMarshaller;
import eu.europa.ec.fisheries.uvms.activity.model.schemas.ForwardQueryToSubscriptionRequest;
import eu.europa.ec.fisheries.uvms.activity.model.schemas.ForwardReportToSubscriptionRequest;
import eu.europa.ec.fisheries.uvms.activity.model.schemas.MapToSubscriptionRequest;
import eu.europa.ec.fisheries.uvms.commons.message.api.MessageConstants;
import eu.europa.ec.fisheries.uvms.commons.message.api.MessageException;
import eu.europa.ec.fisheries.uvms.commons.message.context.FluxEnvelopeHolder;
import eu.europa.ec.fisheries.uvms.commons.message.context.FluxEnvelopePropagatedData;
import eu.europa.ec.fisheries.uvms.commons.message.context.PropagateFluxEnvelopeData;
import eu.europa.ec.fisheries.uvms.commons.message.impl.JAXBUtils;
import eu.europa.ec.fisheries.uvms.subscription.service.bean.SubscriptionService;
import eu.europa.ec.fisheries.uvms.subscription.service.trigger.SenderInformation;
import eu.europa.ec.fisheries.wsdl.subscription.module.SubscriptionPermissionResponse;
import lombok.extern.slf4j.Slf4j;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.*;
import javax.xml.bind.JAXBException;
import java.util.Optional;

import static eu.europa.ec.fisheries.uvms.commons.message.impl.JAXBUtils.marshallJaxBObjectToString;
import static eu.europa.ec.fisheries.uvms.commons.message.impl.JAXBUtils.unMarshallMessage;

@MessageDriven(mappedName = MessageConstants.QUEUE_SUBSCRIPTION_PERMISSION, activationConfig = {
        @ActivationConfigProperty(propertyName = MessageConstants.MESSAGING_TYPE_STR, propertyValue = MessageConstants.CONNECTION_TYPE),
        @ActivationConfigProperty(propertyName = MessageConstants.DESTINATION_TYPE_STR, propertyValue = MessageConstants.DESTINATION_TYPE_QUEUE),
        @ActivationConfigProperty(propertyName = MessageConstants.DESTINATION_STR, propertyValue = MessageConstants.QUEUE_NAME_SUBSCRIPTION_PERMISSION)
})
@Slf4j
public class SubscriptionPermissionMessageConsumerBean implements MessageListener {

    @EJB
    private SubscriptionProducerBean subscriptionProducer;

    @Inject
    private SubscriptionService subscriptionService;

    @Inject
    private FluxEnvelopeHolder fluxEnvelopeHolder;

    @Override
    @PropagateFluxEnvelopeData
    public void onMessage(Message message) {
        TextMessage textMessage;
        String jmsCorrelationID = null;
        String jmsMessageID = null;
        Destination jmsReplyTo = null;
        try {
            log.info("[INFO] Received message in Subscription...");
            jmsReplyTo = message.getJMSReplyTo();
            textMessage = (TextMessage) message;
            jmsCorrelationID = textMessage.getJMSCorrelationID();
            jmsMessageID = textMessage.getJMSMessageID();
            String moduleSender = textMessage.getStringProperty(MessageConstants.JMS_SUBSCRIPTION_SOURCE_PROPERTY);
            switch (moduleSender) {
                case "activity":
                    reviewSubscriptionsForActivity(textMessage, jmsReplyTo, jmsMessageID, jmsCorrelationID);
                    break;
                case "movement":
                    reviewSubscriptionsForMovement(textMessage, jmsReplyTo, jmsMessageID, jmsCorrelationID);
                    break;
                default:
                    throw new MessageException("Message Sender Module not provided in SubscriptionPermissionMessageConsumerBean");
            }

        } catch (MessageException | JAXBException | JMSException | ActivityModelMarshallException e) {
            try {
                subscriptionProducer.sendMessageWithSpecificIds(e.getLocalizedMessage(), jmsReplyTo, null, jmsMessageID, jmsCorrelationID);
            } catch (MessageException e1) {
                log.error("Unrecoverable error while in an JMSException | JAXBException!", e1);
            }
        }
    }

    private void reviewSubscriptionsForFAReportMessage(MapToSubscriptionRequest mapToSubscriptionRequest, String jmsCorrelationID,
                                                       String jmsMessageID, Destination jmsReplyTo) throws JAXBException, MessageException {
        SenderInformation senderInformation = buildSenderInformation();
        ForwardReportToSubscriptionRequest forwardReportToSubscriptionRequest = JAXBUtils.unMarshallMessage(mapToSubscriptionRequest.getRequest(), ForwardReportToSubscriptionRequest.class);
        SubscriptionPermissionResponse dataRequestAllowed = subscriptionService.hasActiveSubscriptions(forwardReportToSubscriptionRequest, senderInformation);
        log.info("[INFO] Checked permissions... Going to send back : " + dataRequestAllowed.getSubscriptionCheck());
        String messageToSend = marshallJaxBObjectToString(dataRequestAllowed);
        subscriptionProducer.sendMessageWithSpecificIds(messageToSend, jmsReplyTo, null, jmsMessageID, jmsCorrelationID);
        log.info("[END] Answer sent to queue : " + jmsReplyTo);
    }

    private void reviewSubscriptionsForFAQueryMessage(MapToSubscriptionRequest mapToSubscriptionRequest, String jmsCorrelationID,
                                                      String jmsMessageID, Destination jmsReplyTo) throws JAXBException, MessageException, ActivityModelMarshallException {
        SenderInformation senderInformation = buildSenderInformation();
        ForwardQueryToSubscriptionRequest forwardQueryToSubscriptionRequest = JAXBMarshaller.unmarshallTextMessage(mapToSubscriptionRequest.getRequest(), ForwardQueryToSubscriptionRequest.class);
        SubscriptionPermissionResponse dataRequestAllowed = subscriptionService.hasActiveSubscriptions(forwardQueryToSubscriptionRequest, senderInformation);
        log.info("[INFO] Checked permissions... Going to send back : " + dataRequestAllowed.getSubscriptionCheck());
        String messageToSend = marshallJaxBObjectToString(dataRequestAllowed);
        subscriptionProducer.sendMessageWithSpecificIds(messageToSend, jmsReplyTo, null, jmsMessageID, jmsCorrelationID);
        log.info("[END] Answer sent to queue : " + jmsReplyTo);
    }

    private void reviewSubscriptionsForActivity(TextMessage textMessage, Destination jmsReplyTo, String jmsMessageID, String jmsCorrelationID)
            throws MessageException, JAXBException, ActivityModelMarshallException, JMSException {
        MapToSubscriptionRequest mapToSubscriptionRequest = unMarshallMessage(textMessage.getText(), MapToSubscriptionRequest.class);
        log.info("[START] Received ACCESS_PERMISSION_REQUEST..");
        switch (mapToSubscriptionRequest.getMessageType()) {
            case FLUX_FA_REPORT_MESSAGE:
                reviewSubscriptionsForFAReportMessage(mapToSubscriptionRequest, jmsCorrelationID, jmsMessageID, jmsReplyTo);
                break;
            case FLUX_FA_QUERY_MESSAGE:
                reviewSubscriptionsForFAQueryMessage(mapToSubscriptionRequest, jmsCorrelationID, jmsMessageID, jmsReplyTo);
                break;
            default:
                throw new MessageException("Message Type not provided in SubscriptionPermissionMessageConsumerBean");
        }
    }

    private void reviewSubscriptionsForMovement(TextMessage textMessage, Destination jmsReplyTo, String jmsMessageID, String jmsCorrelationID)
            throws JMSException, JAXBException, MessageException {
        SenderInformation senderInformation = buildSenderInformation();
        MovementToSubscriptionRequest movementToSubscriptionRequest = unMarshallMessage(textMessage.getText(), MovementToSubscriptionRequest.class);
        SubscriptionPermissionResponse dataRequestAllowed = subscriptionService.hasActiveSubscriptions(movementToSubscriptionRequest, senderInformation);
        log.info("[INFO] Checked permissions... Going to send back : " + dataRequestAllowed.getSubscriptionCheck());
        String messageToSend = marshallJaxBObjectToString(dataRequestAllowed);
        subscriptionProducer.sendMessageWithSpecificIds(messageToSend, jmsReplyTo, null, jmsMessageID, jmsCorrelationID);
        log.info("[END] Answer sent to queue : " + jmsReplyTo);
    }

    private SenderInformation buildSenderInformation() {
        Optional<FluxEnvelopePropagatedData> fluxEnvelopeData = Optional.ofNullable(fluxEnvelopeHolder.get());
        SenderInformation senderInformation = SenderInformation.fromProperties(
                fluxEnvelopeData.map(FluxEnvelopePropagatedData::getDataflow).orElse(null),
                fluxEnvelopeData.map(FluxEnvelopePropagatedData::getSenderOrReceiver).orElse(null));
        return senderInformation;
    }

}
