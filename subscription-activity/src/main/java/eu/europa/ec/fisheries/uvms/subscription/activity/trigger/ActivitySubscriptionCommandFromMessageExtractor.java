/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2020.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.activity.trigger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.PrimitiveIterator;
import java.util.Set;
import java.util.TimeZone;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import eu.europa.ec.fisheries.uvms.activity.model.schemas.ActivityAreas;
import eu.europa.ec.fisheries.uvms.activity.model.schemas.FluxReportIdentifier;
import eu.europa.ec.fisheries.uvms.activity.model.schemas.ForwardReportToSubscriptionRequest;
import eu.europa.ec.fisheries.uvms.activity.model.schemas.ReportToSubscription;
import eu.europa.ec.fisheries.uvms.commons.message.impl.JAXBUtils;
import eu.europa.ec.fisheries.uvms.subscription.service.bean.Command;
import eu.europa.ec.fisheries.uvms.subscription.service.bean.SubscriptionFinder;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionDataEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.ActivityCriterion;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.AreaCriterion;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.SubscriptionSearchCriteria.AssetCriterion;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.SubscriptionSearchCriteria.SenderCriterion;
import eu.europa.ec.fisheries.uvms.subscription.service.messaging.asset.AssetSender;
import eu.europa.ec.fisheries.uvms.subscription.service.trigger.StopConditionCriteria;
import eu.europa.ec.fisheries.uvms.subscription.service.trigger.SubscriptionCommandFromMessageExtractor;
import eu.europa.ec.fisheries.uvms.subscription.service.trigger.TriggerCommandsFactory;
import eu.europa.ec.fisheries.uvms.subscription.service.trigger.TriggeredSubscriptionDataUtil;
import eu.europa.ec.fisheries.uvms.subscription.service.util.DateTimeService;
import eu.europa.ec.fisheries.wsdl.subscription.module.AreaType;
import eu.europa.fisheries.uvms.subscription.model.enums.AssetType;
import eu.europa.fisheries.uvms.subscription.model.enums.SubscriptionFaReportDocumentType;
import eu.europa.fisheries.uvms.subscription.model.enums.TriggerType;
import eu.europa.fisheries.uvms.subscription.model.enums.TriggeredSubscriptionStatus;
import eu.europa.fisheries.uvms.subscription.model.exceptions.MessageFormatException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._20.DelimitedPeriod;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._20.FishingActivity;
import un.unece.uncefact.data.standard.unqualifieddatatype._20.DateTimeType;

/**
 * Implementation of {@link SubscriptionCommandFromMessageExtractor} for activity messages.
 */
@Slf4j
@ApplicationScoped
public class ActivitySubscriptionCommandFromMessageExtractor implements SubscriptionCommandFromMessageExtractor {

	private static final String KEY_REPORT_ID_PREFIX = "reportId_";
	private static final String SOURCE = "activity";
	private static final Predicate<TriggeredSubscriptionDataEntity> BY_KEY_REPORT_ID_PREFIX = d -> d.getKey().startsWith(KEY_REPORT_ID_PREFIX);

	private SubscriptionFinder subscriptionFinder;
	private DatatypeFactory datatypeFactory;
	private AssetSender assetSender;
	private DateTimeService dateTimeService;
	private TriggerCommandsFactory triggerCommandsFactory;

	/**
	 * Constructor for injection.
	 *
	 * @param subscriptionFinder The finder
	 * @param datatypeFactory The data type factory
	 * @param assetSender The asset sender
	 * @param dateTimeService The date/time service
	 * @param triggerCommandsFactory The factory for commands
	 */
	@Inject
	public ActivitySubscriptionCommandFromMessageExtractor(SubscriptionFinder subscriptionFinder, DatatypeFactory datatypeFactory, AssetSender assetSender, DateTimeService dateTimeService, TriggerCommandsFactory triggerCommandsFactory) {
		this.subscriptionFinder = subscriptionFinder;
		this.datatypeFactory = datatypeFactory;
		this.assetSender = assetSender;
		this.dateTimeService = dateTimeService;
		this.triggerCommandsFactory = triggerCommandsFactory;
	}

	/**
	 * Constructor for frameworks.
	 */
	@SuppressWarnings("unused")
    ActivitySubscriptionCommandFromMessageExtractor() {
		// NOOP
	}

	@Override
	public String getEligibleSubscriptionSource() {
		return SOURCE;
	}

	@Override
	public Function<TriggeredSubscriptionEntity, Set<TriggeredSubscriptionDataEntity>> getDataForDuplicatesExtractor() {
		return TriggeredSubscriptionDataUtil::extractConnectId;
	}

