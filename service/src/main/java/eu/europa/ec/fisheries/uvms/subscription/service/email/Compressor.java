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

import java.io.IOException;
import java.io.InputStream;

public interface Compressor {
    /**
     * Compress content protected with given password
     * @param content The initial content
     * @param filename The file name
     * @param password The password to be used as protection
     * @return The password protected compressed content
     * @throws IOException If compression fails
     */
    byte[] compress(byte[] content,String filename,String password) throws IOException;

    /**
     * Decompresses given content, if it is compressed with that password
     * @param inputStream The password protected content InputStream
     * @param password The password to be used for decompression
     * @param unCompressedSize The initial size of file, need to be stored before
     * @return The initial uncompressed content
     * @throws IOException If decompression fails
     */
    byte[] decompress(InputStream inputStream, String password, int unCompressedSize) throws IOException;
}
