package eu.europa.ec.fisheries.uvms.subscription.service.domain.search;

import java.time.ZonedDateTime;
import java.util.Collection;

import eu.europa.ec.fisheries.wsdl.subscription.module.AreaType;
import eu.europa.fisheries.uvms.subscription.model.enums.AssetType;
import eu.europa.fisheries.uvms.subscription.model.enums.OutgoingMessageType;
import eu.europa.fisheries.uvms.subscription.model.enums.TriggerType;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Subscription search criteria.
 */
public interface SubscriptionSearchCriteria {
	String getName();
	Boolean getActive();
	Long getOrganisation();
	Long getEndPoint();
	Long getChannel();
	String getDescription();
	ZonedDateTime getStartDate();
	ZonedDateTime getEndDate();
	ZonedDateTime getValidAt();
	OutgoingMessageType getMessageType();
	/** Demand that the subscription fulfills any of the given area criteria. */
	Collection<AreaCriterion> getInAnyArea();
	/** Return subscriptions that have no area (i.e. {@code hasAreas = false}), even if there are areas in the {@link #getInAnyArea()} criterion. */
	Boolean getAllowWithNoArea();
	/** Demand that the subscription fulfills any of the given asset criteria. */
	Collection<AssetCriterion> getWithAnyAsset();
	/** Return subscriptions that have no asset (i.e. {@code hasAssets = false}), even if there are assets in the {@link #getWithAnyAsset()} criterion. */
	Boolean getAllowWithNoAsset();
	/** Demand that the subscription trigger is any of these. */
	Collection<TriggerType> getWithAnyTriggerType();

	/**
	 * Criterion to demand that a subscription is triggered by an area.
	 */
	@AllArgsConstructor
	@Getter
	@EqualsAndHashCode
	class AreaCriterion {
		private final AreaType type;
		private final Long gid;
	}

	/**
	 * Criterion to demand that a subscription is triggered by an asset.
	 */
	@AllArgsConstructor
	@Getter
	@EqualsAndHashCode
	class AssetCriterion {
		private final AssetType type;
		private final String guid;
	}
}
