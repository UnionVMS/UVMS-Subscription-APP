package eu.europa.ec.fisheries.uvms.subscription.service.messaging;

import java.util.Map;

import eu.europa.fisheries.uvms.subscription.model.enums.SubscriptionVesselIdentifier;


/**
 * Client to interesting Asset services.
 */
public interface AssetClient {
	/**
	 * Retrieve asset .
	 *
	 * @param assetHistGuid asset history guid
	 * @return map holding vessel identifiers of asset
	 */
	Map<SubscriptionVesselIdentifier, String> findAsset(String assetHistGuid);
}
