/*
﻿Developed with the contribution of the European Commission - Directorate General for Maritime Affairs and Fisheries
© European Union, 2015-2016.

This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can
redistribute it and/or modify it under the terms of the GNU General Public License as published by the
Free Software Foundation, either version 3 of the License, or any later version. The IFDM Suite is distributed in
the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details. You should have received a
copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
*/
package eu.europa.ec.fisheries.uvms.subscription.service.email;

import eu.europa.fisheries.uvms.subscription.model.exceptions.EmailException;

import java.util.List;

public interface EmailSender {

    /**
     * Builds the mail and sends it
     *
     * @param subject        The email subject
     * @param sender         The email sender
     * @param body           The email text body
     * @param attachmentList The attachment list, to be compressed
     * @param password       The password to be used as protection on attachments
     * @param receivers      The list of receivers (list of emails)
     * @throws EmailException The exception thrown if building or sending not completed
     */
    void buildAndSend(String subject, String sender, String body, List<EmailAttachment> attachmentList, String password, List<String> receivers) throws EmailException;
}
