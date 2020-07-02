/*
 *
 *  Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2020.
 *
 *  This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 *  and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 *  the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 *  details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package eu.europa.ec.fisheries.uvms.subscription.service.email;

import java.util.Date;
import java.util.List;

public interface AttachmentsFacade {

    /**
     * This method returns a list of AttachmentDto
     * @param guid The AssetHistory guid or connectId
     * @param startDate The start period date
     * @param endDate The end period date
     * @param pdf Whether required attachment is pdf type
     * @param xml Whether required attachment is xml type
     * @param consolidated
     * @return List of AttachmentDto
     */
    List<EmailAttachment> findAttachmentsForGuidAndQueryPeriod(String guid, Date startDate, Date endDate, boolean pdf, boolean xml, boolean consolidated);
}
