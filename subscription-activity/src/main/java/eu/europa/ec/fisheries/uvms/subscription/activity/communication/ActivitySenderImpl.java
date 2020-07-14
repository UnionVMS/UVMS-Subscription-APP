/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.activity.communication;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import eu.europa.ec.fisheries.uvms.activity.model.schemas.ActivityModuleMethod;
import eu.europa.ec.fisheries.uvms.activity.model.schemas.AttachmentResponseObject;
import eu.europa.ec.fisheries.uvms.activity.model.schemas.CreateAndSendFAQueryForTripRequest;
import eu.europa.ec.fisheries.uvms.activity.model.schemas.CreateAndSendFAQueryForVesselRequest;
import eu.europa.ec.fisheries.uvms.activity.model.schemas.CreateAndSendFAQueryResponse;
import eu.europa.ec.fisheries.uvms.activity.model.schemas.EmailConfigForReportGeneration;
import eu.europa.ec.fisheries.uvms.activity.model.schemas.FluxReportIdentifier;
import eu.europa.ec.fisheries.uvms.activity.model.schemas.ForwardFAReportBaseRequest;
import eu.europa.ec.fisheries.uvms.activity.model.schemas.ForwardFAReportFromPositionRequest;
import eu.europa.ec.fisheries.uvms.activity.model.schemas.ForwardFAReportWithLogbookRequest;
import eu.europa.ec.fisheries.uvms.activity.model.schemas.ForwardMultipleFAReportsRequest;
import eu.europa.ec.fisheries.uvms.activity.model.schemas.GetAttachmentsForGuidAndQueryPeriod;
import eu.europa.ec.fisheries.uvms.activity.model.schemas.GetAttachmentsForGuidAndQueryPeriodRequest;
import eu.europa.ec.fisheries.uvms.activity.model.schemas.GetAttachmentsForGuidAndQueryPeriodResponse;
import eu.europa.ec.fisheries.uvms.activity.model.schemas.PluginType;
import eu.europa.ec.fisheries.uvms.activity.model.schemas.VesselIdentifierType;
import eu.europa.ec.fisheries.uvms.subscription.service.email.EmailAttachment;

/**
 * Implementation of {@link ActivitySender}.
 */
@ApplicationScoped
class ActivitySenderImpl implements ActivitySender {

	private ActivityClient activityClient;

	/**
	 * Injection constructor
	 *
	 * @param activityClient The low-level client to the services of the activity module
	 */
	@Inject
	public ActivitySenderImpl(ActivityClient activityClient) {
		this.activityClient = activityClient;
	}

	@Override
	public String createAndSendQueryForVessel(List<VesselIdentifierType> vesselIdentifiers, boolean consolidated, XMLGregorianCalendar startDate, XMLGregorianCalendar endDate, String receiver, String dataflow) {
		CreateAndSendFAQueryForVesselRequest request = new CreateAndSendFAQueryForVesselRequest(ActivityModuleMethod.CREATE_AND_SEND_FA_QUERY_FOR_VESSEL, PluginType.FLUX, vesselIdentifiers, consolidated, startDate, endDate, receiver, dataflow);
		CreateAndSendFAQueryResponse response = activityClient.sendRequest(request,CreateAndSendFAQueryResponse.class);
		String messageId = null;
		if(response != null) {
			messageId = response.getMessageId();
		}
		return messageId;
	}

	@Override
	public String createAndSendQueryForTrip(String tripId, boolean consolidated, String receiver, String dataflow) {
		CreateAndSendFAQueryForTripRequest request = new CreateAndSendFAQueryForTripRequest(ActivityModuleMethod.CREATE_AND_SEND_FA_QUERY_FOR_TRIP, PluginType.FLUX, tripId, consolidated, receiver, dataflow);
		CreateAndSendFAQueryResponse response = activityClient.sendRequest(request,CreateAndSendFAQueryResponse.class);
		String messageId = null;
		if(response != null) {
			messageId = response.getMessageId();
		}
		return messageId;
	}

