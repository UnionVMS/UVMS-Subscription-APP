package eu.europa.ec.fisheries.uvms.subscription.service.dto.search;

import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.ColumnType;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.DirectionType;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.OrderByData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderByDataImpl implements OrderByData<ColumnType> {

    DirectionType direction;
    ColumnType field;
}
