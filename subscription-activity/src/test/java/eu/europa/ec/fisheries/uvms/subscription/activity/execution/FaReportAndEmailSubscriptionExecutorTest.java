package eu.europa.ec.fisheries.uvms.subscription.activity.execution;

import static eu.europa.fisheries.uvms.subscription.model.enums.SubscriptionExecutionStatusType.QUEUED;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.xml.datatype.DatatypeFactory;

import java.util.Collections;
import java.util.EnumSet;

import eu.europa.ec.fisheries.uvms.activity.model.schemas.VesselIdentifierSchemeIdEnum;
import eu.europa.ec.fisheries.uvms.subscription.activity.communication.ActivitySender;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionExecution;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionExecutionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionOutput;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionSubscriber;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionDataEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.email.EmailService;
import eu.europa.ec.fisheries.uvms.subscription.service.execution.SubscriptionExecutionService;
import eu.europa.ec.fisheries.uvms.subscription.service.messaging.asset.AssetSender;
import eu.europa.ec.fisheries.uvms.subscription.service.messaging.usm.UsmSender;
import eu.europa.ec.fisheries.wsdl.asset.types.VesselIdentifiersHolder;
import eu.europa.fisheries.uvms.subscription.model.enums.OutgoingMessageType;
import eu.europa.fisheries.uvms.subscription.model.enums.SubscriptionVesselIdentifier;
import eu.europa.fisheries.uvms.subscription.model.enums.TriggerType;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.ThrowingSupplier;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for the {@link FaReportAndEmailSubscriptionExecutor}.
 */
@EnableAutoWeld
@ExtendWith(MockitoExtension.class)
public class FaReportAndEmailSubscriptionExecutorTest {

	private static final Long TRIGGERED_SUBSCRIPTION_ID = 33L;
	private static final long EXECUTION_ID = 67890L;
	private static final String TRIP_ID = "trip id";
	private static final String REPORT_ID = "report id";
	private static final String CONNECT_ID = "conn id";
	private static final String ASSET_GUID = "asset guid";

	@Produces @Mock
	private ActivitySender activitySender;

	@Produces @Mock
	private UsmSender usmSender;

	@Produces @Mock
	private AssetSender assetSender;

	@Produces @Mock
	private SubscriptionExecutionService subscriptionExecutionService;

	@Produces @Mock
	private EmailService emailService;

	@Produces @Mock
	private DatatypeFactory datatypeFactory;

	@Inject
	private FaReportAndEmailSubscriptionExecutor sut;

	@Test
	void testEmptyConstructor() {
		assertDoesNotThrow((ThrowingSupplier<FaReportAndEmailSubscriptionExecutor>) FaReportAndEmailSubscriptionExecutor::new);
	}

	@Test
	void testExecuteNoFaReportAndNoEmail() {
		SubscriptionExecutionEntity execution = setup(OutgoingMessageType.FA_QUERY, TriggerType.INC_FA_REPORT, false, false);
		sut.execute(execution);
		verifyNoMoreInteractions(activitySender, usmSender, assetSender, subscriptionExecutionService, emailService);
	}

	@Test
	void testExecuteOnlyEmailWithLogbook() {
		SubscriptionExecutionEntity execution = setup(OutgoingMessageType.FA_QUERY, TriggerType.INC_FA_REPORT, true, true);
		VesselIdentifiersHolder vesselIdentifiers = new VesselIdentifiersHolder();
		vesselIdentifiers.setAssetGuid(ASSET_GUID);
		when(assetSender.findVesselIdentifiers(CONNECT_ID)).thenReturn(vesselIdentifiers);
		execution.getTriggeredSubscription().getSubscription().getOutput().setVesselIds(EnumSet.of(SubscriptionVesselIdentifier.CFR));
		sut.execute(execution);
		verify(activitySender).forwardFaReportWithLogbook(
				eq(EXECUTION_ID),
				eq(false),
				eq(null),
				eq(null),
				eq(false),
				eq(Collections.singletonList(TRIP_ID)),
				eq(true),
				eq(ASSET_GUID),
				eq(false),
				eq(false),
				eq(Collections.singletonList(VesselIdentifierSchemeIdEnum.CFR))
		);
		verifyNoMoreInteractions(activitySender, usmSender, assetSender, subscriptionExecutionService, emailService);
	}

	@Test
	void testExecuteOnlyEmailWithoutLogbook() {
		SubscriptionExecutionEntity execution = setup(OutgoingMessageType.FA_QUERY, TriggerType.INC_FA_REPORT, true, false);
		VesselIdentifiersHolder vesselIdentifiers = new VesselIdentifiersHolder();
		vesselIdentifiers.setAssetGuid(ASSET_GUID);
		when(assetSender.findVesselIdentifiers(CONNECT_ID)).thenReturn(vesselIdentifiers);
		execution.getTriggeredSubscription().getSubscription().getOutput().setVesselIds(EnumSet.of(SubscriptionVesselIdentifier.CFR));
		sut.execute(execution);
		verify(activitySender).forwardMultipleFaReports(
				eq(EXECUTION_ID),
				eq(false),
				eq(null),
				eq(null),
				eq(false),
				eq(Collections.singletonList(REPORT_ID)),
				eq(true),
				eq(ASSET_GUID),
				eq(false),
				eq(false),
				eq(Collections.singletonList(VesselIdentifierSchemeIdEnum.CFR))
		);
		verifyNoMoreInteractions(activitySender, usmSender, assetSender, subscriptionExecutionService, emailService);
	}

	private SubscriptionExecutionEntity setup(OutgoingMessageType outgoingMessageType, TriggerType triggerType, boolean hasEmail, boolean logbook) {
		SubscriptionEntity subscription = new SubscriptionEntity();
		SubscriptionOutput output = new SubscriptionOutput();
		output.setMessageType(outgoingMessageType);
		output.setLogbook(logbook);
		output.setHasEmail(hasEmail);
		subscription.setOutput(output);
		SubscriptionSubscriber subscriber = new SubscriptionSubscriber();
		output.setSubscriber(subscriber);
		SubscriptionExecution subscriptionExecution = new SubscriptionExecution();
		subscriptionExecution.setTriggerType(triggerType);subscription.setExecution(subscriptionExecution);
		TriggeredSubscriptionEntity triggeredSubscription = new TriggeredSubscriptionEntity();
		triggeredSubscription.setId(TRIGGERED_SUBSCRIPTION_ID);
		triggeredSubscription.setSubscription(subscription);
		triggeredSubscription.getData().add(new TriggeredSubscriptionDataEntity(triggeredSubscription, "tripId_0", TRIP_ID));
		triggeredSubscription.getData().add(new TriggeredSubscriptionDataEntity(triggeredSubscription, "reportId_0", REPORT_ID));
		triggeredSubscription.getData().add(new TriggeredSubscriptionDataEntity(triggeredSubscription, "connectId", CONNECT_ID));
		SubscriptionExecutionEntity execution = new SubscriptionExecutionEntity();
		execution.setTriggeredSubscription(triggeredSubscription);
		execution.setStatus(QUEUED);
		execution.setId(EXECUTION_ID);
		return execution;
	}
}
