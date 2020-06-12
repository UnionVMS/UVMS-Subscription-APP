/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.service.validation;

import static eu.europa.ec.fisheries.uvms.subscription.service.validation.ValidationUtil.requirePropertyNotNullWithMessage;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import eu.europa.ec.fisheries.uvms.subscription.service.dto.SubscriptionDto;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.SubscriptionOutputDto;
import eu.europa.fisheries.uvms.subscription.model.enums.OutgoingMessageType;
import eu.europa.fisheries.uvms.subscription.model.enums.TriggerType;

/**
 * Implementation of custom validator for SubscriptionOutputDto.
 */
public class SubscriptionDtoOutputValidator implements ConstraintValidator<ValidSubscriptionDtoOutput, SubscriptionDto> {

    @Override
    public boolean isValid(SubscriptionDto subscriptionDto, ConstraintValidatorContext context) {
        boolean valid = true;
        if (subscriptionDto != null && subscriptionDto.getOutput() != null) {
            SubscriptionOutputDto output = subscriptionDto.getOutput();

            if(output.getMessageType() == OutgoingMessageType.FA_QUERY || output.getMessageType() == OutgoingMessageType.FA_REPORT) {
                valid = requirePropertyNotNullWithMessage(context, output.getLogbook(), "output.logbook", "Logbook is required");
                valid &= requirePropertyNotNullWithMessage(context, output.getConsolidated(), "output.consolidated", "Consolidated is required");

                if(TriggerType.MANUAL != subscriptionDto.getExecution().getTriggerType()) {
                    valid &= requirePropertyNotNullWithMessage(context, output.getHistory(), "output.history", "History is required");
                    valid &= requirePropertyNotNullWithMessage(context, output.getHistoryUnit(), "output.historyUnit", "History unit is required");
                }
            }
            if (Boolean.TRUE.equals(output.getHasEmail())) {
                valid &= requirePropertyNotNullWithMessage(context, output.getEmailConfiguration(), "output.emailConfiguration", "EmailConfiguration is required");
            }
        }
        return valid;
    }
}
