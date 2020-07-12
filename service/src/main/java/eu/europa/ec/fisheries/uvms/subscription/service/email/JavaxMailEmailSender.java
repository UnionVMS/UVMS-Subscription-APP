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
import net.lingala.zip4j.io.outputstream.ZipOutputStream;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.AesKeyStrength;
import net.lingala.zip4j.model.enums.CompressionMethod;
import net.lingala.zip4j.model.enums.EncryptionMethod;
import org.apache.commons.io.input.ReaderInputStream;
import org.apache.commons.lang3.StringUtils;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

/**
 * Javax mail implementation of the email sender.
 */
@ApplicationScoped
class JavaxMailEmailSender implements EmailSender {

    private Session mailSession;

    @Inject
    public JavaxMailEmailSender(Session mailSession) {
        this.mailSession = mailSession;
    }

    /**
     * Constructor for frameworks.
     */
    @SuppressWarnings("unused")
    JavaxMailEmailSender() {
        // NOOP
    }

    @Override
    public void send(String subject, String sender, String body, String mimeType, List<String> receivers, boolean zipAttachments, String password, List<EmailAttachment> attachmentList) {
        validateInputs(subject, sender, body, receivers);
        MimeMessage message = new MimeMessage(mailSession);
        try {
            message.setSubject(subject);
            message.setFrom(toInternetAddress(sender));
            Multipart multipart = new MimeMultipart();
            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setContent(body, mimeType);
            multipart.addBodyPart(messageBodyPart);
            Address[] to = receivers.stream().map(this::toInternetAddress).toArray(InternetAddress[]::new);
            message.setRecipients(Message.RecipientType.TO, to);
            handleAttachments(multipart, zipAttachments, password, attachmentList);
            message.setContent(multipart);
            Transport.send(message);
        } catch (MessagingException e) {
            throw new EmailException(e);
        }
    }

    private void validateInputs(String subject, String sender, String body, List<String> receivers) {
        if (subject == null) {
            throw new EmailException("EmailBody cannot be null");
        }
        if (sender == null) {
            throw new EmailException("Sender cannot be null");
        }
        if (body == null) {
            throw new EmailException("Body cannot be null");
        }
        if (receivers == null || receivers.isEmpty()) {
            throw new EmailException("Receivers cannot be empty");
        }
    }

    private void handleAttachments(Multipart multipart, boolean zipAttachments, String password, List<EmailAttachment> attachmentList) {
        if (!attachmentList.isEmpty()) {
            if (zipAttachments) {
                handleZippedAttachments(multipart, password, attachmentList);
            } else {
                handleUnzippedAttachments(multipart, attachmentList);
            }
        }
    }

    private void handleUnzippedAttachments(Multipart multipart, List<EmailAttachment> attachmentList) {
        attachmentList.forEach(attachment -> {
            try {
                MimeBodyPart messageBodyPart = new MimeBodyPart();
                String filename = makeFileName(attachment);
                DataSource source = new Base64DataSource(filename, "application/" + attachment.getType().toLowerCase(), attachment.getContent());
                messageBodyPart.setDataHandler(new DataHandler(source));
                messageBodyPart.setFileName(filename);
                multipart.addBodyPart(messageBodyPart);
            } catch (MessagingException e) {
                throw new EmailException("Error attaching content", e);
            }
        });
    }

    private void handleZippedAttachments(Multipart multipart, String password, List<EmailAttachment> attachmentList) {
        try (
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ZipOutputStream zos = makeZipOutputStream(baos, password)
        ) {
            ZipParameters zipParameters = buildZipParameters(StringUtils.isNotEmpty(password));
            // TODO: This is very BAD for memory; we must find another solution for attachments!
            for (EmailAttachment attachment : attachmentList) {
                String filename = makeFileName(attachment);
                zipParameters.setFileNameInZip(filename);
                zos.putNextEntry(zipParameters);
                pipe(attachment, zos);
                zos.closeEntry();
            }
            MimeBodyPart messageBodyPart = new MimeBodyPart();
            DataSource source = new ByteArrayDataSource(baos.toByteArray(), "application/zip");
            messageBodyPart.setDataHandler(new DataHandler(source));
            messageBodyPart.setFileName("attachments.zip");
            multipart.addBodyPart(messageBodyPart);
        } catch (MessagingException | IOException e) {
            throw new EmailException("Error attaching content", e);
        }
    }

    private String makeFileName(EmailAttachment attachment) {
        return attachment.getTripId().replace(':', '_') + '.' + attachment.getType().toLowerCase();
    }

    private ZipOutputStream makeZipOutputStream(ByteArrayOutputStream baos, String password) throws IOException {
        return StringUtils.isNotEmpty(password) ? new ZipOutputStream(baos, password.toCharArray()) : new ZipOutputStream(baos);
    }

    private ZipParameters buildZipParameters(boolean encrypt) {
        ZipParameters zipParameters = new ZipParameters();
        zipParameters.setCompressionMethod(CompressionMethod.DEFLATE);
        if (encrypt) {
            zipParameters.setEncryptionMethod(EncryptionMethod.AES);
            zipParameters.setAesKeyStrength(AesKeyStrength.KEY_STRENGTH_256);
            zipParameters.setEncryptFiles(true);
        }
        return zipParameters;
    }

    private void pipe(EmailAttachment attachment, OutputStream os) throws IOException {
        try (InputStream in = forAttachment(attachment)) {
            byte[] buf = new byte[8192];
            int r;
            while ((r = in.read(buf, 0, buf.length)) >= 0) {
                os.write(buf, 0, r);
            }
        }
    }

    private InputStream forAttachment(EmailAttachment attachment) {
        if ("PDF".equalsIgnoreCase(attachment.getType())) {
            return Base64.getDecoder().wrap(new ReaderInputStream(new StringReader(attachment.getContent()), StandardCharsets.UTF_8));
        } else {
            return new ReaderInputStream(new StringReader(attachment.getContent()), StandardCharsets.UTF_8);
        }
    }

    private InternetAddress toInternetAddress(String receiver) {
        try {
            return new InternetAddress(receiver);
        } catch (AddressException e) {
            throw new EmailException(e);
        }
    }

    private static class Base64DataSource implements DataSource {
        private final String name;
        private final String contentType;
        private final InputStream stream;

        Base64DataSource(String name, String contentType, String base64EncodedContent) {
            this.name = name;
            this.contentType = contentType;
            stream = Base64.getDecoder().wrap(new ByteArrayInputStream(base64EncodedContent.getBytes()));
        }

        @Override
        public InputStream getInputStream() {
            return stream;
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            throw new IOException("cannot do this");
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public String getName() {
            return name;
        }
    }
}
