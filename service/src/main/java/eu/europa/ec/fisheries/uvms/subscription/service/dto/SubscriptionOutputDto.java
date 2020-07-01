/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.service.dto;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import eu.europa.ec.fisheries.uvms.subscription.service.validation.HasValidSubscriberDto;
import eu.europa.fisheries.uvms.subscription.model.enums.SubscriptionTimeUnit;
import eu.europa.fisheries.uvms.subscription.model.enums.OutgoingMessageType;
import eu.europa.fisheries.uvms.subscription.model.enums.SubscriptionVesselIdentifier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING;
import static eu.europa.ec.fisheries.uvms.commons.date.DateUtils.DATE_TIME_UI_FORMAT;

/**
 * Configuration of the output of a subscription.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@HasValidSubscriberDto
public class SubscriptionOutputDto {

	private boolean alert;

	private List<String> emails = new ArrayList<>();

	@NotNull
	private Boolean hasEmail;

	@Valid
	private SubscriptionEmailConfigurationDto emailConfiguration;

	@NotNull
	private OutgoingMessageType messageType;

	@Valid
	private SubscriptionSubscriberDto subscriber;

	private Boolean logbook;

	private Boolean consolidated;

	private EnumSet<SubscriptionVesselIdentifier> vesselIds = EnumSet.noneOf(SubscriptionVesselIdentifier.class);

	private Boolean generateNewReportId;

	@Min(1)
	private Integer history;

	private SubscriptionTimeUnit historyUnit;

	@JsonFormat(shape = STRING, pattern = DATE_TIME_UI_FORMAT)
	private Date queryStartDate;

	@JsonFormat(shape = STRING, pattern = DATE_TIME_UI_FORMAT)
	private Date queryEndDate;
}
