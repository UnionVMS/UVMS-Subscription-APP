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

import eu.europa.ec.fisheries.uvms.subscription.service.util.EmailUtils;
import lombok.SneakyThrows;
import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldSetup;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static eu.europa.ec.fisheries.uvms.subscription.service.email.EmailMocks.createLargeXmlAttachment;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnableAutoWeld
@ExtendWith(MockitoExtension.class)
public class Zip4jCompressorTest {

    private static final String DEFAULT_PASSWORD = "abc-123";

    @Inject
    @Zip4jCompressorQualifier
    private Compressor zip4jCompressor;

    @WeldSetup
    public WeldInitiator weld = WeldInitiator.from(Zip4jCompressor.class,
            Zip4jCompressorTest.class).build();

    @ParameterizedTest
    @ValueSource(strings = {DEFAULT_PASSWORD, "some_random_password"})
    @SneakyThrows
    public void testCompressingDecompressingWithPasswordWithZip4j(String password) {
        EmailAttachment emailAttachment1 = createLargeXmlAttachment("test12345");
        List<EmailAttachment> emailAttachmentList = new ArrayList<>();
        emailAttachmentList.add(emailAttachment1);
        String filename = EmailUtils.createAttachmentFileName(emailAttachment1.getTripId(), emailAttachment1.getType());

        byte[] initialContentBytes = emailAttachment1.getContent().getBytes();
        byte[] compressedContent = zip4jCompressor.compress(initialContentBytes, filename, DEFAULT_PASSWORD);
        assertTrue(compressedContent.length < initialContentBytes.length);

        InputStream is = new ByteArrayInputStream(compressedContent);
        if (!DEFAULT_PASSWORD.equals(password)) {
            assertThrows(net.lingala.zip4j.exception.ZipException.class, () -> zip4jCompressor.decompress(is, password, initialContentBytes.length), "Wrong password.");
            return;
        }
        byte[] initialContent = zip4jCompressor.decompress(is, password, initialContentBytes.length);
        assertEquals(emailAttachment1.getContent().trim(), new String(initialContent).trim());
    }

}
