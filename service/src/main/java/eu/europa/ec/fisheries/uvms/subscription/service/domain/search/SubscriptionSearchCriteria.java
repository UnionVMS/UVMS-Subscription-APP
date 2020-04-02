package eu.europa.ec.fisheries.uvms.subscription.service.domain.search;

import java.time.ZonedDateTime;

import eu.europa.fisheries.uvms.subscription.model.enums.AccessibilityType;
import eu.europa.fisheries.uvms.subscription.model.enums.OutgoingMessageType;

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
	OutgoingMessageType getMessageType();
	AccessibilityType getAccessibilityType();
}