	@Override
	public List<EmailAttachment> createAndSendRequestForAttachments(String guid, XMLGregorianCalendar startDate, XMLGregorianCalendar endDate, boolean logbook, boolean pdf, boolean xml, boolean consolidated) {
		GetAttachmentsForGuidAndQueryPeriodRequest request = new GetAttachmentsForGuidAndQueryPeriodRequest();
		request.setMethod(ActivityModuleMethod.FIND_ATTACHMENTS_FOR_GUID_AND_QUERY_PERIOD);
		GetAttachmentsForGuidAndQueryPeriod query = new GetAttachmentsForGuidAndQueryPeriod();
		request.setQuery(query);
		query.setGuid(guid);
		query.setStartDate(startDate);
		query.setEndDate(endDate);
		query.setLogbook(logbook);
		query.setPdf(pdf);
		query.setXml(xml);
		GetAttachmentsForGuidAndQueryPeriodResponse response = activityClient.sendRequest(request,GetAttachmentsForGuidAndQueryPeriodResponse.class);
		return Optional.ofNullable(response)
				.map(GetAttachmentsForGuidAndQueryPeriodResponse::getResponseLists)
				.map(responseAttachments -> responseAttachments.stream().map(this::toEmailAttachment).collect(Collectors.toList()))
				.orElseGet(Collections::emptyList);
	}

	private EmailAttachment toEmailAttachment(AttachmentResponseObject attachment) {
		return new EmailAttachment(attachment.getTripId(), attachment.getType().value(), attachment.getContent());
	}

	@Override
	public void forwardMultipleFaReports(long executionId, boolean newReportIds, String receiver, String dataflow, boolean consolidated, List<String> reportIds, boolean hasEmail, String assetGuid, boolean pdf, boolean xml) {
		ForwardMultipleFAReportsRequest request = new ForwardMultipleFAReportsRequest();
		request.setMethod(ActivityModuleMethod.FORWARD_MULTIPLE_FA_REPORTS);
		setForwardFAReportBaseRequestCommonFields(request, executionId, newReportIds, receiver, dataflow, consolidated, hasEmail, assetGuid, pdf, xml);
		request.setReportIds(reportIds.stream().map(this::toFluxReportIdentifier).collect(Collectors.toList()));
		activityClient.sendAsyncRequest(request);
	}

	private FluxReportIdentifier toFluxReportIdentifier(String id) {
		int index = id.indexOf(":");
		FluxReportIdentifier result = new FluxReportIdentifier();
		result.setSchemeId(id.substring(0,index));
		result.setId(id.substring(index + 1));
		return result;
	}

	@Override
	public void forwardFaReportWithLogbook(long executionId, boolean newReportIds, String receiver, String dataflow, boolean consolidated, List<String> tripIds, boolean hasEmail, String assetGuid, boolean pdf, boolean xml) {
		ForwardFAReportWithLogbookRequest request = new ForwardFAReportWithLogbookRequest();
		request.setMethod(ActivityModuleMethod.FORWARD_FA_REPORT_WITH_LOGBOOK);
		setForwardFAReportBaseRequestCommonFields(request, executionId, newReportIds, receiver, dataflow, consolidated, hasEmail, assetGuid, pdf, xml);
		request.setTripIds(tripIds);
		activityClient.sendAsyncRequest(request);
	}

	@Override
	public void forwardFaReportFromPosition(long executionId, boolean newReportIds, String receiver, String dataflow, boolean consolidated, boolean logbook, XMLGregorianCalendar startDate, XMLGregorianCalendar endDate, String assetGuid, String assetHistGuid,boolean hasEmail, boolean pdf, boolean xml) {
		ForwardFAReportFromPositionRequest request = new ForwardFAReportFromPositionRequest();
		request.setMethod(ActivityModuleMethod.FORWARD_FA_REPORT_FROM_POSITION);
		setForwardFAReportBaseRequestCommonFields(request, executionId, newReportIds, receiver, dataflow, consolidated, hasEmail, assetGuid, pdf, xml);
		request.setLogbook(logbook);
		request.setStartDate(startDate);
		request.setEndDate(endDate);
		request.setAssetHistoryGuid(assetHistGuid);
		activityClient.sendAsyncRequest(request);
	}

	private void setForwardFAReportBaseRequestCommonFields(ForwardFAReportBaseRequest request, long executionId, boolean newReportIds, String receiver, String dataflow, boolean consolidated, boolean hasEmail, String assetGuid, boolean pdf, boolean xml) {
		request.setExecutionId(executionId);
		request.setNewReportIds(newReportIds);
		request.setReceiver(receiver);
		request.setDataflow(dataflow);
		request.setConsolidated(consolidated);
		if (hasEmail) {
			EmailConfigForReportGeneration emailConfig = new EmailConfigForReportGeneration();
			emailConfig.setGuid(assetGuid);
			emailConfig.setPdf(pdf);
			emailConfig.setXml(xml);
			request.setEmailConfig(emailConfig);
		}
	}
}
