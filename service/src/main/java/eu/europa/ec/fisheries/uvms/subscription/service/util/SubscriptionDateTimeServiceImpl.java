/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.service.util;

import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionOutput;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.time.ZonedDateTime;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import static eu.europa.ec.fisheries.uvms.subscription.service.util.DateTimeUtil.convertDateToZonedDateTime;

/**
 * Implementation of {@link SubscriptionDateTimeService}.
 */
@ApplicationScoped
public class SubscriptionDateTimeServiceImpl implements SubscriptionDateTimeService {

	private DatatypeFactory datatypeFactory;
	private DateTimeService dateTimeService;

	@Inject
	public SubscriptionDateTimeServiceImpl(DateTimeService dateTimeService,DatatypeFactory datatypeFactory){
		this.dateTimeService = dateTimeService;
		this.datatypeFactory = datatypeFactory;
	}

	/**
	 * Constructor for frameworks.
	 */
	@SuppressWarnings("unused")
	SubscriptionDateTimeServiceImpl(){
		//NOOP
	}

	@Override
	public ZonedDateTime calculateStartDate(SubscriptionOutput output, ZonedDateTime endDate) {
		return output.getQueryPeriod() != null ?
				convertDateToZonedDateTime(output.getQueryPeriod().getStartDate()) : endDate.minus(output.getHistory(), output.getHistoryUnit().getTemporalUnit());
	}

	@Override
	public ZonedDateTime calculateEndDate(SubscriptionOutput output,String occurrence) {
		ZonedDateTime endDate;
		if (output.getQueryPeriod() != null) {
			endDate = convertDateToZonedDateTime(output.getQueryPeriod().getEndDate());
		}
		else{
			endDate = datatypeFactory.newXMLGregorianCalendar(occurrence).toGregorianCalendar().toZonedDateTime();
		}
		return endDate;
	}

	@Override
	public ZonedDateTime calculateEndDate(SubscriptionOutput output) {
		String occurrenceKeyData = null;
		if(output.getQueryPeriod() == null){
			GregorianCalendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
			calendar.setTime(dateTimeService.getNowAsDate());
			XMLGregorianCalendar xmlCalendar = datatypeFactory.newXMLGregorianCalendar(calendar);
			occurrenceKeyData = xmlCalendar.toXMLFormat();
		}
		return calculateEndDate(output,occurrenceKeyData);
	}

}
