package eu.europa.ec.fisheries.uvms.subscription.activity.communication;

import java.util.concurrent.CompletionStage;

import eu.europa.ec.fisheries.wsdl.user.types.EndPoint;


/**
 * Client to interesting USM services.
 */
public interface UsmClient {
	/**
	 * Retrieve the endpoint with the gicen id.
	 *
	 * @param endpointId The endpoint id
	 * @return A promise to the endpoint
	 */
	CompletionStage<EndPoint> findEndpoint(Long endpointId);
}
