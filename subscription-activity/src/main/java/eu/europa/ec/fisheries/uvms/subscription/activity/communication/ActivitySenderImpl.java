/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

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

import eu.europa.ec.fisheries.uvms.activity.model.exception.ActivityModelMarshallException;
import eu.europa.ec.fisheries.uvms.activity.model.mapper.JAXBMarshaller;
import eu.europa.ec.fisheries.uvms.activity.model.schemas.CreateAndSendFAQueryForTripRequest;
import eu.europa.ec.fisheries.uvms.activity.model.schemas.CreateAndSendFAQueryForVesselRequest;
import eu.europa.ec.fisheries.uvms.commons.message.api.MessageException;
import eu.europa.ec.fisheries.uvms.subscription.service.messaging.SubscriptionProducerBean;
import eu.europa.fisheries.uvms.subscription.model.exceptions.ExecutionException;

/**
 * Implementation of {@link ActivitySender}.
 */
@ApplicationScoped
class ActivitySenderImpl implements ActivitySender {

	private SubscriptionProducerBean subscriptionProducer;
	private Queue activityQueue;

	/**
	 * Injection constructor.
	 *
	 * @param subscriptionProducer The (JMS) producer bean for this module
	 * @param activityQueue The activity queue
	 */
	@Inject
	public ActivitySenderImpl(SubscriptionProducerBean subscriptionProducer, @ActivityQueue Queue activityQueue) {
		this.subscriptionProducer = subscriptionProducer;
		this.activityQueue = activityQueue;
	}

	@Override
	public void send(CreateAndSendFAQueryForVesselRequest message) {
		try {
			subscriptionProducer.sendMessageToSpecificQueueSameTx(JAXBMarshaller.marshallJaxBObjectToString(message), activityQueue, subscriptionProducer.getDestination());
		} catch (MessageException | ActivityModelMarshallException e) {
			throw new ExecutionException(e);
		}
	}

	@Override
	public void send(CreateAndSendFAQueryForTripRequest message) {
		try {
			subscriptionProducer.sendMessageToSpecificQueueSameTx(JAXBMarshaller.marshallJaxBObjectToString(message), activityQueue, subscriptionProducer.getDestination());
		} catch (MessageException | ActivityModelMarshallException e) {
			throw new ExecutionException(e);
		}
	}
}
