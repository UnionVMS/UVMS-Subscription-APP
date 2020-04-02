package eu.europa.fisheries.uvms.subscription.model.enums;

/**
 * Vessel identifier types that can be used as subscription outgoing message parameters.
 */
public enum SubscriptionVesselIdentifier {
	// KEEP THE ORDER - IT IS IMPORTANT FOR THE PERSISTENCE LOGIC
	CFR,
	IRCS,
	ICCAT,
	EXT_MARK,
	UVI
}
