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
package eu.europa.ec.fisheries.uvms.subscription.service.email;


import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionExecutionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.execution.SubscriptionExecutor;
import eu.europa.fisheries.uvms.subscription.model.exceptions.EmailException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * Implementation of {@link SubscriptionExecutor} for sending emails.
 */
@ApplicationScoped
public class EmailTriggerSubscriptionExecutor implements SubscriptionExecutor {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EmailTriggerSubscriptionExecutor.class);

    private EmailService emailService;

    @Inject
    public EmailTriggerSubscriptionExecutor(EmailService emailService) {
        this.emailService = emailService;
    }

    /**
     * Constructor for frameworks.
     */
    @SuppressWarnings("unused")
    EmailTriggerSubscriptionExecutor() {
        // NOOP
    }

    @Override
    public void execute(SubscriptionExecutionEntity execution) {

        if (!Boolean.TRUE.equals(execution.getTriggeredSubscription().getSubscription().getOutput().getHasEmail())) {
            return;
        }
        try {
            emailService.prepareAndSendEmail(execution);
        } catch (EmailException e) {
            log.error("Error sending email [" + e.getMessage() + "]");
        }
    }

}
