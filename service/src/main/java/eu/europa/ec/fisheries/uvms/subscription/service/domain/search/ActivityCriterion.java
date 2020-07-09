package eu.europa.ec.fisheries.uvms.subscription.service.domain.search;

import eu.europa.fisheries.uvms.subscription.model.enums.SubscriptionFaReportDocumentType;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Criterion to demand that a subscription is triggered by an activity.
 */
@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class ActivityCriterion {
	private final SubscriptionFaReportDocumentType type;
	private final String value;
}
