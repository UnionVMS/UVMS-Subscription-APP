package eu.europa.ec.fisheries.uvms.subscription.service.messaging;

import java.util.List;

import eu.europa.ec.fisheries.wsdl.user.types.EndPoint;
import eu.europa.ec.fisheries.wsdl.user.types.Organisation;


/**
 * Client to interesting USM services.
 */
public interface UsmClient {
	/**
	 * Retrieve all organisations.
	 *
	 * @param scopeName name of user scope
	 * @param roleName role name of user
	 * @param requester requester value
	 * @return A list of organisations
	 */
	List<Organisation> getAllOrganisations(String scopeName, String roleName, String requester);

	/**
	 * Retrieve the endpoint with the given id.
	 *
	 * @param endpointId The endpoint id
	 * @return The endpoint
	 */
	EndPoint findEndpoint(Long endpointId);
}
