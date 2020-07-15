package eu.europa.ec.fisheries.uvms.subscription.activity.execution;

import static eu.europa.ec.fisheries.uvms.subscription.activity.ActivityConstants.KEY_REPORT_ID_PREFIX;
import static eu.europa.ec.fisheries.uvms.subscription.activity.ActivityConstants.KEY_TRIP_ID_PREFIX;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import eu.europa.ec.fisheries.uvms.activity.model.schemas.VesselIdentifierSchemeIdEnum;
import eu.europa.ec.fisheries.uvms.subscription.activity.communication.ActivitySender;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEmailConfiguration;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionExecutionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionOutput;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.email.EmailAttachment;
import eu.europa.ec.fisheries.uvms.subscription.service.email.EmailData;
import eu.europa.ec.fisheries.uvms.subscription.service.email.EmailService;
import eu.europa.ec.fisheries.uvms.subscription.service.execution.SubscriptionExecutionService;
import eu.europa.ec.fisheries.uvms.subscription.service.execution.SubscriptionExecutor;
import eu.europa.ec.fisheries.uvms.subscription.service.messaging.asset.AssetSender;
import eu.europa.ec.fisheries.uvms.subscription.service.messaging.usm.ReceiverAndDataflow;
import eu.europa.ec.fisheries.uvms.subscription.service.messaging.usm.UsmSender;
import eu.europa.ec.fisheries.uvms.subscription.service.trigger.TriggeredSubscriptionDataUtil;
import eu.europa.ec.fisheries.wsdl.asset.types.VesselIdentifiersHolder;
import eu.europa.ec.fisheries.wsdl.subscription.module.ActivityReportGenerationResultsRequest;
import eu.europa.ec.fisheries.wsdl.subscription.module.AttachmentResponseObject;
import eu.europa.fisheries.uvms.subscription.model.enums.OutgoingMessageType;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of {@link SubscriptionExecutor} for executing FA Reports and/or emails.
 * <p>
 * The principle is that the email attachments contain the same information as the FA Report that is sent
 * or would be sent from the subscription configuration. We notify the Activities module that we do not
 * want to forward a report by sending {@code null} receiver/dataflow. We specify a dedicated flag for
 * sending/not sending email.
 * <p>
 * Since the generation of attachments is potentially a time consuming operation, we do not wait
 * synchronously. The activity sender/client requests the response in the subscriptions incoming queue and
 * the {@code SubscriptionMessageConsumerBean} emits a CDI event with the response that we handle here.
 * The response from Activities contains the id of the{@code SubscriptionExecutionEntity}.
 */
@ApplicationScoped
@Slf4j
public class FaReportAndEmailSubscriptionExecutor implements SubscriptionExecutor {

	private ActivitySender activitySender;
	private UsmSender usmSender;
	private AssetSender assetSender;
	private SubscriptionExecutionService subscriptionExecutionService;
	private EmailService emailService;
	private DatatypeFactory datatypeFactory;

	@Inject
	public FaReportAndEmailSubscriptionExecutor(ActivitySender activitySender, UsmSender usmSender, AssetSender assetSender, SubscriptionExecutionService subscriptionExecutionService, EmailService emailService, DatatypeFactory datatypeFactory) {
		this.activitySender = activitySender;
		this.usmSender = usmSender;
		this.assetSender = assetSender;
		this.subscriptionExecutionService = subscriptionExecutionService;
		this.emailService = emailService;
		this.datatypeFactory = datatypeFactory;
	}

	/**
	 * Default constructor for frameworks.
	 */
	@SuppressWarnings("unused")
	public FaReportAndEmailSubscriptionExecutor() {
		// NOOP
	}

	@Override
	public void execute(SubscriptionExecutionEntity execution) {
		TriggeredSubscriptionEntity triggeredSubscription = execution.getTriggeredSubscription();
		SubscriptionEntity subscription = triggeredSubscription.getSubscription();
		if (TRUE.equals(subscription.getOutput().getHasEmail()) || subscription.getOutput().getMessageType() == OutgoingMessageType.FA_REPORT) {
			ReceiverAndDataflow receiverAndDataflow = Optional.ofNullable(subscription.getOutput().getSubscriber())
					.filter(s -> subscription.getOutput().getMessageType() == OutgoingMessageType.FA_REPORT)
					.filter(subscriber -> subscriber.getEndpointId() != null && subscriber.getChannelId() != null)
					.map(subscriber -> usmSender.findReceiverAndDataflow(subscriber.getEndpointId(), subscriber.getChannelId()))
					.orElseGet(ReceiverAndDataflow::new);
			switch (subscription.getExecution().getTriggerType()) {
				case INC_FA_REPORT:
					fromReportTrigger(subscription, triggeredSubscription, execution, receiverAndDataflow);
					break;
				case INC_POSITION:
					fromPositionTrigger(subscription, triggeredSubscription, execution, receiverAndDataflow);
					break;
				default:
					log.warn("cannot handle subscriptions triggered by {}", subscription.getExecution().getTriggerType());
			}
		}
	}

