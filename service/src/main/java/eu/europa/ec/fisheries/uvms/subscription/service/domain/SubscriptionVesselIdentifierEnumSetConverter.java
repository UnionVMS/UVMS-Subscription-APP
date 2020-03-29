package eu.europa.ec.fisheries.uvms.subscription.service.domain;

/**
 * {@code EnumSet} converter for sets of {@link SubscriptionVesselIdentifier}.
 */
public class SubscriptionVesselIdentifierEnumSetConverter extends BaseEnumSetConverter<SubscriptionVesselIdentifier> {
	public SubscriptionVesselIdentifierEnumSetConverter() {
		super(SubscriptionVesselIdentifier.class);
	}
}
