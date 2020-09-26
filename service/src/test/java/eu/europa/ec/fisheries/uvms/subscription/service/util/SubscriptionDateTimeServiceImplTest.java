/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.service.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.xml.datatype.DatatypeFactory;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import eu.europa.ec.fisheries.uvms.commons.domain.DateRange;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionOutput;
import eu.europa.fisheries.uvms.subscription.model.enums.SubscriptionTimeUnit;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link SubscriptionDateTimeServiceImpl}.
 */
@EnableAutoWeld
class SubscriptionDateTimeServiceImplTest {

	@Inject
	private SubscriptionDateTimeServiceImpl sut;
	
	@Produces
	private DateTimeService dateTimeService = new DateTimeServiceImpl();
	
	@Produces
	DatatypeFactory getDatatypeFactory() throws Exception {
		return DatatypeFactory.newInstance();
	}

	@Test
	void testCalculateStartDateWithQueryPeriod() {
		SubscriptionOutput output = new SubscriptionOutput();
		DateRange queryPeriod = new DateRange(d("20200416"), null);
		output.setQueryPeriod(queryPeriod);
		ZonedDateTime endDate = DateTimeUtil.convertDateToZonedDateTime(d("20200517"));
		ZonedDateTime result = sut.calculateStartDate(output, endDate);
		assertEquals(2020, result.getYear());
		assertEquals(Month.APRIL, result.getMonth());
		assertEquals(16, result.getDayOfMonth());
	}

	@Test
	void testCalculateStartDateWithoutQueryPeriod() {
		SubscriptionOutput output = new SubscriptionOutput();
		output.setHistory(10);
		output.setHistoryUnit(SubscriptionTimeUnit.DAYS);
		ZonedDateTime endDate = DateTimeUtil.convertDateToZonedDateTime(d("20200517"));
		ZonedDateTime result = sut.calculateStartDate(output, endDate);
		assertEquals(2020, result.getYear());
		assertEquals(Month.MAY, result.getMonth());
		assertEquals(7, result.getDayOfMonth());
	}

	@Test
	void testCalculateEndDateWithQueryPeriod() {
		SubscriptionOutput output = new SubscriptionOutput();
		DateRange queryPeriod = new DateRange(null, d("20200416"));
		output.setQueryPeriod(queryPeriod);
		ZonedDateTime result = sut.calculateEndDate(output, "2020-05-17T13:24:35Z");
		assertEquals(2020, result.getYear());
		assertEquals(Month.APRIL, result.getMonth());
		assertEquals(16, result.getDayOfMonth());
	}

	@Test
	void testCalculateEndDateWithoutQueryPeriodAndOccurrence() {
		ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));
		ZonedDateTime result = sut.calculateEndDate(new SubscriptionOutput());
		assertEquals(now.getYear(), result.getYear());
		assertEquals(now.getMonth(), result.getMonth());
		assertEquals(now.getDayOfMonth(), result.getDayOfMonth());
	}
	
	@Test
	void testCalculateEndDateWithoutQueryPeriod() {
		SubscriptionOutput output = new SubscriptionOutput();
		ZonedDateTime result = sut.calculateEndDate(output, "2020-05-17T13:24:35Z");
		assertEquals(2020, result.getYear());
		assertEquals(Month.MAY, result.getMonth());
		assertEquals(17, result.getDayOfMonth());
	}

	static Date d(String s) {
		return Date.from(ZonedDateTime.parse(s + " 00:00:00", DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss").withZone(ZoneId.of("UTC"))).toInstant());
	}
}
