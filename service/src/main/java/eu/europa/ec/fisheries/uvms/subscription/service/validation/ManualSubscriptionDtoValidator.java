/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.service.validation;

import eu.europa.ec.fisheries.uvms.subscription.service.dto.SubscriptionDto;
import eu.europa.fisheries.uvms.subscription.model.enums.TriggerType;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static eu.europa.ec.fisheries.uvms.subscription.service.validation.ValidationUtil.addViolationMessageToPath;
import static eu.europa.ec.fisheries.uvms.subscription.service.validation.ValidationUtil.require;
import static eu.europa.ec.fisheries.uvms.subscription.service.validation.ValidationUtil.requirePropertyNotNullWithMessage;

/**
 * Implementation of custom validator for SubscriptionDto with its execution TriggerType.MANUAL.
 */
public class ManualSubscriptionDtoValidator implements ConstraintValidator<ValidManualSubscriptionDto, SubscriptionDto> {

    private static final String ASSETS_PATH = "assets";

    @Override
    public boolean isValid(SubscriptionDto subscriptionDto, ConstraintValidatorContext context) {
        boolean valid = true;
        if (subscriptionDto != null && TriggerType.MANUAL == subscriptionDto.getExecution().getTriggerType()) {

            valid = requirePropertyNotNullWithMessage(context, subscriptionDto.getOutput(), "output", "Manual Subscription output cannot be null.");

            if(valid) {
                if(subscriptionDto.getOutput().getHistory() == null && subscriptionDto.getOutput().getHistoryUnit() == null) {
                    valid = requirePropertyNotNullWithMessage(context, subscriptionDto.getOutput().getQueryEndDate(), "output.queryEndDate", "Either duration or Query Period should have a value.");
                    valid &= requirePropertyNotNullWithMessage(context, subscriptionDto.getOutput().getQueryStartDate(), "output.queryStartDate", "Either duration or Query Period should have a value.");
                }
                else{
                    valid = requirePropertyNotNullWithMessage(context, subscriptionDto.getOutput().getHistory(), "output.history", "History is required");

                    if(valid && subscriptionDto.getOutput().getHistory() < 1){
                        valid = false;
                        addViolationMessageToPath(context, "History cannot be 0 or negative number" , new String[]{"output.history"});
                    }
                    valid &= requirePropertyNotNullWithMessage(context, subscriptionDto.getOutput().getHistoryUnit(), "output.historyUnit", "HistoryUnit is required");
                }
            }
            valid &= require(context, "Assets must be selected for manual subscriptions", subscriptionDto)
                    .path(ASSETS_PATH, SubscriptionDto::getAssets)
                    .toBe(assets -> !assets.isEmpty());
        }
        return valid;
    }
}
