package eu.europa.ec.fisheries.uvms.subscription.activity.communication;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import eu.europa.ec.fisheries.uvms.activity.model.schemas.ActivityModuleMethod;
import eu.europa.ec.fisheries.uvms.activity.model.schemas.AttachmentResponseObject;
import eu.europa.ec.fisheries.uvms.activity.model.schemas.AttachmentType;
import eu.europa.ec.fisheries.uvms.activity.model.schemas.CreateAndSendFAQueryForTripRequest;
import eu.europa.ec.fisheries.uvms.activity.model.schemas.CreateAndSendFAQueryForVesselRequest;
import eu.europa.ec.fisheries.uvms.activity.model.schemas.CreateAndSendFAQueryResponse;
import eu.europa.ec.fisheries.uvms.activity.model.schemas.FindMovementGuidsByReportIdsAndAssetGuidResponse;
import eu.europa.ec.fisheries.uvms.activity.model.schemas.FluxReportIdentifier;
import eu.europa.ec.fisheries.uvms.activity.model.schemas.ForwardFAReportFromPositionRequest;
import eu.europa.ec.fisheries.uvms.activity.model.schemas.ForwardFAReportWithLogbookRequest;
import eu.europa.ec.fisheries.uvms.activity.model.schemas.ForwardMultipleFAReportsRequest;
import eu.europa.ec.fisheries.uvms.activity.model.schemas.GetAttachmentsForGuidAndQueryPeriodResponse;
import eu.europa.ec.fisheries.uvms.activity.model.schemas.VesselIdentifierSchemeIdEnum;
import eu.europa.ec.fisheries.uvms.activity.model.schemas.VesselIdentifierType;
import eu.europa.ec.fisheries.uvms.subscription.service.email.EmailAttachment;
import lombok.SneakyThrows;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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

    @Test
    @SneakyThrows
    void testSendVesselQuery() {
        CreateAndSendFAQueryResponse response = new CreateAndSendFAQueryResponse();
        response.setMessageId("uuid1");
        when(activityClient.sendRequest(any(CreateAndSendFAQueryForVesselRequest.class), eq(CreateAndSendFAQueryResponse.class))).thenReturn(response);
        String messageId = sut.createAndSendQueryForVessel(Collections.singletonList(new VesselIdentifierType(VesselIdentifierSchemeIdEnum.CFR, "CFR123456789")), true,
                DatatypeFactory.newInstance().newXMLGregorianCalendar("2019-01-01T10:00:00"), DatatypeFactory.newInstance().newXMLGregorianCalendar("2019-02-01T10:00:00"), "receiver", "dataflow",false);
        assertEquals("uuid1", messageId);
    }

    @Test
    @SneakyThrows
    void testSendTripQuery() {
        CreateAndSendFAQueryResponse response = new CreateAndSendFAQueryResponse();
        response.setMessageId("uuid1");
        when(activityClient.sendRequest(any(CreateAndSendFAQueryForTripRequest.class), eq(CreateAndSendFAQueryResponse.class))).thenReturn(response);
        String messageId = sut.createAndSendQueryForTrip("SRC-TRP-00000000001", true, "receiver", "dataflow");
        assertEquals("uuid1", messageId);
    }

    @Test
    @SneakyThrows
    void testSendVesselQueryWithNullResponse() {
        when(activityClient.sendRequest(any(CreateAndSendFAQueryForVesselRequest.class), eq(CreateAndSendFAQueryResponse.class))).thenReturn(null);
        String messageId = sut.createAndSendQueryForVessel(Collections.singletonList(new VesselIdentifierType(VesselIdentifierSchemeIdEnum.CFR, "CFR123456789")), true,
                DatatypeFactory.newInstance().newXMLGregorianCalendar("2019-01-01T10:00:00"), DatatypeFactory.newInstance().newXMLGregorianCalendar("2019-02-01T10:00:00"), "receiver", "dataflow",false);
        assertNull(messageId);
    }

    @Test
    @SneakyThrows
    void testSendTripQueryWithNullResponse() {
        when(activityClient.sendRequest(any(CreateAndSendFAQueryForTripRequest.class), eq(CreateAndSendFAQueryResponse.class))).thenReturn(null);
        String messageId = sut.createAndSendQueryForTrip("SRC-TRP-00000000001", true, "receiver", "dataflow");
        assertNull(messageId);
    }

    @Test
    @SneakyThrows
    void testSendVesselQueryWithEmptyRequest() {
        assertDoesNotThrow(() -> sut.createAndSendQueryForVessel(null, false, null, null, null, null,false));
    }

    @Test
    @SneakyThrows
    void testSendTripQueryWithException() {
        assertDoesNotThrow(()  -> sut.createAndSendQueryForTrip(null, false, null, null));
    }

    @Test
    @SneakyThrows
    void testCreateAndSendRequestForAttachments() {
        String guid = UUID.randomUUID().toString();
        XMLGregorianCalendar startDate = DatatypeFactory.newInstance().newXMLGregorianCalendar("2019-01-01T10:00:00");
        XMLGregorianCalendar endDate = DatatypeFactory.newInstance().newXMLGregorianCalendar("2019-02-01T10:00:00");
        GetAttachmentsForGuidAndQueryPeriodResponse response = new GetAttachmentsForGuidAndQueryPeriodResponse();
        AttachmentResponseObject attachmentResponse = new AttachmentResponseObject();
        attachmentResponse.setTripId("trip id");
        attachmentResponse.setType(AttachmentType.PDF);
        attachmentResponse.setContent("CONTENT");
        response.setResponseLists(Collections.singletonList(attachmentResponse));
        when(activityClient.sendRequest(any(),eq(GetAttachmentsForGuidAndQueryPeriodResponse.class))).thenReturn(response);
        List<EmailAttachment> result = sut.createAndSendRequestForAttachments(guid, startDate, endDate, true, false, true, false);
        assertNotNull(result);
        assertEquals(1, result.size());
        EmailAttachment attachment = result.get(0);
        assertEquals("trip id", attachment.getTripId());
        assertEquals("PDF", attachment.getType());
        assertEquals("CONTENT", attachment.getContent());
    }

    @Test
    @SneakyThrows
    void testForwardMultipleFaReports() {
        sut.forwardMultipleFaReports(67890L, true, "receiver", "dataflow", false, Collections.singletonList("schemeId:reportId"), true, "asset guid", false, true, Collections.singletonList(VesselIdentifierSchemeIdEnum.ICCAT));
        ArgumentCaptor<ForwardMultipleFAReportsRequest> captor = ArgumentCaptor.forClass(ForwardMultipleFAReportsRequest.class);
        verify(activityClient).sendAsyncRequest(captor.capture());
        ForwardMultipleFAReportsRequest value = captor.getValue();
        assertEquals(ActivityModuleMethod.FORWARD_MULTIPLE_FA_REPORTS, value.getMethod());
        assertEquals(67890L, value.getExecutionId());
        assertTrue(value.isNewReportIds());
        assertEquals("receiver", value.getReceiver());
        assertEquals("dataflow", value.getDataflow());
        assertFalse(value.isConsolidated());
        assertEquals(Collections.singletonList(VesselIdentifierSchemeIdEnum.ICCAT), value.getVesselIdentifiers());
        assertFalse(value.getEmailConfig().isPdf());
        assertTrue(value.getEmailConfig().isXml());
        FluxReportIdentifier id = new FluxReportIdentifier();
        id.setId("reportId");
        id.setSchemeId("schemeId");
        assertEquals(Collections.singletonList(id), value.getReportIds());
    }

    @Test
    @SneakyThrows
    void testForwardFaReportWithLogbook() {
        sut.forwardFaReportWithLogbook(67890L, true, "receiver", "dataflow", false, Collections.singletonList("tripId"), false, null, true, true, Collections.singletonList(VesselIdentifierSchemeIdEnum.ICCAT));
        ArgumentCaptor<ForwardFAReportWithLogbookRequest> captor = ArgumentCaptor.forClass(ForwardFAReportWithLogbookRequest.class);
        verify(activityClient).sendAsyncRequest(captor.capture());
        ForwardFAReportWithLogbookRequest value = captor.getValue();
        assertEquals(ActivityModuleMethod.FORWARD_FA_REPORT_WITH_LOGBOOK, value.getMethod());
        assertEquals(67890L, value.getExecutionId());
        assertTrue(value.isNewReportIds());
        assertEquals("receiver", value.getReceiver());
        assertEquals("dataflow", value.getDataflow());
        assertFalse(value.isConsolidated());
        assertNull(value.getEmailConfig());
        assertEquals(Collections.singletonList("tripId"), value.getTripIds());
    }

    @Test
    @SneakyThrows
    void testForwardFaReportFromPosition() {
        XMLGregorianCalendar startDate = DatatypeFactory.newInstance().newXMLGregorianCalendar("2019-01-01T10:00:00");
        XMLGregorianCalendar endDate = DatatypeFactory.newInstance().newXMLGregorianCalendar("2019-02-01T10:00:00");
        sut.forwardFaReportFromPosition(67890L, true, "receiver", "dataflow", false, true, startDate, endDate, null, "asset hist guid", false, true, true, Collections.singletonList(VesselIdentifierSchemeIdEnum.ICCAT));
        ArgumentCaptor<ForwardFAReportFromPositionRequest> captor = ArgumentCaptor.forClass(ForwardFAReportFromPositionRequest.class);
        verify(activityClient).sendAsyncRequest(captor.capture());
        ForwardFAReportFromPositionRequest value = captor.getValue();
        assertEquals(ActivityModuleMethod.FORWARD_FA_REPORT_FROM_POSITION, value.getMethod());
        assertEquals(67890L, value.getExecutionId());
        assertTrue(value.isNewReportIds());
        assertEquals("receiver", value.getReceiver());
        assertEquals("dataflow", value.getDataflow());
        assertFalse(value.isConsolidated());
        assertNull(value.getEmailConfig());
        assertTrue(value.isLogbook());
        assertEquals(startDate, value.getStartDate());
        assertEquals(endDate, value.getEndDate());
        assertEquals("asset hist guid", value.getAssetHistoryGuid());
    }

    @Test
    @SneakyThrows
    void testFindMovementGuidsByReportIdsAndAssetGuidNotFound() {
        List<String> reportIds = Collections.singletonList("reportId");
        String assetGuid = UUID.randomUUID().toString();
        when(activityClient.sendRequest(any(), eq(FindMovementGuidsByReportIdsAndAssetGuidResponse.class))).thenReturn(null);
        List<String> result = sut.findMovementGuidsByReportIdsAndAssetGuid(reportIds, assetGuid);
        assertTrue(result.isEmpty());
    }

    @Test
    @SneakyThrows
    void testFindMovementGuidsByReportIdsAndAssetGuid() {
        List<String> reportIds = Collections.singletonList("reportId");
        String assetGuid = UUID.randomUUID().toString();
        FindMovementGuidsByReportIdsAndAssetGuidResponse response = new FindMovementGuidsByReportIdsAndAssetGuidResponse();
        response.setMovementGuids(Collections.singletonList("mov guid"));
        when(activityClient.sendRequest(any(), eq(FindMovementGuidsByReportIdsAndAssetGuidResponse.class))).thenReturn(response);
        List<String> result = sut.findMovementGuidsByReportIdsAndAssetGuid(reportIds, assetGuid);
        assertEquals(1, result.size());
        assertEquals("mov guid", result.get(0));
    }
}
