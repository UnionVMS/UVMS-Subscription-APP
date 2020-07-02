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

import eu.europa.ec.fisheries.uvms.activity.model.schemas.AttachmentResponseObject;
import eu.europa.ec.fisheries.uvms.activity.model.schemas.VesselIdentifierType;

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
	 * Sends request to activity module for attachments
	 * @param guid
	 * @param startDate
	 * @param endDate
	 * @param pdf
	 * @param xml
	 * @return
	 */
	List<AttachmentResponseObject> createAndSendRequestForAttachments(String guid,
																	  XMLGregorianCalendar startDate,
																	  XMLGregorianCalendar endDate,
																	  boolean pdf,boolean xml);
}
