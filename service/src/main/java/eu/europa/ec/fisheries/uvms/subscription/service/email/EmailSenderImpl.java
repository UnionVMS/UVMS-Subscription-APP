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

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;
import java.util.List;


@ApplicationScoped
public class EmailSenderImpl implements EmailSender {

    private EmailBuilder emailBuilder;
    private Session mailSession;

    @Inject
    public EmailSenderImpl(EmailBuilder emailBuilder) {
        this.emailBuilder = emailBuilder;
    }

    /**
     * Constructor for frameworks.
     */
    @SuppressWarnings("unused")
    EmailSenderImpl() {
        // NOOP
    }

    @Resource(name = "java:jboss/mail/Default")
    public void setMailSession(Session mailSession) {
        this.mailSession = mailSession;
    }

    @Override
    public void buildAndSend(String subject, String sender, String body, List<EmailAttachment> attachmentList, String password, List<String> receivers) throws EmailException {

        EmailBody emailBody = new EmailBody(body, "text/plain", attachmentList, password);
        Message message = emailBuilder.build(new MimeMessage(mailSession), subject, emailBody, sender, receivers.stream().toArray(String[]::new));
        try {
            Transport.send(message);
        } catch (MessagingException e) {
            throw new EmailException("Messaging error", e);
        }
    }
}
