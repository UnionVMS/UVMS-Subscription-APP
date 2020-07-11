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
package eu.europa.ec.fisheries.uvms.subscription.activity.execution;

import static java.lang.Boolean.TRUE;

import eu.europa.ec.fisheries.uvms.subscription.activity.communication.ActivitySender;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionExecutionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.email.EmailAttachment;
import eu.europa.ec.fisheries.uvms.subscription.service.email.EmailData;
import eu.europa.ec.fisheries.uvms.subscription.service.email.EmailService;
import eu.europa.ec.fisheries.uvms.subscription.service.execution.SubscriptionExecutor;
import eu.europa.ec.fisheries.uvms.subscription.service.messaging.asset.AssetSender;
import eu.europa.ec.fisheries.uvms.subscription.service.trigger.TriggeredSubscriptionDataUtil;
import eu.europa.ec.fisheries.uvms.subscription.service.util.SubscriptionDateTimeService;
import eu.europa.ec.fisheries.wsdl.asset.types.VesselIdentifiersHolder;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.xml.datatype.DatatypeFactory;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Implementation of {@link SubscriptionExecutor} for sending emails.
 */
@ApplicationScoped
public class EmailSubscriptionExecutor implements SubscriptionExecutor {

    private EmailService emailService;
    private ActivitySender activitySender;
    private AssetSender assetSender;
    private SubscriptionDateTimeService subscriptionDateTimeService;
    private DatatypeFactory datatypeFactory;

    @Inject
    public EmailSubscriptionExecutor(EmailService emailService, ActivitySender activitySender, AssetSender assetSender, SubscriptionDateTimeService subscriptionDateTimeService, DatatypeFactory datatypeFactory) {
        this.emailService = emailService;
        this.activitySender = activitySender;
        this.assetSender = assetSender;
        this.subscriptionDateTimeService = subscriptionDateTimeService;
        this.datatypeFactory = datatypeFactory;
    }

    /**
     * Constructor for frameworks.
     */
    @SuppressWarnings("unused")
    EmailSubscriptionExecutor() {
        // NOOP
    }

    @Override
    public void execute(SubscriptionExecutionEntity execution) {
        TriggeredSubscriptionEntity triggeredSubscription = execution.getTriggeredSubscription();
        SubscriptionEntity subscription = triggeredSubscription.getSubscription();
        if (TRUE.equals(subscription.getOutput().getHasEmail())) {
            EmailData emailData = new EmailData();
            emailData.setBody(emailService.findEmailBodyEntity(subscription).getBody());
            emailData.setMimeType("text/plain");
            emailData.setReceivers(subscription.getOutput().getEmails());
            emailData.setZipAttachments(subscription.getOutput().getEmailConfiguration().getZipAttachments());
            emailData.setPassword(new String(Base64.getDecoder().decode(subscription.getOutput().getEmailConfiguration().getPassword())));
            Map<String, String> dataMap = toDataMap(triggeredSubscription);
            String assetGuid = extractAssetGuid(triggeredSubscription.getId(), dataMap);
            ZonedDateTime endDate = calculateEndDate(triggeredSubscription, dataMap);
            ZonedDateTime startDate = calculateStartDate(triggeredSubscription, endDate);
            List<EmailAttachment> attachments = activitySender.createAndSendRequestForAttachments(
                    assetGuid,
                    datatypeFactory.newXMLGregorianCalendar(GregorianCalendar.from(startDate)),
                    datatypeFactory.newXMLGregorianCalendar(GregorianCalendar.from(endDate.plus(1L, ChronoUnit.DAYS))),
                    TRUE.equals(subscription.getOutput().getLogbook()),
                    TRUE.equals(subscription.getOutput().getEmailConfiguration().getIsPdf()),
                    TRUE.equals(subscription.getOutput().getEmailConfiguration().getIsXml()),
                    TRUE.equals(subscription.getOutput().getConsolidated())
            );
            emailData.setEmailAttachmentList(attachments);
            emailService.send(emailData);
        }
    }

    private String extractAssetGuid(Long triggeredSubscriptionId, Map<String, String> dataMap) {
        String connectId = dataMap.get(TriggeredSubscriptionDataUtil.KEY_CONNECT_ID);
        Objects.requireNonNull(connectId, "connectId not found in data of " + triggeredSubscriptionId);
        VesselIdentifiersHolder vesselIdentifiers = assetSender.findVesselIdentifiers(connectId);
        Objects.requireNonNull(vesselIdentifiers.getAssetGuid(), "asset GUID null for connectId " + connectId + " of " + triggeredSubscriptionId);
        return vesselIdentifiers.getAssetGuid();
    }

    private ZonedDateTime calculateEndDate(TriggeredSubscriptionEntity triggeredSubscription, Map<String, String> dataMap) {
        // TODO Verify logic!!!!!
        String occurrence = null;
        if (triggeredSubscription.getSubscription().getOutput().getQueryPeriod() == null) {
            occurrence = dataMap.get(TriggeredSubscriptionDataUtil.KEY_OCCURRENCE);
            Objects.requireNonNull(occurrence, "occurrence not found in data of " + triggeredSubscription.getId());
        }
        return subscriptionDateTimeService.calculateEndDate(triggeredSubscription.getSubscription().getOutput(),occurrence);
    }

    private ZonedDateTime calculateStartDate(TriggeredSubscriptionEntity triggeredSubscription, ZonedDateTime endDate) {
        // TODO Verify logic!!!!!
        return subscriptionDateTimeService.calculateStartDate(triggeredSubscription.getSubscription().getOutput(),endDate);
    }
}
