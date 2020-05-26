/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2020.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.service.messaging.asset;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.TextMessage;
import javax.xml.bind.JAXBException;

import eu.europa.ec.fisheries.uvms.commons.message.api.MessageException;
import eu.europa.ec.fisheries.uvms.commons.message.impl.JAXBUtils;
import eu.europa.ec.fisheries.uvms.subscription.service.messaging.SubscriptionAssetProducerBean;
import eu.europa.ec.fisheries.uvms.subscription.service.messaging.SubscriptionUserConsumerBean;
import eu.europa.ec.fisheries.uvms.user.model.exception.ModelMarshallException;
import eu.europa.ec.fisheries.uvms.user.model.mapper.JAXBMarshaller;
import eu.europa.ec.fisheries.wsdl.asset.module.FindAssetHistGuidByAssetGuidAndOccurrenceDateRequest;
import eu.europa.ec.fisheries.wsdl.asset.module.FindAssetHistGuidByAssetGuidAndOccurrenceDateResponse;
import eu.europa.ec.fisheries.wsdl.asset.module.FindVesselIdsByAssetHistGuidRequest;
import eu.europa.ec.fisheries.wsdl.asset.module.FindVesselIdsByAssetHistGuidResponse;
import eu.europa.ec.fisheries.wsdl.asset.module.FindVesselIdsByMultipleAssetHistGuidsRequest;
import eu.europa.ec.fisheries.wsdl.asset.module.FindVesselIdsByMultipleAssetHistGuidsResponse;
import eu.europa.fisheries.uvms.subscription.model.exceptions.ApplicationException;

/**
 * JMS implementation of the {@link AssetClient}.
 */
@ApplicationScoped
public class JmsAssetClient implements AssetClient {

	private SubscriptionAssetProducerBean subscriptionAssetProducerBean;
	private SubscriptionUserConsumerBean subscriptionUserConsumer;

	/**
	 * Injection constructor.
	 *
	 * @param subscriptionAssetProducerBean The (JMS) producer bean for this module
	 * @param subscriptionUserConsumer The user queue
	 */
	@Inject
	public JmsAssetClient(SubscriptionAssetProducerBean subscriptionAssetProducerBean, SubscriptionUserConsumerBean subscriptionUserConsumer) {
		this.subscriptionAssetProducerBean = subscriptionAssetProducerBean;
		this.subscriptionUserConsumer = subscriptionUserConsumer;
	}

	/**
	 * Constructor for frameworks.
	 */
	@SuppressWarnings("unused")
	JmsAssetClient() {
		// NOOP
	}

	@Override
	public FindVesselIdsByAssetHistGuidResponse findVesselIdsByAssetHistGuid(FindVesselIdsByAssetHistGuidRequest request) {
		try {
			String correlationID = subscriptionAssetProducerBean.sendMessageToSpecificQueue(JAXBMarshaller.marshallJaxBObjectToString(request),
					subscriptionAssetProducerBean.getDestination(),
					subscriptionUserConsumer.getDestination());

			FindVesselIdsByAssetHistGuidResponse response = null;
			if(correlationID != null) {
				TextMessage message = subscriptionUserConsumer.getMessage(correlationID, TextMessage.class );
				response = JAXBUtils.unMarshallMessage( message.getText() , FindVesselIdsByAssetHistGuidResponse.class);
			}
			return response;
		} catch (MessageException | ModelMarshallException | JMSException | JAXBException e) {
			throw new ApplicationException(e);
		}
	}

	@Override
	public FindVesselIdsByMultipleAssetHistGuidsResponse findVesselIdsByMultipleAssetHistGuid(FindVesselIdsByMultipleAssetHistGuidsRequest request) {
		try {
			String correlationID = subscriptionAssetProducerBean.sendMessageToSpecificQueue(JAXBMarshaller.marshallJaxBObjectToString(request),
					subscriptionAssetProducerBean.getDestination(),
					subscriptionUserConsumer.getDestination());

			FindVesselIdsByMultipleAssetHistGuidsResponse response = null;
			if(correlationID != null) {
				TextMessage message = subscriptionUserConsumer.getMessage(correlationID, TextMessage.class );
				response = JAXBUtils.unMarshallMessage( message.getText() , FindVesselIdsByMultipleAssetHistGuidsResponse.class);
			}
			return response;
		} catch (MessageException | ModelMarshallException | JMSException | JAXBException e) {
			throw new ApplicationException(e);
		}
	}

	@Override
	public FindAssetHistGuidByAssetGuidAndOccurrenceDateResponse findAssetHistGuidByAssetGuidAndOccurrenceDate(FindAssetHistGuidByAssetGuidAndOccurrenceDateRequest request) {
		try {
			String correlationID = subscriptionAssetProducerBean.sendMessageToSpecificQueue(JAXBMarshaller.marshallJaxBObjectToString(request),
					subscriptionAssetProducerBean.getDestination(),
					subscriptionUserConsumer.getDestination());

			FindAssetHistGuidByAssetGuidAndOccurrenceDateResponse response = null;
			if(correlationID != null) {
				TextMessage message = subscriptionUserConsumer.getMessage(correlationID, TextMessage.class );
				response = JAXBMarshaller.unmarshallTextMessage(message, FindAssetHistGuidByAssetGuidAndOccurrenceDateResponse.class);
			}
			return response;
		} catch (MessageException | ModelMarshallException e) {
			throw new ApplicationException(e);
		}
	}
}
