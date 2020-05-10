/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.service.util;

import javax.enterprise.context.ApplicationScoped;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * Implementation of {@link DateTimeService}.
 */
@ApplicationScoped
class DateTimeServiceImpl implements DateTimeService {
	@Override
	public LocalDateTime getNow() {
		return LocalDateTime.now();
	}

	@Override
	public Date getNowAsDate() {
		return new Date();
	}

	@Override
	public Instant getNowAsInstant() {
		return Instant.now();
	}

	@Override
	public LocalDate getToday() {
		return LocalDate.now();
	}

	@Override
	public long currentTimeMillis() {
		return System.currentTimeMillis();
	}
}