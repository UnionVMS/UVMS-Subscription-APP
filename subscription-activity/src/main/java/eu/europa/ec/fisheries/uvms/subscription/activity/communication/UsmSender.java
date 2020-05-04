package eu.europa.ec.fisheries.uvms.subscription.activity.communication;

/**
 * Service to communicate with the User module.
 */
public interface UsmSender {
	/**
	 * Map the given endpoint and channel ids to the receiver symbolic name and dataflow URN.
	 *
	 * @param endpointId The endpoint id
	 * @param channelId The channel id
	 */
	ReceiverAndDataflow findReceiverAndDataflow(Long endpointId, Long channelId);
}
