package eu.europa.ec.fisheries.uvms.subscription.service.domain.search;

import eu.europa.ec.fisheries.wsdl.subscription.module.AreaType;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Criterion to demand that a subscription is triggered by an area.
 */
@AllArgsConstructor
@Getter
@EqualsAndHashCode
public
class AreaCriterion {
	private final AreaType type;
	private final Long gid;
}
