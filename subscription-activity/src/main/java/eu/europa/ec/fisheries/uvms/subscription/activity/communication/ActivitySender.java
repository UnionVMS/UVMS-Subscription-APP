/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.activity.communication;

import javax.xml.datatype.XMLGregorianCalendar;
import java.util.List;

import eu.europa.ec.fisheries.uvms.activity.model.schemas.VesselIdentifierSchemeIdEnum;
import eu.europa.ec.fisheries.uvms.activity.model.schemas.VesselIdentifierType;
import eu.europa.ec.fisheries.uvms.subscription.service.email.EmailAttachment;

/**
 * Service for sending messages to Activity.
 */
public interface ActivitySender {
	/**
	 * Ask Activity to generate a FA Query with a vessel id.
	 *
	 * @param vesselIdentifiers
	 * @param consolidated
	 * @param startDate
	 * @param endDate
	 * @param receiver
	 * @param dataflow
	 * @return generated query id
	 */
	String createAndSendQueryForVessel(
			  List<VesselIdentifierType> vesselIdentifiers,
			  boolean consolidated,
			  XMLGregorianCalendar startDate,
			  XMLGregorianCalendar endDate,
			  String receiver,
			  String dataflow);

	/**
	 * Ask Activity to generate a FA Query with a trip id.
	 *
	 * @param tripId
	 * @param consolidated
	 * @param receiver
	 * @param dataflow
	 * @return generated query id
	 */
	String createAndSendQueryForTrip(
				String tripId,
				boolean consolidated,
				String receiver,
				String dataflow);

	/**
	 * Sends request to activity module for attachments.
	 *
	 * @param guid         The asset history GUID (connect id)
	 * @param startDate    The start date
	 * @param endDate      The end date
	 * @param logbook      The logbook flag
	 * @param pdf          The pdf flag
	 * @param xml          The xml flag
	 * @param consolidated The consolidated flag
	 * @return The attachments
	 */
	List<EmailAttachment> createAndSendRequestForAttachments(
			String guid,
			XMLGregorianCalendar startDate,
			XMLGregorianCalendar endDate,
			boolean logbook,
			boolean pdf,
			boolean xml,
			boolean consolidated
	);

	/**
	 * Requests Activities to forward a report and/or create email attachments for the given incoming report ids.
	 * This is essentially report forwarding, for the {@code logbook=false} case.
	 *
	 * @param executionId  The id of the {@code SubscriptionExecutionEntity}, needed to correlate the response
	 * @param newReportIds Instruct Activities to generate new ids for the reports
	 * @param receiver     The FLUX receiver, {@code null} to avoid sending an FA Report
	 * @param dataflow     The FLUX dataflow, {@code null} to avoid sending an FA Report
	 * @param consolidated The consolidated flag
	 * @param reportIds    The individual report ids to forward and/or generate email
	 * @param hasEmail     Instruct Activities to generate the email attachments
	 * @param assetGuid    The Asset GUID
	 * @param pdf          Instruct Activities to generate the PDF attachments
	 * @param xml          Instruct Activities to generate the XML (FA Report) attachments
	 */
	void forwardMultipleFaReports(long executionId, boolean newReportIds, String receiver, String dataflow, boolean consolidated, List<String> reportIds, boolean hasEmail, String assetGuid, boolean pdf, boolean xml, List<VesselIdentifierSchemeIdEnum> vesselIdentifiers);

	/**
	 * Requests Activities to forward complete logbooks and/or create email attachments for the given trip ids.
	 * This is essentially report generation, for the {@code logbook=true} case.
	 *
	 * @param executionId  The id of the {@code SubscriptionExecutionEntity}, needed to correlate the response
	 * @param newReportIds Instruct Activities to generate new ids for the reports
	 * @param receiver     The FLUX receiver, {@code null} to avoid sending an FA Report
	 * @param dataflow     The FLUX dataflow, {@code null} to avoid sending an FA Report
	 * @param consolidated The consolidated flag
	 * @param tripIds      The trip ids for which to generate output
	 * @param hasEmail     Instruct Activities to generate the email attachments
	 * @param assetGuid    The Asset GUID
	 * @param pdf          Instruct Activities to generate the PDF attachments
	 * @param xml          Instruct Activities to generate the XML (FA Report) attachments
	 */
	void forwardFaReportWithLogbook(long executionId, boolean newReportIds, String receiver, String dataflow, boolean consolidated, List<String> tripIds, boolean hasEmail, String assetGuid, boolean pdf, boolean xml, List<VesselIdentifierSchemeIdEnum> vesselIdentifiers);

	/**
	 * Requests Activities to forward a report and/or create email attachments for the given incoming movement.
	 *
	 * @param executionId  The id of the {@code SubscriptionExecutionEntity}, needed to correlate the response
	 * @param newReportIds Instruct Activities to generate new ids for the reports
	 * @param receiver     The FLUX receiver, {@code null} to avoid sending an FA Report
	 * @param dataflow     The FLUX dataflow, {@code null} to avoid sending an FA Report
	 * @param consolidated The consolidated flag
	 * @param logbook The logbook flag
	 * @param startDate The starting date of the period in which reports will be searched
	 * @param endDate The ending  date of the period in which reports will be searched
	 * @param assetHistGuid The Asset History GUID
	 * @param hasEmail Instruct Activities to generate the email attachments
	 * @param pdf Instruct Activities to generate the PDF attachments
	 * @param xml Instruct Activities to generate the XML (FA Report) attachments
	 */
	void forwardFaReportFromPosition(long executionId, boolean newReportIds, String receiver, String dataflow, boolean consolidated, boolean logbook, XMLGregorianCalendar startDate, XMLGregorianCalendar endDate, String assetGuid, String assetHistGuid, boolean hasEmail, boolean pdf, boolean xml, List<VesselIdentifierSchemeIdEnum> vesselIdentifiers);

	/**
	 * Asks activity module to map given report id list with its associated occurrence date
	 * @param reportIds The report id list
	 * @param assetGuid Asset guid
	 * @return List of movement guids
	 */
	List<String> findMovementGuidsByReportIdsAndAssetGuid(List<String> reportIds,String assetGuid);
}
