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

import static eu.europa.ec.fisheries.uvms.commons.message.impl.JAXBUtils.marshallJaxBObjectToString;
import static eu.europa.ec.fisheries.uvms.commons.message.impl.JAXBUtils.unMarshallMessage;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.xml.bind.JAXBException;
import java.util.Optional;

import eu.europa.ec.fisheries.uvms.activity.model.schemas.ForwardReportToSubscriptionRequest;
import eu.europa.ec.fisheries.uvms.commons.message.api.MessageConstants;
import eu.europa.ec.fisheries.uvms.commons.message.api.MessageException;
import eu.europa.ec.fisheries.uvms.commons.message.context.FluxEnvelopeHolder;
import eu.europa.ec.fisheries.uvms.commons.message.context.FluxEnvelopePropagatedData;
import eu.europa.ec.fisheries.uvms.commons.message.context.PropagateFluxEnvelopeData;
import eu.europa.ec.fisheries.uvms.subscription.service.bean.SubscriptionService;
import eu.europa.ec.fisheries.uvms.subscription.service.trigger.SenderInformation;
import eu.europa.ec.fisheries.wsdl.subscription.module.SubscriptionPermissionResponse;
import lombok.extern.slf4j.Slf4j;

@MessageDriven(mappedName = "jms/queue/UVMSSubscriptionPermissionEvent", activationConfig = {
        @ActivationConfigProperty(propertyName = MessageConstants.MESSAGING_TYPE_STR, propertyValue = MessageConstants.CONNECTION_TYPE),
        @ActivationConfigProperty(propertyName = MessageConstants.DESTINATION_TYPE_STR, propertyValue = MessageConstants.DESTINATION_TYPE_QUEUE),
        @ActivationConfigProperty(propertyName = MessageConstants.DESTINATION_STR, propertyValue = "UVMSSubscriptionPermissionEvent")
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
            ForwardReportToSubscriptionRequest forwardReportToSubscriptionRequest = unMarshallMessage(textMessage.getText(), ForwardReportToSubscriptionRequest.class);
            log.info("[START] Received ACCESS_PERMISSION_REQUEST..");

            Optional<FluxEnvelopePropagatedData> fluxEnvelopeData = Optional.ofNullable(fluxEnvelopeHolder.get());
            SenderInformation senderInformation = SenderInformation.fromProperties(
                    fluxEnvelopeData.map(FluxEnvelopePropagatedData::getDataflow).orElse(null),
                    fluxEnvelopeData.map(FluxEnvelopePropagatedData::getSenderOrReceiver).orElse(null));

            SubscriptionPermissionResponse dataRequestAllowed = subscriptionService.hasActiveSubscriptions(forwardReportToSubscriptionRequest, senderInformation);
            log.info("[INFO] Checked permissions... Going to send back : " + dataRequestAllowed.getSubscriptionCheck());
            String messageToSend = marshallJaxBObjectToString(dataRequestAllowed);
            subscriptionProducer.sendMessageWithSpecificIds(messageToSend, jmsReplyTo, null, jmsMessageID, jmsCorrelationID);
            log.info("[END] Answer sent to queue : " + jmsReplyTo);
        } catch (MessageException | JAXBException | JMSException e) {
            try {
                subscriptionProducer.sendMessageWithSpecificIds(e.getLocalizedMessage(), jmsReplyTo, null, jmsMessageID, jmsCorrelationID);
            } catch (MessageException e1) {
                log.error("Unrecoverable error while in an JMSException | JAXBException!", e1);
            }
        }
    }
}
