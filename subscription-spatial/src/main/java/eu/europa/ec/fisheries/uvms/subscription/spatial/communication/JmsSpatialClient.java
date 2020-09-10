/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2020.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.spatial.communication;

import eu.europa.ec.fisheries.uvms.commons.message.api.MessageException;
import eu.europa.ec.fisheries.uvms.spatial.model.exception.SpatialModelMarshallException;
import eu.europa.ec.fisheries.uvms.spatial.model.mapper.JAXBMarshaller;
import eu.europa.ec.fisheries.uvms.spatial.model.schemas.SpatialModuleRequest;
import eu.europa.ec.fisheries.uvms.subscription.service.messaging.SubscriptionConsumerBean;
import eu.europa.ec.fisheries.uvms.subscription.service.messaging.SubscriptionProducerBean;
import eu.europa.fisheries.uvms.subscription.model.exceptions.ExecutionException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.jms.Queue;
import javax.jms.TextMessage;
import javax.xml.bind.JAXBException;

/**
 * JMS implementation of the {@link SpatialClient}.
 */
@ApplicationScoped
class JmsSpatialClient implements SpatialClient {

    private SubscriptionProducerBean subscriptionProducer;
    private SubscriptionConsumerBean subscriptionConsumerBean;
    private Queue spatialQueue;

    /**
     * Injection constructor.
     * @param subscriptionProducer     The subscription producer
     * @param subscriptionConsumerBean The subscription consumer bean
     * @param spatialQueue             The spatial queue
     */
    @Inject
    public JmsSpatialClient(SubscriptionProducerBean subscriptionProducer,SubscriptionConsumerBean subscriptionConsumerBean, @SpatialQueue Queue spatialQueue) {
        this.subscriptionProducer = subscriptionProducer;
        this.subscriptionConsumerBean = subscriptionConsumerBean;
        this.spatialQueue = spatialQueue;
    }

    /**
     * Constructor for frameworks.
     */
    @SuppressWarnings("unused")
    JmsSpatialClient() {
        // NOOP
    }

    @Override
    public <T> T sendRequest(SpatialModuleRequest request, Class<T> responseClass) {
        try {
            String correlationId = subscriptionProducer.sendMessageToSpecificQueue(JAXBMarshaller.marshallJaxBObjectToString(request), spatialQueue, subscriptionConsumerBean.getDestination());
            return createResponse(correlationId, responseClass);
        } catch (MessageException | SpatialModelMarshallException e) {
            throw new ExecutionException(e);
        }
    }

    private <T> T createResponse(String correlationId, Class<T> responseClass) throws MessageException, SpatialModelMarshallException {
        T response = null;
        if (correlationId != null) {
            TextMessage textMessage = subscriptionConsumerBean.getMessage(correlationId, TextMessage.class);
            try {
                response = JAXBMarshaller.unmarshallTextMessage(textMessage, responseClass);
            } catch (SpatialModelMarshallException sme1) {
                if (sme1.getCause() instanceof JAXBException) {
                    JAXBException exception = JAXBMarshaller.unmarshallTextMessage(textMessage, JAXBException.class);
                    throw new SpatialFaultException(exception.getErrorCode(), exception.getMessage(),exception.getCause());
                }
            }
        }
        return response;
    }
}
