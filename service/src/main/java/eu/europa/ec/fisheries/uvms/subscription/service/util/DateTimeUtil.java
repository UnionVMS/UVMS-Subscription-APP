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
package eu.europa.ec.fisheries.uvms.subscription.service.util;


import java.time.ZonedDateTime;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class DateTimeUtil {

    /**
     * Converts given date to ZonedDateTime
     * @param date To be converted
     * @return The converted ZonedDateTime
     */
    public static ZonedDateTime convertDateToZonedDateTime(Date date) {
        GregorianCalendar calendarStartDate = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        calendarStartDate.setTime(date);
        return calendarStartDate.toZonedDateTime();
    }
}
