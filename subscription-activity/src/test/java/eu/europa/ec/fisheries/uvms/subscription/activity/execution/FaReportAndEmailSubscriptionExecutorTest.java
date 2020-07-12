package eu.europa.ec.fisheries.uvms.subscription.activity.execution;

import static eu.europa.fisheries.uvms.subscription.model.enums.SubscriptionExecutionStatusType.QUEUED;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import eu.europa.ec.fisheries.uvms.subscription.activity.communication.ActivitySender;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionExecution;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionExecutionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionOutput;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.email.EmailService;
import eu.europa.ec.fisheries.uvms.subscription.service.execution.SubscriptionExecutionService;
import eu.europa.ec.fisheries.uvms.subscription.service.messaging.asset.AssetSender;
import eu.europa.ec.fisheries.uvms.subscription.service.messaging.usm.UsmSender;
import eu.europa.fisheries.uvms.subscription.model.enums.OutgoingMessageType;
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

	@Inject
	private FaReportAndEmailSubscriptionExecutor sut;

	@Test
	void testEmptyConstructor() {
		assertDoesNotThrow((ThrowingSupplier<FaReportAndEmailSubscriptionExecutor>) FaReportAndEmailSubscriptionExecutor::new);
	}

	@Test
	void testExecuteNoFaReportAndNoEmail() {
		SubscriptionExecutionEntity execution = setup(OutgoingMessageType.FA_QUERY, TriggerType.INC_FA_REPORT, false);
		sut.execute(execution);
		verifyNoMoreInteractions(activitySender, usmSender, assetSender, subscriptionExecutionService, emailService);
	}

	private SubscriptionExecutionEntity setup(OutgoingMessageType outgoingMessageType, TriggerType triggerType, boolean hasEmail) {
		SubscriptionEntity subscription = new SubscriptionEntity();
		SubscriptionOutput output = new SubscriptionOutput();
		output.setMessageType(outgoingMessageType);
		subscription.setOutput(output);
		SubscriptionExecution subscriptionExecution = new SubscriptionExecution();
		subscriptionExecution.setTriggerType(triggerType);subscription.setExecution(subscriptionExecution);
		TriggeredSubscriptionEntity triggeredSubscription = new TriggeredSubscriptionEntity();
		triggeredSubscription.setId(TRIGGERED_SUBSCRIPTION_ID);
		triggeredSubscription.setSubscription(subscription);
		SubscriptionExecutionEntity execution = new SubscriptionExecutionEntity();
		execution.setTriggeredSubscription(triggeredSubscription);
		execution.setStatus(QUEUED);
		return execution;
	}
}