	void onActivityReportGenerationResultsRequest(@Observes ActivityReportGenerationResultsRequest request) {
		SubscriptionExecutionEntity execution = subscriptionExecutionService.findById(request.getExecutionId());
		if (execution != null) {
			if (request.getMessageId() != null && execution.getTriggeredSubscription().getSubscription().getOutput().getMessageType() == OutgoingMessageType.FA_REPORT) {
				execution.getMessageIds().add(request.getMessageId());
			}
			Optional.ofNullable(execution.getTriggeredSubscription().getSubscription().getOutput())
					.map(SubscriptionOutput::getHasEmail)
					.filter(TRUE::equals)
					.ifPresent(x -> sendEmail(request, execution));
		}
	}

	private void sendEmail(ActivityReportGenerationResultsRequest request, SubscriptionExecutionEntity execution) {
		TriggeredSubscriptionEntity triggeredSubscription = execution.getTriggeredSubscription();
		SubscriptionEntity subscription = triggeredSubscription.getSubscription();
		EmailData emailData = new EmailData();
		emailData.setBody(emailService.findEmailBodyEntity(subscription).getBody());
		emailData.setMimeType("text/plain");
		emailData.setReceivers(subscription.getOutput().getEmails());
		emailData.setZipAttachments(TRUE.equals(subscription.getOutput().getEmailConfiguration().getZipAttachments()));
		Optional.ofNullable(subscription.getOutput().getEmailConfiguration())
				.map(SubscriptionEmailConfiguration::getPassword)
				.map(Base64.getDecoder()::decode)
				.map(String::new)
				.ifPresent(emailData::setPassword);
		emailData.setEmailAttachmentList(request.getAttachments().stream().map(this::toEmailAttachment).collect(Collectors.toList()));
		emailService.send(emailData);
	}

	private EmailAttachment toEmailAttachment(AttachmentResponseObject attachmentResponseObject) {
		return new EmailAttachment(attachmentResponseObject.getTripId(), attachmentResponseObject.getType().value(), attachmentResponseObject.getContent());
	}

	private void fromReportTrigger(SubscriptionEntity subscription, TriggeredSubscriptionEntity triggeredSubscription, SubscriptionExecutionEntity execution, ReceiverAndDataflow receiverAndDataflow) {
		Map<String, String> dataMap = toDataMap(triggeredSubscription);
		if (TRUE.equals(subscription.getOutput().getLogbook())) {
			activitySender.forwardFaReportWithLogbook(
					execution.getId(),
					TRUE.equals(subscription.getOutput().getGenerateNewReportId()),
					receiverAndDataflow.getReceiver(),
					receiverAndDataflow.getDataflow(),
					TRUE.equals(subscription.getOutput().getConsolidated()),
					extractTripIds(dataMap),
					TRUE.equals(subscription.getOutput().getHasEmail()),
					extractAssetGuid(TRUE.equals(subscription.getOutput().getHasEmail()), triggeredSubscription.getId(), dataMap),
					Optional.ofNullable(subscription.getOutput().getEmailConfiguration()).map(SubscriptionEmailConfiguration::getIsPdf).orElse(FALSE),
					Optional.ofNullable(subscription.getOutput().getEmailConfiguration()).map(SubscriptionEmailConfiguration::getIsXml).orElse(FALSE),
					extractVesselIdentifiers(subscription)
			);
		} else {
			activitySender.forwardMultipleFaReports(
					execution.getId(),
					TRUE.equals(subscription.getOutput().getGenerateNewReportId()),
					receiverAndDataflow.getReceiver(),
					receiverAndDataflow.getDataflow(),
					TRUE.equals(subscription.getOutput().getConsolidated()),
					extractReportIds(dataMap),
					TRUE.equals(subscription.getOutput().getHasEmail()),
					extractAssetGuid(TRUE.equals(subscription.getOutput().getHasEmail()), triggeredSubscription.getId(), dataMap),
					Optional.ofNullable(subscription.getOutput().getEmailConfiguration()).map(SubscriptionEmailConfiguration::getIsPdf).orElse(FALSE),
					Optional.ofNullable(subscription.getOutput().getEmailConfiguration()).map(SubscriptionEmailConfiguration::getIsXml).orElse(FALSE),
					extractVesselIdentifiers(subscription)
			);
		}
	}

