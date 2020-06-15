/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.service.messaging;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.TextMessage;
import javax.xml.bind.JAXBException;
import java.util.List;

import eu.europa.ec.fisheries.uvms.commons.message.api.MessageException;
import eu.europa.ec.fisheries.uvms.commons.message.impl.JAXBUtils;
import eu.europa.ec.fisheries.uvms.user.model.exception.ModelMarshallException;
import eu.europa.ec.fisheries.uvms.user.model.mapper.JAXBMarshaller;
import eu.europa.ec.fisheries.uvms.user.model.mapper.UserModuleRequestMapper;
import eu.europa.ec.fisheries.wsdl.user.module.FindEndpointRequest;
import eu.europa.ec.fisheries.wsdl.user.module.FindEndpointResponse;
import eu.europa.ec.fisheries.wsdl.user.module.FindOrganisationsResponse;
import eu.europa.ec.fisheries.wsdl.user.module.UserModuleMethod;
import eu.europa.ec.fisheries.wsdl.user.types.EndPoint;
import eu.europa.ec.fisheries.wsdl.user.types.Organisation;
import eu.europa.fisheries.uvms.subscription.model.exceptions.ApplicationException;

/**
 * Implementation of {@link UsmClient}.
 */
@ApplicationScoped
public class UsmClientImpl implements UsmClient{

    private SubscriptionUserProducerBean subscriptionUserProducer;
    private SubscriptionConsumerBean subscriptionConsumer;

    /**
     * Injection constructor.
     *
     * @param subscriptionUserProducer The (JMS) producer bean for this module
     * @param subscriptionConsumer The user queue
     */
    @Inject
    public UsmClientImpl(SubscriptionUserProducerBean subscriptionUserProducer, SubscriptionConsumerBean subscriptionConsumer) {
        this.subscriptionUserProducer = subscriptionUserProducer;
        this.subscriptionConsumer = subscriptionConsumer;
    }

    /**
     * Constructor for frameworks.
     */
    @SuppressWarnings("unused")
    UsmClientImpl() {
        // NOOP
    }

    @Override
    public List<Organisation> getAllOrganisations(String scopeName, String roleName, String requester) {
        List<Organisation> organisations = null;
        try {
            String getAllOrganisationRequest = UserModuleRequestMapper.mapToGetAllOrganisationRequest(scopeName, roleName, requester);
            String correlationID = subscriptionUserProducer.sendModuleMessage(getAllOrganisationRequest, subscriptionConsumer.getDestination());
            if (correlationID != null){
                TextMessage message = subscriptionConsumer.getMessage(correlationID, TextMessage.class );
                FindOrganisationsResponse responseMessage = JAXBUtils.unMarshallMessage(message.getText() , FindOrganisationsResponse.class);
                organisations = responseMessage.getOrganisation();
            }
        } catch (ModelMarshallException | MessageException | JMSException | JAXBException e) {
            throw new ApplicationException(e);
        }
        return organisations;
    }

    @Override
    public EndPoint findEndpoint(Long endpointId) {
        FindEndpointRequest request = new FindEndpointRequest();
        request.setMethod(UserModuleMethod.FIND_ENDPOINT);
        request.setId(endpointId);
        EndPoint endpoint = null;
        try {
            String correlationID = subscriptionUserProducer.sendMessageToSpecificQueue(JAXBMarshaller.marshallJaxBObjectToString(request),
                                                                                                subscriptionUserProducer.getDestination(),
                                                                                                subscriptionConsumer.getDestination());
            if(correlationID != null) {
                TextMessage message = subscriptionConsumer.getMessage(correlationID, TextMessage.class );
                FindEndpointResponse response = JAXBUtils.unMarshallMessage( message.getText() , FindEndpointResponse.class);
                endpoint = response.getEndpoint();
            }
        } catch (MessageException | ModelMarshallException | JMSException | JAXBException e) {
            throw new ApplicationException(e);
        }
        return endpoint;
    }
}
