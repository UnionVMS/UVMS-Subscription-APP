package eu.europa.ec.fisheries.uvms.subscription.activity.communication;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
		try {
			EndPoint endpoint = usmClient.findEndpoint(endpointId).toCompletableFuture().get(30, TimeUnit.SECONDS);
			String dataflow = endpoint.getChannels().stream()
					.filter(channel -> channel.getId() == channelId)
					.map(Channel::getDataFlow)
					.findFirst()
					.orElseThrow(() -> new EntityDoesNotExistException("Cannot find channelId " + channelId + " under endpoint " + endpointId));
			return new ReceiverAndDataflow(endpoint.getUri(), dataflow);
		} catch (InterruptedException | TimeoutException e) {
			throw new CommunicationException(e);
		} catch (ExecutionException e) {
			if (e.getCause() != null && e.getCause() instanceof ApplicationException) {
				throw (ApplicationException) e.getCause();
			}
			throw new CommunicationException(e);
		}
	}
}
