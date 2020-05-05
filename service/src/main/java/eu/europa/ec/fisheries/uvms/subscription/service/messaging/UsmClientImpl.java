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
    private SubscriptionUserConsumerBean subscriptionUserConsumer;

    /**
     * Injection constructor.
     *
     * @param subscriptionUserProducer The (JMS) producer bean for this module
     * @param subscriptionUserConsumer The user queue
     */
    @Inject
    public UsmClientImpl(SubscriptionUserProducerBean subscriptionUserProducer, SubscriptionUserConsumerBean subscriptionUserConsumer) {
        this.subscriptionUserProducer = subscriptionUserProducer;
        this.subscriptionUserConsumer = subscriptionUserConsumer;
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
            String correlationID = subscriptionUserProducer.sendModuleMessage(getAllOrganisationRequest, subscriptionUserConsumer.getDestination());
            if (correlationID != null){
                TextMessage message = subscriptionUserConsumer.getMessage(correlationID, TextMessage.class );
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
                                                                                                subscriptionUserConsumer.getDestination());
            if(correlationID != null) {
                TextMessage message = subscriptionUserConsumer.getMessage(correlationID, TextMessage.class );
                FindEndpointResponse response = JAXBUtils.unMarshallMessage( message.getText() , FindEndpointResponse.class);
                endpoint = response.getEndpoint();
            }
        } catch (MessageException | ModelMarshallException | JMSException | JAXBException e) {
            throw new ApplicationException(e);
        }
        return endpoint;
    }
}
