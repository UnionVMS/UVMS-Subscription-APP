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
import static eu.europa.ec.fisheries.uvms.subscription.service.bean.SubscriptionMessageConsumerBean.QUEUE_NAME_SUBSCRIPTION_EVENT;
import static eu.europa.ec.fisheries.uvms.subscription.service.bean.SubscriptionMessageConsumerBean.QUEUE_SUBSCRIPTION_EVENT;

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

@MessageDriven(mappedName = QUEUE_SUBSCRIPTION_EVENT, activationConfig = {
        @ActivationConfigProperty(propertyName = "messagingType", propertyValue = CONNECTION_TYPE),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = DESTINATION_TYPE_QUEUE),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = QUEUE_NAME_SUBSCRIPTION_EVENT),
        @ActivationConfigProperty(propertyName = "destinationJndiName", propertyValue = QUEUE_SUBSCRIPTION_EVENT),
        @ActivationConfigProperty(propertyName = "connectionFactoryJndiName", propertyValue = CONNECTION_FACTORY)
})
@Slf4j
public class SubscriptionMessageConsumerBean implements MessageListener {

    static final String QUEUE_SUBSCRIPTION_EVENT = "jms/queue/UVMSSubscriptionEvent";
    static final String QUEUE_NAME_SUBSCRIPTION_EVENT = "UVMSSubscriptionEvent";

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
            textMessage = (TextMessage) message;
            jmsCorrelationID = textMessage.getJMSCorrelationID();
            jmsMessageID = textMessage.getJMSMessageID();
            SubscriptionBaseRequest moduleRequest = unMarshallMessage(textMessage.getText(), SubscriptionBaseRequest.class);
            switch (moduleRequest.getMethod()) {
                case PING:
                    break;
                case MODULE_ACCESS_PERMISSION_REQUEST:
                    SubscriptionDataRequest request = unMarshallMessage(textMessage.getText(), SubscriptionDataRequest.class);
                    SubscriptionPermissionResponse dataRequestAllowed = service.hasActiveSubscriptions(request.getQuery());
                    String messageToSend = marshallJaxBObjectToString(dataRequestAllowed);
                    subscriptionProducerBean.sendMessage(jmsMessageID, jmsCorrelationID, subscriptionProducerBean.getRulesQueue(), null, messageToSend);
                    break;
                case DATA_CHANGE_REQUEST:
                    break;
                default:
                    subscriptionProducerBean.sendMessage(jmsMessageID, jmsCorrelationID, subscriptionProducerBean.getRulesQueue(), null, "[ Not implemented method consumed: {} ]");
            }
        } catch (Exception e) {
            subscriptionProducerBean.sendMessage(jmsMessageID, jmsCorrelationID, subscriptionProducerBean.getRulesQueue(), null, e.getLocalizedMessage());
        }
    }
}

