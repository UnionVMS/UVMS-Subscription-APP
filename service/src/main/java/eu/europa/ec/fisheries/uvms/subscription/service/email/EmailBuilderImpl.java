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

import eu.europa.fisheries.uvms.subscription.model.exceptions.EmailException;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static eu.europa.ec.fisheries.uvms.subscription.service.util.EmailUtils.createAttachmentFileName;

@ApplicationScoped
public class EmailBuilderImpl implements EmailBuilder {

    private Compressor compressor;

    @Inject
    public EmailBuilderImpl(@Zip4jCompressorQualifier Compressor compressor) {
        this.compressor = compressor;
    }

    /**
     * Constructor for frameworks.
     */
    @SuppressWarnings("unused")
    EmailBuilderImpl() {
        // NOOP
    }

    @Override
    public Message build(Message message, String subject, EmailBody body, String sender, String... receivers) throws EmailException {
        validateInputs(message, body, sender, receivers);
        try {
            return Builder.init(message)
                    .subject(subject)
                    .content(body, compressor)
                    .withSender(sender)
                    .withReceivers(receivers)
                    .build();
        } catch (javax.mail.MessagingException e) {
            throw new EmailException(e);
        }
    }

    private void validateInputs(Message message,EmailBody body, String sender, String... receivers) throws EmailException{
        if(message == null) throw new EmailException("Message cannot be null");
        if(body == null) throw new EmailException("EmailBody cannot be null");
        if(sender == null) throw new EmailException("Sender cannot be null");
        if(receivers == null || receivers.length == 0) throw new EmailException("Receivers cannot be empty");
    }

    private static class Builder {
        private Message message;

        static Builder init(Message message) {
            Builder instance = new Builder();
            instance.message = message;
            return instance;
        }

        Builder subject(String subject) throws MessagingException {
            message.setSubject(subject);
            return this;
        }

        Builder content(EmailBody body, Compressor compressor) throws MessagingException {
            // Create the message part
            BodyPart messageBodyPart = new MimeBodyPart();
            // Now set the actual message
            messageBodyPart.setContent(body.getBody(), body.getMimeType());
            // Create a multipart message
            Multipart multipart = new MimeMultipart();
            // Set text message part
            multipart.addBodyPart(messageBodyPart);
            // handle attachments
            handleAttachments(multipart, body.getEmailAttachmentList(), body.getPassword(), compressor);
            // Set multipart as content
            message.setContent(multipart);
            return this;
        }

        void handleAttachments(Multipart multipart, List<EmailAttachment> emailAttachmentList, String password, Compressor compressor) throws EmailException, MessagingException {
            if (emailAttachmentList != null && !emailAttachmentList.isEmpty()) {
                for (EmailAttachment emailAttachment : emailAttachmentList) {
                    MimeBodyPart messageBodyPart = new MimeBodyPart();
                    String filename = createAttachmentFileName(emailAttachment.getTripId(), emailAttachment.getType().toLowerCase());
                    try {
                        byte[] compressed = compressor.compress(emailAttachment.getContent().getBytes(), filename, password);
                        DataSource source = new ByteArrayDataSource(compressed, "application/zip");
                        messageBodyPart.setDataHandler(new DataHandler(source));
                        messageBodyPart.setFileName(filename + ".zip");
                        multipart.addBodyPart(messageBodyPart);
                    } catch (IOException e) {
                        throw new EmailException("Error compressing attachments", e);
                    }
                }
            }
        }

        Builder withSender(String sender) throws MessagingException {
            return withSender(new InternetAddress(sender));
        }

        Builder withSender(InternetAddress senderAddress) throws MessagingException {
            message.setFrom(senderAddress);
            return this;
        }

        Builder withReceivers(String... receivers) throws MessagingException {
            Address[] to = Arrays.stream(receivers).map(toInternetAddress()).toArray(InternetAddress[]::new);
            message.setRecipients(Message.RecipientType.TO, to);
            return this;
        }

        Message build() throws MessagingException {
            message.setSentDate(new java.util.Date());
            return message;
        }

        static Function<String, InternetAddress> toInternetAddress() {
            return address -> {
                try {
                    return new InternetAddress(address);
                } catch (AddressException e) {
                    e.printStackTrace();
                    return null;
                }
            };
        }
    }

}
