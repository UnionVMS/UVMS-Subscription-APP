package eu.europa.ec.fisheries.uvms.subscription.activity.communication.asset;

import java.util.Map;

import eu.europa.fisheries.uvms.subscription.model.enums.SubscriptionVesselIdentifier;

/**
 * Service to communicate with the Asset module.
 */
public interface AssetSender {

	/**
	 * Find vessel identifiers by the asset history guid.
	 * @param assetHistGuid the asset history guid
	 */
	Map<SubscriptionVesselIdentifier, String> findVesselIdentifiers(String assetHistGuid);
}
