package eu.europa.ec.fisheries.uvms.subscription.movement.communication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.europa.ec.fisheries.schema.movement.common.v1.SimpleResponse;
import eu.europa.ec.fisheries.schema.movement.module.v1.FilterGuidListByAreaAndDateResponse;
import eu.europa.ec.fisheries.schema.movement.module.v1.ForwardPositionRequest;
import eu.europa.ec.fisheries.schema.movement.module.v1.ForwardPositionResponse;
import eu.europa.ec.fisheries.schema.movement.module.v1.MovementModuleMethod;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.AreaEntity;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for {@link MovementSenderImpl}
 */
@EnableAutoWeld
@ExtendWith(MockitoExtension.class)
class MovementSenderImplTest {

    private static final String GUID_1 = "97a40d34-45ea-11e7-bec7-4c32759615eb";
    private static final String GUID_2 = "9694463e-45ea-11e7-bec7-4c32759615eb";
    private static final String MESSAGE_ID = "a-message-id";

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

    @Test
    void testForwardPosition() {
        Map<String, String> vesselIdentifiers = new HashMap<>();
        vesselIdentifiers.put("IRCS", "IRCS_VALUE");
        vesselIdentifiers.put("CFR", "CFR_VALUE");
        vesselIdentifiers.put("UVI", "UVI_VALUE");
        vesselIdentifiers.put("EXT_MARK", "EXT_MARK_VALUE");
        vesselIdentifiers.put("ICCAT", "ICCAT_VALUE");
        String vesselFlagState = "GRC";
        List<String> movementGuids = Arrays.asList("a-movement-guid", "another-movement-guid");

        ForwardPositionResponse response = new ForwardPositionResponse();
        response.setMessageId(MESSAGE_ID);
        response.setResponse(SimpleResponse.OK);
        when(movementClient.forwardPosition(any())).thenReturn(response);

        String result = sut.forwardPosition(vesselIdentifiers, vesselFlagState, movementGuids, "receiver", "dataflow");

        ArgumentCaptor<ForwardPositionRequest> requestCaptor = ArgumentCaptor.forClass(ForwardPositionRequest.class);
        verify(movementClient).forwardPosition(requestCaptor.capture());
        ForwardPositionRequest invokedArgument = requestCaptor.getValue();

        assertEquals(MovementModuleMethod.FORWARD_POSITION, invokedArgument.getMethod());
        assertEquals(vesselFlagState, invokedArgument.getVesselIdentifyingProperties().getFlagState());

        assertEquals(vesselIdentifiers.get("IRCS"), invokedArgument.getVesselIdentifyingProperties().getIrcs());
        assertEquals(vesselIdentifiers.get("CFR"), invokedArgument.getVesselIdentifyingProperties().getCfr());
        assertEquals(vesselIdentifiers.get("UVI"), invokedArgument.getVesselIdentifyingProperties().getUvi());
        assertEquals(vesselIdentifiers.get("ICCAT"), invokedArgument.getVesselIdentifyingProperties().getIccat());
        assertEquals(vesselIdentifiers.get("EXT_MARK"), invokedArgument.getVesselIdentifyingProperties().getExtMarking());

        assertEquals(movementGuids.size(), invokedArgument.getMovementGuids().size());
        assertTrue(movementGuids.containsAll(invokedArgument.getMovementGuids()));

        assertEquals(MESSAGE_ID, result);
    }
}
