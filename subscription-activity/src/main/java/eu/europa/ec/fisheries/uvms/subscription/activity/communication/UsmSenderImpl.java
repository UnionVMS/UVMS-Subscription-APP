package eu.europa.ec.fisheries.uvms.subscription.activity.communication;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityNotFoundException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import eu.europa.ec.fisheries.uvms.subscription.service.messaging.UsmClient;
import eu.europa.ec.fisheries.wsdl.user.types.Channel;
import eu.europa.ec.fisheries.wsdl.user.types.EndPoint;
import eu.europa.fisheries.uvms.subscription.model.exceptions.ApplicationException;
import eu.europa.fisheries.uvms.subscription.model.exceptions.EntityDoesNotExistException;

/**
 * Implementation of the {@link UsmSender}.
 */
@ApplicationScoped
class UsmSenderImpl implements UsmSender {

	private UsmClient usmClient;

	/**
	 * Injection constructor.
	 *
	 * @param usmClient The USM client
	 */
	@Inject
	public UsmSenderImpl(UsmClient usmClient) {
		this.usmClient = usmClient;
	}

	/**
	 * Constructor for frameworks.
	 */
	@SuppressWarnings("unused")
	UsmSenderImpl() {
		// NOOP
	}

	@Override
	public ReceiverAndDataflow findReceiverAndDataflow(Long endpointId, Long channelId) {
		EndPoint endpoint = usmClient.findEndpoint(endpointId);
		if(endpoint == null){
			throw new EntityNotFoundException("Endpoint with id " + endpoint + " not found.");
		}
		String dataflow = endpoint.getChannels().stream()
				.filter(channel -> channel.getId() == channelId)
				.map(Channel::getDataFlow)
				.findFirst()
				.orElseThrow(() -> new EntityDoesNotExistException("Cannot find channelId " + channelId + " under endpoint " + endpointId));
		return new ReceiverAndDataflow(endpoint.getUri(), dataflow);
	}
}
