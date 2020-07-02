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
import lombok.SneakyThrows;
import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldSetup;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.inject.Inject;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static eu.europa.ec.fisheries.uvms.subscription.service.email.EmailMocks.createXmlAttachment;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@EnableAutoWeld
@ExtendWith(MockitoExtension.class)
public class EmailBuilderImplTest {

    private static final String DEFAULT_PASSWORD = "abc-123";

    @Inject
    private EmailBuilderImpl emailBuilder;

    @Inject
    @Zip4jCompressorQualifier
    private Compressor zip4jCompressor;

    private Session session;

    @WeldSetup
    public WeldInitiator weld = WeldInitiator.from(EmailBuilderImpl.class, Zip4jCompressor.class,
            EmailBuilderImplTest.class).bindResource("MailDefault", "java:jboss/mail/Default").build();

    @Before
    @SneakyThrows
    public void setupMockSessionAndTransport() {
        Properties sessionProperties = new Properties();
        sessionProperties.put("mail.smtp.host", "localhost");
        sessionProperties.put("mail.smtp.port", 25);
        session = Session.getInstance(sessionProperties);
    }

    @Test
    @SneakyThrows
    public void testSuccessfullyBuildingEmail() {
        EmailAttachment emailAttachment1 = createXmlAttachment("test12345");
        List<EmailAttachment> emailAttachmentList = new ArrayList<>();
        emailAttachmentList.add(emailAttachment1);
        EmailBody emailBody = new EmailBody("TEXT CONTENT", "text/plain", emailAttachmentList, DEFAULT_PASSWORD);
        List<String> receivers = Arrays.asList("leontios.sotiriadis@sword-group.com","test@test-test-test.com");
        assertDoesNotThrow(() -> emailBuilder.build(new MimeMessage(session), "Test subject", emailBody, "leonidas.sot@gmail.com", receivers.stream().toArray(String[]::new)), "Wrong password.");
    }
    @Test
    @SneakyThrows
    public void testBuildingEmailWithWrongInputs() {
        EmailAttachment emailAttachment1 = createXmlAttachment("test12345");
        List<EmailAttachment> emailAttachmentList = new ArrayList<>();
        emailAttachmentList.add(emailAttachment1);
        EmailBody emailBody = new EmailBody("TEXT CONTENT", "text/plain", emailAttachmentList, DEFAULT_PASSWORD);
        List<String> receivers = Arrays.asList("leontios.sotiriadis@sword-group.com","test@test-test-test.com");
        assertThrows(EmailException.class,() -> emailBuilder.build(new MimeMessage(session), "Test subject", emailBody, null, receivers.stream().toArray(String[]::new)));
        assertThrows(EmailException.class,() -> emailBuilder.build(new MimeMessage(session), "Test subject", emailBody, "leonidas.sot@gmail.com"));
        assertThrows(EmailException.class,() -> emailBuilder.build(new MimeMessage(session), "Test subject", null, "leonidas.sot@gmail.com",receivers.stream().toArray(String[]::new)));
        assertThrows(EmailException.class,() -> emailBuilder.build(null, "Test subject", emailBody, "leonidas.sot@gmail.com",receivers.stream().toArray(String[]::new)));
    }

}
