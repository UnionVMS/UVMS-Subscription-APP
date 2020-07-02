/*
 *
 *  Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2020.
 *
 *  This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 *  and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 *  the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 *  details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package eu.europa.ec.fisheries.uvms.subscription.service.email;

import eu.europa.ec.fisheries.uvms.config.exception.ConfigServiceException;
import eu.europa.ec.fisheries.uvms.config.service.ParameterService;
import eu.europa.ec.fisheries.uvms.subscription.service.config.ParameterKey;
import eu.europa.ec.fisheries.uvms.subscription.service.dao.SubscriptionDao;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.EmailBodyEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEmailConfiguration;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionExecutionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionOutput;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionDataEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.util.DateTimeUtil;
import eu.europa.fisheries.uvms.subscription.model.exceptions.EmailException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

import static eu.europa.ec.fisheries.uvms.subscription.service.util.DateTimeUtil.convertDateToZonedDateTime;

@ApplicationScoped
public class EmailServiceImpl implements EmailService {

    private static final String KEY_CONNECT_ID = "connectId";

    private ParameterService parameterService;
    private SubscriptionDao subscriptionDAO;
    private AttachmentsFacade attachmentsFacade;
    private EmailSender emailSender;

    @Inject
    public EmailServiceImpl(SubscriptionDao subscriptionDAO, AttachmentsFacade attachmentsFacade, ParameterService parameterService, EmailSender emailSender) {
        this.subscriptionDAO = subscriptionDAO;
        this.attachmentsFacade = attachmentsFacade;
        this.parameterService = parameterService;
        this.emailSender = emailSender;
    }

    /**
     * Constructor for frameworks.
     */
    @SuppressWarnings("unused")
    EmailServiceImpl() {
        // NOOP
    }


    @Override
    public void prepareAndSendEmail(SubscriptionExecutionEntity execution) {
        String subject = findDefaultParamValue(ParameterKey.SUBSCRIPTION_EMAIL_DEFAULT_SUBJECT.getKey());
        String sender  = findDefaultParamValue(ParameterKey.SUBSCRIPTION_EMAIL_DEFAULT_SENDER.getKey());

        SubscriptionEntity subscriptionEntity = execution.getTriggeredSubscription().getSubscription();
        SubscriptionOutput subscriptionOutput = subscriptionEntity.getOutput();
        SubscriptionEmailConfiguration emailConfiguration = subscriptionOutput.getEmailConfiguration();

        EmailBodyEntity emailBodyEntity = subscriptionDAO.findEmailBodyEntity(subscriptionEntity.getId());
        List<String> receivers = subscriptionOutput.getEmails();
        String connectId = execution.getTriggeredSubscription()
                .getData().stream()
                .filter(tsd -> KEY_CONNECT_ID.equals(tsd.getKey()))
                .map(TriggeredSubscriptionDataEntity::getValue)
                .findFirst().orElseThrow(() -> new EmailException("Not found connect id to fetch attachments from activity"));

        List<EmailAttachment> emailAttachmentList = null;
        if (Boolean.TRUE.equals(emailConfiguration.getHasAttachments())) {
            Date endDate = new Date();
            ZonedDateTime startDate = subscriptionOutput.getQueryPeriod() != null ? convertDateToZonedDateTime(subscriptionOutput.getQueryPeriod().getStartDate()) :
                    DateTimeUtil.convertDateToZonedDateTime(endDate).minus(subscriptionOutput.getHistory(), subscriptionOutput.getHistoryUnit().getTemporalUnit());
            // if null default true
            boolean consolidated = !Boolean.FALSE.equals(subscriptionOutput.getConsolidated());
            emailAttachmentList = attachmentsFacade.findAttachmentsForGuidAndQueryPeriod(connectId, Date.from(startDate.toInstant()), endDate, emailConfiguration.getIsPdf(), emailConfiguration.getIsXml(), consolidated);
        }
        // build mail
        emailSender.buildAndSend(subject, sender, emailBodyEntity.getBody(), emailAttachmentList, emailConfiguration.getPassword(), receivers);
    }

    @Override
    public String findEmailTemplateBodyValue() {
        return findDefaultParamValue(ParameterKey.SUBSCRIPTION_EMAIL_DEFAULT_BODY.getKey());
    }

    private String findDefaultParamValue(String key) {
        try {
            return parameterService.getParamValueById(key);
        } catch (ConfigServiceException e) {
            throw new EmailException(e);
        }
    }
}
