package eu.europa.ec.fisheries.uvms.subscription.activity.communication;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.jms.Destination;
import javax.jms.Queue;
import javax.xml.datatype.DatatypeFactory;
import java.util.Collections;

import eu.europa.ec.fisheries.uvms.activity.model.schemas.CreateAndSendFAQueryForTripRequest;
import eu.europa.ec.fisheries.uvms.activity.model.schemas.CreateAndSendFAQueryForVesselRequest;
import eu.europa.ec.fisheries.uvms.activity.model.schemas.CreateAndSendFAQueryResponse;
import eu.europa.ec.fisheries.uvms.activity.model.schemas.VesselIdentifierSchemeIdEnum;
import eu.europa.ec.fisheries.uvms.activity.model.schemas.VesselIdentifierType;
import eu.europa.ec.fisheries.uvms.commons.message.api.MessageException;
import eu.europa.ec.fisheries.uvms.subscription.service.messaging.SubscriptionProducerBean;
import eu.europa.fisheries.uvms.subscription.model.exceptions.ExecutionException;
import lombok.SneakyThrows;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests  for {@link ActivitySenderImpl}
 */
@EnableAutoWeld
@ExtendWith(MockitoExtension.class)
class ActivitySenderImplTest {

    @Produces @Mock
    private ActivityClient activityClient;

    @Inject
    ActivitySenderImpl sut;

    @Mock
    Destination destination;

    @Test
    @SneakyThrows
    void testSendVesselQuery() {
        CreateAndSendFAQueryResponse response = new CreateAndSendFAQueryResponse();
        response.setMessageId("uuid1");
        when(activityClient.sendRequest(any(CreateAndSendFAQueryForVesselRequest.class),CreateAndSendFAQueryResponse.class)).thenReturn(response);
        String messageId = sut.createAndSendQueryForVessel(Collections.singletonList(new VesselIdentifierType(VesselIdentifierSchemeIdEnum.CFR, "CFR123456789")), true,
                DatatypeFactory.newInstance().newXMLGregorianCalendar("2019-01-01T10:00:00"), DatatypeFactory.newInstance().newXMLGregorianCalendar("2019-02-01T10:00:00"), "receiver", "dataflow");
        assertEquals("uuid1", messageId);
    }

    @Test
    @SneakyThrows
    void testSendTripQuery() {
        CreateAndSendFAQueryResponse response = new CreateAndSendFAQueryResponse();
        response.setMessageId("uuid1");
        when(activityClient.sendRequest(any(CreateAndSendFAQueryForTripRequest.class),CreateAndSendFAQueryResponse.class)).thenReturn(response);
        String messageId = sut.createAndSendQueryForTrip("SRC-TRP-00000000001", true, "receiver", "dataflow");
        assertEquals("uuid1", messageId);
    }

    @Test
    @SneakyThrows
    void testSendVesselQueryWithNullResponse() {
        when(activityClient.sendRequest(any(CreateAndSendFAQueryForVesselRequest.class),CreateAndSendFAQueryResponse.class)).thenReturn(null);
        String messageId = sut.createAndSendQueryForVessel(Collections.singletonList(new VesselIdentifierType(VesselIdentifierSchemeIdEnum.CFR, "CFR123456789")), true,
                DatatypeFactory.newInstance().newXMLGregorianCalendar("2019-01-01T10:00:00"), DatatypeFactory.newInstance().newXMLGregorianCalendar("2019-02-01T10:00:00"), "receiver", "dataflow");
        assertEquals(null, messageId);
    }

    @Test
    @SneakyThrows
    void testSendTripQueryWithNullResponse() {
        when(activityClient.sendRequest(any(CreateAndSendFAQueryForTripRequest.class),CreateAndSendFAQueryResponse.class)).thenReturn(null);
        String messageId = sut.createAndSendQueryForTrip("SRC-TRP-00000000001", true, "receiver", "dataflow");
        assertEquals(null, messageId);
    }

    @Test
    @SneakyThrows
    void testSendVesselQueryWithEmptyRequest() {
        assertDoesNotThrow(() -> sut.createAndSendQueryForVessel(null, false, null, null, null, null));
    }

    @Test
    @SneakyThrows
    void testSendTripQueryWithException() {
        assertDoesNotThrow(()  -> sut.createAndSendQueryForTrip(null, false, null, null));
    }
}