	@Override
	public void preserveDataFromSupersededTriggering(TriggeredSubscriptionEntity superseded, TriggeredSubscriptionEntity replacement) {
		processTriggering(superseded, replacement);
	}

	@Override
	public Stream<Command> extractCommands(String representation, SenderCriterion senderCriterion) {
		Map<String, Map<Long, TriggeredSubscriptionEntity>> assetHistoryGuidToTriggeringsMap = new HashMap<>();
		return Stream.of(unmarshal(representation))
				.flatMap(this::extractReportsFromRequest)
				.flatMap(makeCommandsForReport(assetHistoryGuidToTriggeringsMap, senderCriterion));
	}

	private ForwardReportToSubscriptionRequest unmarshal(String representation) {
		try {
			return JAXBUtils.unMarshallMessage(representation, ForwardReportToSubscriptionRequest.class);
		} catch (JAXBException e) {
			throw new MessageFormatException(e);
		}
	}

	private Stream<ReportContext> extractReportsFromRequest(ForwardReportToSubscriptionRequest request) {
		return request.getFaReports().stream()
				.filter(reportToSubscription -> {
					if (reportToSubscription.getAssetHistoryGuids() == null) {
						log.error("Incoming FA Report ({}) has null asset guids", makeReportToSubscriptionId(reportToSubscription));
						return false;
					}
					if (reportToSubscription.getAssetHistoryGuids().size() != 1) {
						log.error("Incoming FA Report ({}) has {} asset guids, must only have one!", makeReportToSubscriptionId(reportToSubscription), reportToSubscription.getAssetHistoryGuids().size());
						return false;
					}
					return true;
				})
				.flatMap(reportToSubscription -> {
					Iterator<ActivityAreas> activityAreas = reportToSubscription.getActivityAreas().iterator();
					return reportToSubscription.getFishingActivities().stream()
							.map(fishingActivity -> new ReportContext(request, reportToSubscription.getFluxFaReportMessageIds(), fishingActivity, reportToSubscription.getFaReportType(), activityAreas.next(), null, reportToSubscription.getAssetHistoryGuids().get(0)))
							.filter(this::handleOccurrenceDate);
				});
	}

	private String makeReportToSubscriptionId(ReportToSubscription reportToSubscription) {
		return reportToSubscription.getFluxFaReportMessageIds().stream().map(id -> id.getSchemeId() + ':' + id.getId()).collect(Collectors.joining(","));
	}

	private boolean handleOccurrenceDate(ReportContext reportContext) {
		FishingActivity fishingActivity = reportContext.getFishingActivity();
		DateTimeType activityDateTimeType = fishingActivity.getOccurrenceDateTime() != null ? fishingActivity.getOccurrenceDateTime() : getFirstDateFromDelimitedPeriods(fishingActivity.getSpecifiedDelimitedPeriods());
		if (activityDateTimeType == null || activityDateTimeType.getDateTime() == null) {
			return false;
		}
		reportContext.setOccurrenceDate(activityDateTimeType.getDateTime().toGregorianCalendar().getTime());
		return true;
	}

	private Function<ReportContext, Stream<Command>> makeCommandsForReport(Map<String, Map<Long, TriggeredSubscriptionEntity>> assetHistoryGuidToTriggeringsMap, SenderCriterion senderCriterion) {
		return reportContext -> Stream.concat(
				Stream.of(reportContext)
						.flatMap(r -> findTriggeredSubscriptions(r, senderCriterion))
						.flatMap(makeTriggeredSubscriptionEntity(assetHistoryGuidToTriggeringsMap))
						.map(this::makeTriggerSubscriptionCommand),
				Stream.of(reportContext)
						.flatMap(this::makeStopConditionCriteria)
						.map(triggerCommandsFactory::createStopSubscriptionCommand)
		);
	}

	private Stream<ReportAndSubscription> findTriggeredSubscriptions(ReportContext reportContext, SenderCriterion senderCriterion) {
		FishingActivity fishingActivity = reportContext.getFishingActivity();
		ZonedDateTime validAt = ZonedDateTime.ofInstant(reportContext.occurrenceDate.toInstant(), ZoneId.of("UTC"));

		List<AreaCriterion> areas = extractAreas(reportContext).collect(Collectors.toList());

		List<AssetCriterion> assets = new ArrayList<>();
		assets.add(makeAssetCriteria(reportContext.getAssetHistGuid()));
		assets.addAll(makeAssetGroupCriteria(reportContext.occurrenceDate, reportContext.getAssetHistGuid()));

		List<ActivityCriterion> startActivityCriteria = Collections.singletonList(new ActivityCriterion(SubscriptionFaReportDocumentType.valueOf(reportContext.getFaReportType()), fishingActivity.getTypeCode().getValue()));

		return subscriptionFinder.findTriggeredSubscriptions(areas, assets, startActivityCriteria, senderCriterion, validAt, Collections.singleton(TriggerType.INC_FA_REPORT)).stream()
				.map(subscription -> new ReportAndSubscription(reportContext, subscription));
	}

