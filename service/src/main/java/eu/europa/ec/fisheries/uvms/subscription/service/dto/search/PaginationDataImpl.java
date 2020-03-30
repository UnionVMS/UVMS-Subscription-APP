package eu.europa.ec.fisheries.uvms.subscription.service.dto.search;

import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.PaginationData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaginationDataImpl implements PaginationData {

    Integer offset;
    Integer pageSize;
}
