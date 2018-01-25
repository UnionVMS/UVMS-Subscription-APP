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

package eu.europa.ec.fisheries.uvms.subscription.service.bean;

import static eu.europa.ec.fisheries.uvms.commons.message.api.MessageConstants.CONNECTION_FACTORY;
import static eu.europa.ec.fisheries.uvms.commons.message.api.MessageConstants.CONNECTION_TYPE;
import static eu.europa.ec.fisheries.uvms.commons.message.api.MessageConstants.DESTINATION_TYPE_QUEUE;
import static eu.europa.ec.fisheries.uvms.commons.message.impl.JAXBUtils.marshallJaxBObjectToString;
import static eu.europa.ec.fisheries.uvms.commons.message.impl.JAXBUtils.unMarshallMessage;
import static eu.europa.ec.fisheries.uvms.subscription.service.bean.SubscriptionMessageConsumerBean.QUEUE_SUBSCRIPTION;
import static eu.europa.ec.fisheries.uvms.subscription.service.bean.SubscriptionMessageConsumerBean.QUEUE_NAME_SUBSCRIPTION;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import eu.europa.ec.fisheries.wsdl.subscription.module.SubscriptionBaseRequest;
import eu.europa.ec.fisheries.wsdl.subscription.module.SubscriptionDataRequest;
import eu.europa.ec.fisheries.wsdl.subscription.module.SubscriptionPermissionResponse;
import lombok.extern.slf4j.Slf4j;

@MessageDriven(mappedName = QUEUE_SUBSCRIPTION, activationConfig = {
        @ActivationConfigProperty(propertyName = "messagingType", propertyValue = CONNECTION_TYPE),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = DESTINATION_TYPE_QUEUE),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = QUEUE_NAME_SUBSCRIPTION),
        @ActivationConfigProperty(propertyName = "destinationJndiName", propertyValue = QUEUE_SUBSCRIPTION),
        @ActivationConfigProperty(propertyName = "connectionFactoryJndiName", propertyValue = CONNECTION_FACTORY)
})
@Slf4j
public class SubscriptionMessageConsumerBean implements MessageListener {

    static final String QUEUE_SUBSCRIPTION = "jms/queue/UVMSSubscription";
    static final String QUEUE_NAME_SUBSCRIPTION = "UVMSSubscription";

    @EJB
    private SubscriptionProducerBean subscriptionProducerBean;

    @EJB
    private SubscriptionServiceBean service;


    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void onMessage(Message message) {
        TextMessage textMessage;
        String jmsCorrelationID = null;
        String jmsMessageID = null;
        try {
            log.info("[INFO] Received message in Subscription...");
            textMessage = (TextMessage) message;
            jmsCorrelationID = textMessage.getJMSCorrelationID();
            jmsMessageID = textMessage.getJMSMessageID();
            SubscriptionBaseRequest moduleRequest = unMarshallMessage(textMessage.getText(), SubscriptionBaseRequest.class);
            switch (moduleRequest.getMethod()) {
                case PING:
                    break;
                case MODULE_ACCESS_PERMISSION_REQUEST :
                    log.info("[START] Received MODULE_ACCESS_PERMISSION_REQUEST..");
                    SubscriptionDataRequest request = unMarshallMessage(textMessage.getText(), SubscriptionDataRequest.class);
                    SubscriptionPermissionResponse dataRequestAllowed = service.hasActiveSubscriptions(request.getQuery());
                    log.info("[INFO] Checked permissions... Going to send back : " + dataRequestAllowed.getSubscriptionCheck());
                    String messageToSend = marshallJaxBObjectToString(dataRequestAllowed);
                    subscriptionProducerBean.sendMessage(jmsMessageID, jmsCorrelationID, subscriptionProducerBean.getRulesQueue(), null, messageToSend);
                    log.info("[END] Answer sent to Rules module...");
                    break;
                case DATA_CHANGE_REQUEST :
                    log.error("DATA_CHANGE_REQUEST not implemented yet!");
                    break;
                default:
                    subscriptionProducerBean.sendMessage(jmsMessageID, jmsCorrelationID, subscriptionProducerBean.getRulesQueue(), null, "[ Not implemented method consumed: {} ]");
            }
        } catch (Exception e) {
            subscriptionProducerBean.sendMessage(jmsMessageID, jmsCorrelationID, subscriptionProducerBean.getRulesQueue(), null, e.getLocalizedMessage());
        }
    }
}

