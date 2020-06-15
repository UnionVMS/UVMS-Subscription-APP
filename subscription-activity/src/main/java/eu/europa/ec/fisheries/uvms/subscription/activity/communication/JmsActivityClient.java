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

import eu.europa.ec.fisheries.uvms.activity.model.exception.ActivityModelMarshallException;
import eu.europa.ec.fisheries.uvms.activity.model.mapper.JAXBMarshaller;
import eu.europa.ec.fisheries.uvms.activity.model.schemas.CreateAndSendFAQueryForTripRequest;
import eu.europa.ec.fisheries.uvms.activity.model.schemas.CreateAndSendFAQueryForVesselRequest;
import eu.europa.ec.fisheries.uvms.activity.model.schemas.CreateAndSendFAQueryResponse;
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
    public CreateAndSendFAQueryResponse sendRequest(CreateAndSendFAQueryForVesselRequest request) {
        try {
            String correlationId = subscriptionProducer.sendMessageToSpecificQueue(JAXBMarshaller.marshallJaxBObjectToString(request), activityQueue, subscriptionConsumerBean.getDestination());
            return getCreateAndSendFAQueryResponse(correlationId);
        } catch (MessageException | ActivityModelMarshallException e) {
            throw new ExecutionException(e);
        }
    }

    @Override
    public CreateAndSendFAQueryResponse sendRequest(CreateAndSendFAQueryForTripRequest request) {
        try {
            String correlationId = subscriptionProducer.sendMessageToSpecificQueue(JAXBMarshaller.marshallJaxBObjectToString(request), activityQueue, subscriptionConsumerBean.getDestination());
            return getCreateAndSendFAQueryResponse(correlationId);
        } catch (MessageException | ActivityModelMarshallException e) {
            throw new ExecutionException(e);
        }
    }

    private CreateAndSendFAQueryResponse getCreateAndSendFAQueryResponse(String correlationId) throws MessageException, ActivityModelMarshallException {
        CreateAndSendFAQueryResponse response = null;
        if(correlationId != null) {
            TextMessage textMessage = subscriptionConsumerBean.getMessage(correlationId, TextMessage.class);
            response = JAXBMarshaller.unmarshallTextMessage(textMessage, CreateAndSendFAQueryResponse.class);
        }
        return response;
    }
}
