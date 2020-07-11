/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2020.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.activity.communication;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.jms.Queue;
import javax.jms.TextMessage;

import java.text.MessageFormat;

import eu.europa.ec.fisheries.uvms.activity.model.exception.ActivityModelMarshallException;
import eu.europa.ec.fisheries.uvms.activity.model.mapper.JAXBMarshaller;
import eu.europa.ec.fisheries.uvms.activity.model.schemas.ActivityFault;
import eu.europa.ec.fisheries.uvms.activity.model.schemas.ActivityModuleMethod;
import eu.europa.ec.fisheries.uvms.activity.model.schemas.ActivityModuleRequest;
import eu.europa.ec.fisheries.uvms.commons.message.api.MessageException;
import eu.europa.ec.fisheries.uvms.subscription.service.messaging.SubscriptionConsumerBean;
import eu.europa.ec.fisheries.uvms.subscription.service.messaging.SubscriptionProducerBean;
import eu.europa.fisheries.uvms.subscription.model.exceptions.ExecutionException;

/**
 * JMS implementation of the {@link ActivityClient}.
 */
@ApplicationScoped
public class JmsActivityClient implements ActivityClient {

    private SubscriptionProducerBean subscriptionProducer;
    private SubscriptionConsumerBean subscriptionConsumerBean;
    private Queue activityQueue;

    /**
     * Injection constructor.
     *
     * @param subscriptionProducer
     * @param subscriptionConsumerBean
     * @param activityQueue
     */
    @Inject
    public JmsActivityClient(SubscriptionProducerBean subscriptionProducer, SubscriptionConsumerBean subscriptionConsumerBean, @ActivityQueue Queue activityQueue) {
        this.subscriptionProducer = subscriptionProducer;
        this.subscriptionConsumerBean = subscriptionConsumerBean;
        this.activityQueue = activityQueue;
    }

    /**
     * Constructor for frameworks.
     */
    @SuppressWarnings("unused")
    JmsActivityClient() {
        // NOOP
    }

    @Override
    public <T> T sendRequest(ActivityModuleRequest request, Class<T> responseClass) {
        try {
            String correlationId = subscriptionProducer.sendMessageToSpecificQueue(JAXBMarshaller.marshallJaxBObjectToString(request), activityQueue, subscriptionConsumerBean.getDestination());
            return createResponse(correlationId, responseClass, request.getMethod());
        } catch (MessageException | ActivityModelMarshallException e) {
            throw new ExecutionException(e);
        }
    }

    private <T> T createResponse(String correlationId, Class<T> responseClass, ActivityModuleMethod requestMethod) throws MessageException, ActivityModelMarshallException {
        T response = null;
        if(correlationId != null) {
            TextMessage textMessage = subscriptionConsumerBean.getMessage(correlationId, TextMessage.class);
            try {
                response = JAXBMarshaller.unmarshallTextMessage(textMessage, responseClass);
            } catch (ActivityModelMarshallException me1) {
                try {
                    // We may not be able to unmarshal to the wanted object, because activity returned us a fault:
                    ActivityFault activityFault = JAXBMarshaller.unmarshallTextMessage(textMessage, ActivityFault.class);
                    throw new ActivityFaultException(activityFault.getCode(), activityFault.getFault(), MessageFormat.format("error invoking {0}: {1} - {2}", requestMethod, activityFault.getCode(), activityFault.getFault()));
                } catch (ActivityModelMarshallException me2) {
                    // Genuine ActivityModelMarshallException, throwing the original:
                    throw me1;
                }
            }
        }
        return response;
    }
}
