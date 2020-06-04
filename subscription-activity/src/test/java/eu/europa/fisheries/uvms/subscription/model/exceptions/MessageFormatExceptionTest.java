/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.fisheries.uvms.subscription.model.exceptions;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test for class {@link MessageFormatException}
 */
class MessageFormatExceptionTest {

    @Test
    public void testThrowException() {
        Assertions.assertThrows(MessageFormatException.class, () -> { throw new MessageFormatException(); });
    }

    @Test
    public void testThrowExceptionWithMessageAndCause() {
        Assertions.assertThrows(MessageFormatException.class, () -> { throw new MessageFormatException("message", new Throwable()); });
    }

    @Test
    public void testThrowExceptionWithMessage() {
        Assertions.assertThrows(MessageFormatException.class, () -> { throw new MessageFormatException("message"); });
    }

    @Test
    public void testThrowExceptionWithCause() {
        Assertions.assertThrows(MessageFormatException.class, () -> { throw new MessageFormatException(new Throwable()); });
    }
}