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

import com.fasterxml.jackson.annotation.JsonFormat;
import eu.europa.ec.fisheries.uvms.subscription.service.validation.SubscriptionHasUniqueName;
import eu.europa.fisheries.uvms.subscription.model.enums.AccessibilityType;
import eu.europa.ec.fisheries.uvms.subscription.service.validation.ValidSubscriptionExecutionDto;
import eu.europa.ec.fisheries.uvms.subscription.service.validation.ValidSubscriptionOutputDto;
import lombok.Data;

/**
 * Configuration of a subscription data object.
 */
@Data
@SubscriptionHasUniqueName
public class SubscriptionDto {

    private Long id;

    @NotEmpty
    private String name;

    private AccessibilityType accessibility;

    private String description;

    @NotNull
    private Boolean active;

    @NotNull
    @Valid
    @ValidSubscriptionOutputDto
    private SubscriptionOutputDto output;

    @Valid
    @ValidSubscriptionExecutionDto
    private SubscriptionExecutionDto execution;

    //private List<Object> conditions = new ArrayList<>();

    //private List<Object> areas = new ArrayList<>();

    @JsonFormat(shape = STRING, pattern = DATE_TIME_UI_FORMAT)
    public Date startDate;

    @JsonFormat(shape = STRING, pattern = DATE_TIME_UI_FORMAT)
    public Date endDate;
}
