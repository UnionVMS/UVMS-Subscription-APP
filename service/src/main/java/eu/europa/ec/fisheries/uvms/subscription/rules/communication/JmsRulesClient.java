/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2020.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.rules.communication;

import eu.europa.ec.fisheries.schema.rules.module.v1.RulesBaseRequest;
import eu.europa.ec.fisheries.uvms.commons.message.api.MessageException;
import eu.europa.ec.fisheries.uvms.rules.model.exception.RulesModelMarshallException;
import eu.europa.ec.fisheries.uvms.rules.model.mapper.JAXBMarshaller;
import eu.europa.ec.fisheries.uvms.subscription.service.messaging.SubscriptionConsumerBean;
import eu.europa.ec.fisheries.uvms.subscription.service.messaging.SubscriptionProducerBean;
import eu.europa.ec.fisheries.uvms.subscription.service.messaging.SubscriptionQueue;
import eu.europa.fisheries.uvms.subscription.model.exceptions.ExecutionException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.jms.Queue;

/**
 * JMS implementation of the {@link RulesClient}.
 */
@ApplicationScoped
public class JmsRulesClient implements RulesClient {

    private SubscriptionProducerBean subscriptionProducer;
    private SubscriptionConsumerBean subscriptionConsumerBean;
    private Queue rulesQueue;
    private Queue subscriptionQueue;

    /**
     * Injection constructor.
     * @param subscriptionProducer
     * @param subscriptionConsumerBean
     * @param rulesQueue
     * @param subscriptionQueue
     */
    @Inject
    public JmsRulesClient(SubscriptionProducerBean subscriptionProducer, SubscriptionConsumerBean subscriptionConsumerBean, @RulesQueue Queue rulesQueue, @SubscriptionQueue Queue subscriptionQueue) {
        this.subscriptionProducer = subscriptionProducer;
        this.subscriptionConsumerBean = subscriptionConsumerBean;
        this.rulesQueue = rulesQueue;
        this.subscriptionQueue = subscriptionQueue;
    }

    /**
     * Constructor for frameworks.
     */
    @SuppressWarnings("unused")
    JmsRulesClient() {
        // NOOP
    }

    @Override
    public void sendAsyncRequest(RulesBaseRequest request) {
        try {
            subscriptionProducer.sendMessageToSpecificQueueSameTx(JAXBMarshaller.marshallJaxBObjectToString(request), rulesQueue, subscriptionQueue);
        } catch (MessageException | RulesModelMarshallException e) {
            throw new ExecutionException(e);
        }
    }
}
