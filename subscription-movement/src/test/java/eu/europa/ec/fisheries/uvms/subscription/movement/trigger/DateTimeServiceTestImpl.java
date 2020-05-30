/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.movement.trigger;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;

import eu.europa.ec.fisheries.uvms.subscription.service.util.DateTimeService;

/**
 * Test implementation of the {@link DateTimeService}.
 */
public class DateTimeServiceTestImpl implements DateTimeService {

	private LocalDateTime now;

	public void setNow(Date now) {
		this.now = LocalDateTime.ofInstant(now.toInstant(), ZoneOffset.UTC);
	}

	@Override
	public LocalDateTime getNow() {
		return now;
	}

	@Override
	public Date getNowAsDate() {
		return Date.from(now.toInstant(ZoneOffset.UTC));
	}

	@Override
	public Instant getNowAsInstant() {
		return now.toInstant(ZoneOffset.UTC);
	}

	@Override
	public LocalDate getToday() {
		return LocalDate.from(now);
	}

	@Override
	public long currentTimeMillis() {
		return now.toInstant(ZoneOffset.UTC).toEpochMilli();
	}
}
