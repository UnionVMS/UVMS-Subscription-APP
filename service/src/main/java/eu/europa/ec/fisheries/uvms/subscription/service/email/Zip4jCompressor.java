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

import net.lingala.zip4j.io.inputstream.ZipInputStream;
import net.lingala.zip4j.io.outputstream.ZipOutputStream;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.AesKeyStrength;
import net.lingala.zip4j.model.enums.CompressionMethod;
import net.lingala.zip4j.model.enums.EncryptionMethod;

import javax.enterprise.context.ApplicationScoped;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


@ApplicationScoped
@Zip4jCompressorQualifier
public class Zip4jCompressor implements Compressor {

    /**
     * Constructor for frameworks.
     */
    @SuppressWarnings("unused")
    Zip4jCompressor() {
        // NOOP
    }

    @Override
    public byte[] compress(byte[] content, String filename, String password) throws IOException {

        ZipParameters zipParameters = buildEncryptionParameters();
        zipParameters.setFileNameInZip(filename);
        byte[] buff = new byte[1024];
        int readLen;

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            ZipOutputStream zos = new ZipOutputStream(byteArrayOutputStream, password.toCharArray());
            zos.putNextEntry(zipParameters);
            try (InputStream inputStream = new ByteArrayInputStream(content)) {
                while ((readLen = inputStream.read(buff)) != -1) {
                    zos.write(buff, 0, readLen);
                }
            }
            zos.closeEntry();
            zos.close();
            return byteArrayOutputStream.toByteArray();
        }
    }

    @Override
    public byte[] decompress(InputStream inputStream, String password, int unCompressedSize) throws IOException {
        int readLen;
        byte[] readBuffer = new byte[unCompressedSize];
        try (ZipInputStream zipInputStream = new ZipInputStream(inputStream, password.toCharArray())) {
            zipInputStream.getNextEntry();
            try (OutputStream outputStream = new ByteArrayOutputStream(unCompressedSize)) {
                while ((readLen = zipInputStream.read(readBuffer)) != -1) {
                    outputStream.write(readBuffer, 0, readLen);
                }
            }
        }
        return readBuffer;
    }

    private ZipParameters buildEncryptionParameters() {
        ZipParameters zipParameters = new ZipParameters();
        zipParameters.setCompressionMethod(CompressionMethod.DEFLATE);
        zipParameters.setEncryptionMethod(EncryptionMethod.AES);
        zipParameters.setAesKeyStrength(AesKeyStrength.KEY_STRENGTH_256);
        zipParameters.setEncryptFiles(true);
        return zipParameters;
    }
}
