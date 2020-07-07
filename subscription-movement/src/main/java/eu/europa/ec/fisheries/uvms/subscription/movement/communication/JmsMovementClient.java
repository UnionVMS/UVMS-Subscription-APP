/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2020.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.movement.communication;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.jms.Queue;
import javax.jms.TextMessage;

import eu.europa.ec.fisheries.schema.movement.module.v1.FilterGuidListByAreaAndDateRequest;
import eu.europa.ec.fisheries.schema.movement.module.v1.FilterGuidListByAreaAndDateResponse;
import eu.europa.ec.fisheries.schema.movement.module.v1.ForwardPositionRequest;
import eu.europa.ec.fisheries.schema.movement.module.v1.ForwardPositionResponse;
import eu.europa.ec.fisheries.uvms.commons.message.api.MessageException;
import eu.europa.ec.fisheries.uvms.movement.model.exception.MovementModelException;
import eu.europa.ec.fisheries.uvms.movement.model.mapper.JAXBMarshaller;
import eu.europa.ec.fisheries.uvms.subscription.service.messaging.SubscriptionConsumerBean;
import eu.europa.ec.fisheries.uvms.subscription.service.messaging.SubscriptionProducerBean;

/**
 * JMS implementation of the {@link MovementClient}.
 */
@ApplicationScoped
public class JmsMovementClient implements MovementClient {

    private SubscriptionProducerBean subscriptionProducer;
    private SubscriptionConsumerBean subscriptionConsumerBean;
    private Queue movementQueue;

    /**
     * Injection constructor.
     *
     * @param subscriptionProducer     The subscription module producer bean
     * @param subscriptionConsumerBean The subscription module consumer bean
     * @param movementQueue            The movement specific queue created by ManagedObjectsProducer
     */
    @Inject
    public JmsMovementClient(SubscriptionProducerBean subscriptionProducer, SubscriptionConsumerBean subscriptionConsumerBean, @MovementQueue Queue movementQueue) {
        this.subscriptionProducer = subscriptionProducer;
        this.subscriptionConsumerBean = subscriptionConsumerBean;
        this.movementQueue = movementQueue;
    }

    /**
     * Constructor for frameworks.
     */
    @SuppressWarnings("unused")
    JmsMovementClient() {
        // NOOP
    }

    @Override
    public FilterGuidListByAreaAndDateResponse filterGuidListForDateByArea(FilterGuidListByAreaAndDateRequest request) {
        try {
            String correlationId = subscriptionProducer.sendMessageToSpecificQueue(JAXBMarshaller.marshallJaxBObjectToString(request), movementQueue, subscriptionConsumerBean.getDestination());
            FilterGuidListByAreaAndDateResponse response = null;
            if (correlationId != null) {
                TextMessage textMessage = subscriptionConsumerBean.getMessage(correlationId, TextMessage.class);
                response = JAXBMarshaller.unmarshallTextMessage(textMessage, FilterGuidListByAreaAndDateResponse.class);
            }
            return response;
        } catch (MessageException | MovementModelException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public ForwardPositionResponse forwardPosition(ForwardPositionRequest request) {
        try {
            String correlationId = subscriptionProducer.sendMessageToSpecificQueue(JAXBMarshaller.marshallJaxBObjectToString(request), movementQueue, subscriptionConsumerBean.getDestination());
            ForwardPositionResponse response = null;
            if (correlationId != null) {
                TextMessage textMessage = subscriptionConsumerBean.getMessage(correlationId, TextMessage.class);
                response = JAXBMarshaller.unmarshallTextMessage(textMessage, ForwardPositionResponse.class);
            }
            return response;
        } catch (MessageException | MovementModelException e) {
            e.printStackTrace();
        }
        return null;
    }
}
