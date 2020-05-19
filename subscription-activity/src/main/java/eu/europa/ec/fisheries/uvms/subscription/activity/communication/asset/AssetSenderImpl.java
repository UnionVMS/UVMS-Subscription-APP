package eu.europa.ec.fisheries.uvms.subscription.activity.communication.asset;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityNotFoundException;
import java.util.Map;

import eu.europa.ec.fisheries.uvms.activity.model.schemas.VesselIdentifierType;
import eu.europa.ec.fisheries.uvms.subscription.service.messaging.AssetClient;
import eu.europa.fisheries.uvms.subscription.model.enums.SubscriptionVesselIdentifier;

/**
 * Implementation of {@link AssetSender}
 */
@ApplicationScoped
public class AssetSenderImpl implements AssetSender {

    private AssetClient assetClient;

    /**
     * Injection constructor.
     *
     * @param assetClient The Asset client
     */
    @Inject
    public AssetSenderImpl(AssetClient assetClient) {
        this.assetClient = assetClient;
    }

    /**
     * Constructor for frameworks.
     */
    @SuppressWarnings("unused")
    AssetSenderImpl() {
        // NOOP
    }

    @Override
    public Map<SubscriptionVesselIdentifier, String> findVesselIdentifiers(String assetHistGuid) {
        Map<SubscriptionVesselIdentifier, String> identifiers = assetClient.findAsset(assetHistGuid);
        if(identifiers.isEmpty()) {
            throw new EntityNotFoundException("Vessel identifiers fr asset with history guid " + assetHistGuid + " not found.");
        }
        return identifiers;
    }
}
