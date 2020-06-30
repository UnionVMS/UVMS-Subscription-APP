package eu.europa.ec.fisheries.uvms.subscription.movement.communication;

import eu.europa.ec.fisheries.schema.movement.module.v1.FilterGuidListByAreaAndDateResponse;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.AreaEntity;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link MovementSenderImpl}
 */
@EnableAutoWeld
@ExtendWith(MockitoExtension.class)
class MovementSenderImplTest {

    private static final String GUID_1 = "97a40d34-45ea-11e7-bec7-4c32759615eb";
    private static final String GUID_2 = "9694463e-45ea-11e7-bec7-4c32759615eb";

    @Produces @Mock
    private MovementClient movementClient;

    @Inject
    private MovementSenderImpl sut;

    @Test
    void testCreateUsingEmptyConstructor() {
        MovementSender movementSender = new MovementSenderImpl();
        assertNotNull(movementSender);
    }

    @Test
    void testFindReceiverAndDataflowNullEndpoint() {

        final LocalDate todayLD = LocalDate.now();
        final Instant lastMonth = todayLD.minusMonths(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
        final Instant nextMonth = todayLD.plusMonths(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
        FilterGuidListByAreaAndDateResponse response = new FilterGuidListByAreaAndDateResponse();
        response.getFilteredList().add(GUID_1);
        List<String> guidList = new ArrayList<>();
        guidList.add(GUID_1);
        guidList.add(GUID_2);
        Set<AreaEntity> areas = new HashSet<>();
        AreaEntity areaEntity1 = new AreaEntity();
        areaEntity1.setGid(11L);
        areaEntity1.setAreaType(eu.europa.ec.fisheries.wsdl.subscription.module.AreaType.GFCM);
        AreaEntity areaEntity2 = new AreaEntity();
        areaEntity2.setGid(12L);
        areaEntity2.setAreaType(eu.europa.ec.fisheries.wsdl.subscription.module.AreaType.PORT);
        areas.add(areaEntity1);
        areas.add(areaEntity2);

        when(movementClient.filterGuidListForDateByArea(any())).thenReturn(response);
        List<String> filteredList = sut.sendFilterGuidListForAreasRequest(guidList,Date.from(lastMonth),Date.from(nextMonth),areas);
        assertEquals(GUID_1,filteredList.get(0));
    }
}
