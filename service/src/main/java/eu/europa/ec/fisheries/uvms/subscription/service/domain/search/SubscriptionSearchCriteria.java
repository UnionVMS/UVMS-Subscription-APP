package eu.europa.ec.fisheries.uvms.subscription.service.domain.search;

import java.time.ZonedDateTime;

import eu.europa.ec.fisheries.uvms.subscription.service.domain.AccessibilityType;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionType;
import eu.europa.ec.fisheries.wsdl.subscription.module.MessageType;

/**
 * Subscription search criteria.
 */
public interface SubscriptionSearchCriteria {
	String getName();
	Long getOrganisation();
	Boolean getEnabled();
	Long getChannel();
	Long getEndPoint();
	MessageType getMessageType();
	SubscriptionType getSubscriptionType();
	String getDescription();
	AccessibilityType getAccessibility();
	ZonedDateTime getStartDate();
	ZonedDateTime getEndDate();
}
