package eu.europa.ec.fisheries.uvms.subscription.service.dto.search;

import eu.europa.fisheries.uvms.subscription.model.enums.ColumnType;
import eu.europa.fisheries.uvms.subscription.model.enums.DirectionType;
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