	private void fromPositionTrigger(SubscriptionEntity subscription, TriggeredSubscriptionEntity triggeredSubscription, SubscriptionExecutionEntity execution, ReceiverAndDataflow receiverAndDataflow) {
		Map<String, String> dataMap = toDataMap(triggeredSubscription);
		activitySender.forwardFaReportFromPosition(
				execution.getId(),
				TRUE.equals(subscription.getOutput().getGenerateNewReportId()),
				receiverAndDataflow.getReceiver(),
				receiverAndDataflow.getDataflow(),
				TRUE.equals(subscription.getOutput().getConsolidated()),
				TRUE.equals(subscription.getOutput().getLogbook()),
				calculateStartDate(triggeredSubscription.getId(), dataMap),
				calculateEndDate(triggeredSubscription.getId(), dataMap),
				extractAssetGuid(TRUE.equals(subscription.getOutput().getHasEmail()), triggeredSubscription.getId(), dataMap),
				extractAssetHistGuid(triggeredSubscription.getId(), dataMap),
				TRUE.equals(subscription.getOutput().getHasEmail()),
				Optional.ofNullable(subscription.getOutput().getEmailConfiguration()).map(SubscriptionEmailConfiguration::getIsPdf).orElse(FALSE),
				Optional.ofNullable(subscription.getOutput().getEmailConfiguration()).map(SubscriptionEmailConfiguration::getIsXml).orElse(FALSE),
				extractVesselIdentifiers(subscription)
		);
	}

	private String extractAssetGuid(boolean hasEmail, Long triggeredSubscriptionId, Map<String, String> dataMap) {
		if (!hasEmail) {
			return null;
		}
		String connectId = dataMap.get(TriggeredSubscriptionDataUtil.KEY_CONNECT_ID);
		Objects.requireNonNull(connectId, "connectId not found in data of " + triggeredSubscriptionId);
		VesselIdentifiersHolder vesselIdentifiers = assetSender.findVesselIdentifiers(connectId);
		Objects.requireNonNull(vesselIdentifiers.getAssetGuid(), "asset GUID null for connectId " + connectId + " of " + triggeredSubscriptionId);
		return vesselIdentifiers.getAssetGuid();
	}

	private String extractAssetHistGuid(Long triggeredSubscriptionId, Map<String, String> dataMap) {
		String connectId = dataMap.get(TriggeredSubscriptionDataUtil.KEY_CONNECT_ID);
		Objects.requireNonNull(connectId, "connectId not found in data of " + triggeredSubscriptionId);
		return connectId;
	}

	private List<String> extractReportIds(Map<String, String> dataMap) {
		return dataMap.entrySet().stream()
				.filter(e -> e.getKey().startsWith(KEY_REPORT_ID_PREFIX))
				.sorted(Comparator.comparingInt(e -> Integer.parseInt(e.getKey().substring(KEY_REPORT_ID_PREFIX.length()))))
				.map(Map.Entry::getValue)
				.collect(Collectors.toList());
	}

	private List<String> extractTripIds(Map<String, String> dataMap) {
		return dataMap.entrySet().stream()
				.filter(e -> e.getKey().startsWith(KEY_TRIP_ID_PREFIX))
				.sorted(Comparator.comparingInt(e -> Integer.parseInt(e.getKey().substring(KEY_TRIP_ID_PREFIX.length()))))
				.map(Map.Entry::getValue)
				.collect(Collectors.toList());
	}

	private XMLGregorianCalendar calculateStartDate(Long triggeredSubscriptionId, Map<String, String> dataMap) {
		String occurrence = extractOccurrenceDate(triggeredSubscriptionId, dataMap);
		ZonedDateTime zdt = ZonedDateTime.parse(occurrence);
		ZonedDateTime startOfDay = zdt.toLocalDate().atStartOfDay(zdt.getZone());
		return datatypeFactory.newXMLGregorianCalendar(GregorianCalendar.from(startOfDay));
	}

	private XMLGregorianCalendar calculateEndDate(Long triggeredSubscriptionId, Map<String, String> dataMap) {
		String occurrence = extractOccurrenceDate(triggeredSubscriptionId, dataMap);
		ZonedDateTime zdt = ZonedDateTime.parse(occurrence);
		ZonedDateTime startOfNextDay = zdt.toLocalDate().atStartOfDay(zdt.getZone()).plusDays(1);
		return datatypeFactory.newXMLGregorianCalendar(GregorianCalendar.from(startOfNextDay));
	}

	private String extractOccurrenceDate(Long triggeredSubscriptionId, Map<String, String> dataMap) {
		String occurrence = dataMap.get(TriggeredSubscriptionDataUtil.KEY_OCCURRENCE);
		Objects.requireNonNull(occurrence, "occurence not found in data of " + triggeredSubscriptionId);
		return occurrence;
	}

	private List<VesselIdentifierSchemeIdEnum> extractVesselIdentifiers(SubscriptionEntity subscription) {
		return subscription.getOutput().getVesselIds().stream().map(id -> VesselIdentifierSchemeIdEnum.fromValue(id.toString())).collect(Collectors.toList());
	}
}
