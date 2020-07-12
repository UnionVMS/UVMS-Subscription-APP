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

import eu.europa.ec.fisheries.uvms.commons.message.api.MessageConstants;
import eu.europa.ec.fisheries.uvms.commons.message.api.MessageException;
import eu.europa.ec.fisheries.uvms.subscription.service.bean.SubscriptionService;
import eu.europa.ec.fisheries.wsdl.subscription.module.ActivityReportGenerationResultsRequest;
import eu.europa.ec.fisheries.wsdl.subscription.module.SubscriptionBaseRequest;
import eu.europa.ec.fisheries.wsdl.subscription.module.SubscriptionDataRequest;
import eu.europa.ec.fisheries.wsdl.subscription.module.SubscriptionPermissionResponse;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.xml.bind.JAXBException;
import lombok.extern.slf4j.Slf4j;

@MessageDriven(mappedName = MessageConstants.QUEUE_SUBSCRIPTION_EVENT, activationConfig = {
        @ActivationConfigProperty(propertyName = MessageConstants.MESSAGING_TYPE_STR, propertyValue = MessageConstants.CONNECTION_TYPE),
        @ActivationConfigProperty(propertyName = MessageConstants.DESTINATION_TYPE_STR, propertyValue = MessageConstants.DESTINATION_TYPE_QUEUE),
        @ActivationConfigProperty(propertyName = MessageConstants.DESTINATION_STR, propertyValue = MessageConstants.QUEUE_NAME_SUBSCRIPTION_EVENT)
})
@Slf4j
public class SubscriptionMessageConsumerBean implements MessageListener {

    @EJB
    private SubscriptionProducerBean subscriptionProducer;

    @Inject
    private SubscriptionService subscriptionService;

    @Inject
    private Event<ActivityReportGenerationResultsRequest> activityReportGenerationResultsRequestEvent;

    @Override
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
            SubscriptionBaseRequest moduleRequest = unMarshallMessage(textMessage.getText(), SubscriptionBaseRequest.class);
            switch (moduleRequest.getMethod()) {
                case PING :
                    break;
                case MODULE_ACCESS_PERMISSION_REQUEST :
                    log.info("[START] Received MODULE_ACCESS_PERMISSION_REQUEST..");
                    SubscriptionDataRequest request = (SubscriptionDataRequest) moduleRequest;
                    SubscriptionPermissionResponse dataRequestAllowed = subscriptionService.hasActiveSubscriptions(request.getQuery());
                    log.info("[INFO] Checked permissions... Going to send back : " + dataRequestAllowed.getSubscriptionCheck());
                    String messageToSend = marshallJaxBObjectToString(dataRequestAllowed);
                    subscriptionProducer.sendMessageWithSpecificIds(messageToSend, jmsReplyTo, null, jmsMessageID, jmsCorrelationID);
                    log.info("[END] Answer sent to queue : " + jmsReplyTo);
                    break;
                case DATA_CHANGE_REQUEST :
                    log.error("DATA_CHANGE_REQUEST not implemented yet!");
                    break;
                case ACTIVITY_REPORT_GENERATION_RESULTS_REQUEST:
                    activityReportGenerationResultsRequestEvent.fire((ActivityReportGenerationResultsRequest) moduleRequest);
                    break;
                default:
                    subscriptionProducer.sendMessageWithSpecificIds("[ Not implemented method consumed: {} ]", jmsReplyTo, null, jmsMessageID, jmsCorrelationID );
            }
        } catch (MessageException | JAXBException | JMSException e) {
            try {
                subscriptionProducer.sendMessageWithSpecificIds(e.getLocalizedMessage(), jmsReplyTo,null, jmsMessageID, jmsCorrelationID);
            } catch (MessageException e1) {
                log.error("Unrecoverable error while in an JMSException | JAXBException!", e1);
            }
        }
    }
}
