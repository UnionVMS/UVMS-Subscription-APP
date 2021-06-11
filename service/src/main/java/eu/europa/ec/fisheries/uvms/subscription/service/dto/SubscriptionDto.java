/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */

package eu.europa.ec.fisheries.uvms.subscription.service.dto;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING;
import static eu.europa.ec.fisheries.uvms.commons.date.DateUtils.DATE_TIME_UI_FORMAT;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import eu.europa.ec.fisheries.uvms.subscription.service.validation.SubscriptionHasUniqueName;
import eu.europa.ec.fisheries.uvms.subscription.service.validation.ValidManualSubscriptionDto;
import eu.europa.ec.fisheries.uvms.subscription.service.validation.ValidScheduledSubscriptionDto;
import eu.europa.ec.fisheries.uvms.subscription.service.validation.ValidSubscriptionDtoExecution;
import eu.europa.ec.fisheries.uvms.subscription.service.validation.ValidSubscriptionDtoOutput;
import eu.europa.fisheries.uvms.subscription.model.enums.SubscriptionTimeUnit;
import lombok.Data;

/**
 * Configuration of a subscription data object.
 */
@Data
@SubscriptionHasUniqueName
@ValidManualSubscriptionDto
@ValidScheduledSubscriptionDto
@ValidSubscriptionDtoOutput
@ValidSubscriptionDtoExecution
public class SubscriptionDto {

    private Long id;

    @NotEmpty
    private String name;

    private String description;

    @NotNull
    private Boolean active;

    @NotNull
    @Valid
    private SubscriptionOutputDto output;

    @Valid
    private SubscriptionExecutionDto execution;

    @JsonFormat(shape = STRING, pattern = DATE_TIME_UI_FORMAT)
    public Date startDate;

    @JsonFormat(shape = STRING, pattern = DATE_TIME_UI_FORMAT)
    public Date endDate;

    @Valid
    private Set<AreaDto> areas;

    @Valid
    private Set<AssetDto> assets;

    private Integer deadline;

    private SubscriptionTimeUnit deadlineUnit;

    private boolean stopWhenQuitArea;

    @Valid
    private Set<SubscriptionFishingActivityDto> startActivities;

    @Valid
    private Set<SubscriptionFishingActivityDto> stopActivities;

    @Valid
    private Set<SubscriptionSubscriberDto> senders;
}
