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

import eu.europa.ec.fisheries.uvms.activity.model.schemas.ActivityModuleMethod;
import eu.europa.ec.fisheries.uvms.activity.model.schemas.AttachmentResponseObject;
import eu.europa.ec.fisheries.uvms.activity.model.schemas.CreateAndSendFAQueryForTripRequest;
import eu.europa.ec.fisheries.uvms.activity.model.schemas.CreateAndSendFAQueryForVesselRequest;
import eu.europa.ec.fisheries.uvms.activity.model.schemas.CreateAndSendFAQueryResponse;
import eu.europa.ec.fisheries.uvms.activity.model.schemas.GetAttachmentsForGuidAndQueryPeriod;
import eu.europa.ec.fisheries.uvms.activity.model.schemas.GetAttachmentsForGuidAndQueryPeriodRequest;
import eu.europa.ec.fisheries.uvms.activity.model.schemas.GetAttachmentsForGuidAndQueryPeriodResponse;
import eu.europa.ec.fisheries.uvms.activity.model.schemas.PluginType;
import eu.europa.ec.fisheries.uvms.activity.model.schemas.VesselIdentifierType;

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
	public List<AttachmentResponseObject> createAndSendRequestForAttachments(String guid, XMLGregorianCalendar startDate, XMLGregorianCalendar endDate, boolean pdf, boolean xml) {
		GetAttachmentsForGuidAndQueryPeriodRequest request = new GetAttachmentsForGuidAndQueryPeriodRequest();
		request.setMethod(ActivityModuleMethod.FIND_ATTACHMENTS_FOR_GUID_AND_QUERY_PERIOD);
		GetAttachmentsForGuidAndQueryPeriod query = new GetAttachmentsForGuidAndQueryPeriod();
		request.setQuery(query);
		query.setGuid(guid);
		query.setStartDate(startDate);
		query.setEndDate(endDate);
		query.setPdf(pdf);
		query.setXml(xml);
		GetAttachmentsForGuidAndQueryPeriodResponse response = activityClient.sendRequest(request,GetAttachmentsForGuidAndQueryPeriodResponse.class);
		return response != null ? response.getResponseLists() : Collections.emptyList();
	}
}