	private DateTimeType getFirstDateFromDelimitedPeriods(Collection<DelimitedPeriod> delimitedPeriods) {
		return delimitedPeriods.stream()
				.map(DelimitedPeriod::getStartDateTime)
				.filter(Objects::nonNull)
				.min((a, b) -> a.getDateTime().compare(b.getDateTime()))
				.orElse(null);
	}

	private Stream<AreaCriterion> extractAreas(ReportContext reportContext) {
		return reportContext.getActivityAreas().getAreas().stream()
				.map(area -> new AreaCriterion(AreaType.fromValue(area.getAreaType()), Long.valueOf(area.getRemoteId())));
	}

	private AssetCriterion makeAssetCriteria(String assetHistGuid) {
		return new AssetCriterion(AssetType.ASSET, assetHistGuid);
	}

	private List<AssetCriterion> makeAssetGroupCriteria(Date date, String assetHistGuid) {
		return assetSender.findAssetGroupsForAsset(assetHistGuid, date)
							.stream()
							.map(assetGroupGuid -> new AssetCriterion(AssetType.VGROUP, assetGroupGuid))
							.collect(Collectors.toList());
	}

	private Function<ReportAndSubscription, Stream<TriggeredSubscriptionAndRequestContext>> makeTriggeredSubscriptionEntity(Map<String, Map<Long, TriggeredSubscriptionEntity>> assetHistoryGuidToTriggeringsMap) {
		return reportAndSubscription -> {
			TriggeredSubscriptionEntity result;
			Map<Long, TriggeredSubscriptionEntity> triggerings = assetHistoryGuidToTriggeringsMap.get(reportAndSubscription.getReportContext().getAssetHistGuid());
			if (triggerings == null) {
				result = createNewTriggeredSubscriptionEntity(reportAndSubscription);
				triggerings = new HashMap<>();
				triggerings.put(reportAndSubscription.getSubscription().getId(), result);
				assetHistoryGuidToTriggeringsMap.put(reportAndSubscription.getReportContext().getAssetHistGuid(), triggerings);
				return Stream.of(new TriggeredSubscriptionAndRequestContext(reportAndSubscription.getReportContext().getUnmarshalledMessage(), result));
			} else {
				result = triggerings.get(reportAndSubscription.getSubscription().getId());
				if (result == null) {
					result = createNewTriggeredSubscriptionEntity(reportAndSubscription);
					triggerings.put(reportAndSubscription.getSubscription().getId(), result);
					return Stream.of(new TriggeredSubscriptionAndRequestContext(reportAndSubscription.getReportContext().getUnmarshalledMessage(), result));
				} else {
					addFaReportMessageId(reportAndSubscription, result.getData(), result);
					return Stream.empty();
				}
			}
		};
	}

	private TriggeredSubscriptionEntity createNewTriggeredSubscriptionEntity(ReportAndSubscription reportAndSubscription) {
		TriggeredSubscriptionEntity result;
		result = new TriggeredSubscriptionEntity();
		result.setSubscription(reportAndSubscription.getSubscription());
		result.setSource(SOURCE);
		result.setCreationDate(dateTimeService.getNowAsDate());
		result.setStatus(TriggeredSubscriptionStatus.ACTIVE);
		result.setEffectiveFrom(reportAndSubscription.getReportContext().getOccurrenceDate());
		makeTriggeredSubscriptionData(result, reportAndSubscription);
		return result;
	}

