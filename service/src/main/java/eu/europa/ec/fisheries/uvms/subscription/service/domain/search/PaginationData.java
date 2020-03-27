package eu.europa.ec.fisheries.uvms.subscription.service.domain.search;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * Pagination data for queries.
 */
public interface PaginationData {
	@Min(1)
	@NotNull
	Integer getOffset();

	@Min(1)
	@NotNull
	Integer getPageSize();
}