	private void makeTriggeredSubscriptionData(TriggeredSubscriptionEntity triggeredSubscription, ReportAndSubscription input) {
		Set<TriggeredSubscriptionDataEntity> result = new HashSet<>();
		Optional.ofNullable(input.getReportContext().getAssetHistGuid()).ifPresent(connectId ->
			result.add(new TriggeredSubscriptionDataEntity(triggeredSubscription, TriggeredSubscriptionDataUtil.KEY_CONNECT_ID, connectId))
		);
		Optional.ofNullable(input.getReportContext().getOccurrenceDate()).ifPresent(positionTime -> {
			// XXX This probably needs to use the message report time, not the occurrence time
			GregorianCalendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
			calendar.setTime(positionTime);
			XMLGregorianCalendar xmlCalendar = datatypeFactory.newXMLGregorianCalendar(calendar);
			result.add(new TriggeredSubscriptionDataEntity(triggeredSubscription, TriggeredSubscriptionDataUtil.KEY_OCCURRENCE, xmlCalendar.toXMLFormat()));
		});
		addFaReportMessageId(input, result, triggeredSubscription);
		triggeredSubscription.setData(result);
	}

	private void addFaReportMessageId(ReportAndSubscription reportAndSubscription, Set<TriggeredSubscriptionDataEntity> triggeredSubscriptionData, TriggeredSubscriptionEntity triggeredSubscription) {
		long nextIndex = triggeredSubscriptionData.stream().filter(BY_KEY_REPORT_ID_PREFIX).count();
		Optional.ofNullable(reportAndSubscription.getReportContext().getFluxFaReportMessageIds()).map(List::stream).flatMap(Stream::findFirst)
				.ifPresent(faReportMessageId -> triggeredSubscriptionData.add(new TriggeredSubscriptionDataEntity(triggeredSubscription, KEY_REPORT_ID_PREFIX + nextIndex, faReportMessageId.getSchemeId() + ':' + faReportMessageId.getId())));
	}

	private Command makeTriggerSubscriptionCommand(TriggeredSubscriptionAndRequestContext context) {
		return triggerCommandsFactory.createTriggerSubscriptionFromSpecificMessageCommand(context.getTriggeredSubscription(), getDataForDuplicatesExtractor(), this::processTriggering);
	}

	private boolean processTriggering(TriggeredSubscriptionEntity triggeredSubscriptionCandidate, TriggeredSubscriptionEntity existingTriggeredSubscription) {
		Sequence indexes = new Sequence((int) existingTriggeredSubscription.getData().stream().filter(BY_KEY_REPORT_ID_PREFIX).count());
		triggeredSubscriptionCandidate.getData().stream()
				.filter(BY_KEY_REPORT_ID_PREFIX)
				.map(data -> new TriggeredSubscriptionDataEntity(existingTriggeredSubscription, KEY_REPORT_ID_PREFIX + indexes.nextInt(), data.getValue()))
				.forEach(existingTriggeredSubscription.getData()::add);
		return true;
	}

	private Stream<StopConditionCriteria> makeStopConditionCriteria(ReportContext reportContext) {
		StopConditionCriteria areaCriterion = new StopConditionCriteria();
		areaCriterion.setConnectId(reportContext.getAssetHistGuid());
		areaCriterion.setAreas(extractAreas(reportContext).collect(Collectors.toSet()));

		StopConditionCriteria stopActivityCriterion = new StopConditionCriteria();
		stopActivityCriterion.setConnectId(reportContext.getAssetHistGuid());
		stopActivityCriterion.setActivities(Collections.singleton(new ActivityCriterion(SubscriptionFaReportDocumentType.valueOf(reportContext.getFaReportType()), reportContext.getFishingActivity().getTypeCode().getValue())));

		return Stream.of(areaCriterion, stopActivityCriterion);
	}

	@Getter
	@Setter
	@AllArgsConstructor
	private static class ReportContext {
		/** The message that has triggered us. */
		private ForwardReportToSubscriptionRequest unmarshalledMessage;
		private List<FluxReportIdentifier> fluxFaReportMessageIds;
		private FishingActivity fishingActivity;
		private String faReportType;
		private ActivityAreas activityAreas;
		/** Occurrence date of activity, extracted from activity. */
		private Date occurrenceDate;
		/** Asset history guid of reporting asset. */
		private String assetHistGuid;
	}

	@Getter
	@AllArgsConstructor
	private static class ReportAndSubscription {
		private final ReportContext reportContext;
		private final SubscriptionEntity subscription;
	}

	@Getter
	@AllArgsConstructor
	private static class TriggeredSubscriptionAndRequestContext {
		/** The message that has triggered us. */
		private final ForwardReportToSubscriptionRequest unmarshalledMessage;
		/** The triggered subscription. */
		private final TriggeredSubscriptionEntity triggeredSubscription;
	}

	@AllArgsConstructor
	private static class Sequence implements PrimitiveIterator.OfInt {
		private int curval;

		@Override public int nextInt() {
			return curval++;
		}

		@Override public boolean hasNext() {
			return true;
		}
	}
}
