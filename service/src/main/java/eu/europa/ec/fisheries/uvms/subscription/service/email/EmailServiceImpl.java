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
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.fisheries.uvms.subscription.model.exceptions.EmailException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class EmailServiceImpl implements EmailService {

    private ParameterService parameterService;
    private SubscriptionDao subscriptionDAO;
    private EmailSender emailSender;

    @Inject
    public EmailServiceImpl(SubscriptionDao subscriptionDAO, ParameterService parameterService, EmailSender emailSender) {
        this.subscriptionDAO = subscriptionDAO;
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
    public void send(EmailData data) {
        String subject = findDefaultParamValue(ParameterKey.SUBSCRIPTION_EMAIL_DEFAULT_SUBJECT.getKey());
        String sender  = findDefaultParamValue(ParameterKey.SUBSCRIPTION_EMAIL_DEFAULT_SENDER.getKey());
        emailSender.send(subject, sender, data.getBody(), data.getMimeType(), data.getReceivers(), data.isZipAttachments(), data.getPassword(), data.getEmailAttachmentList());
    }

    @Override
    public String findEmailTemplateBodyValue() {
        return findDefaultParamValue(ParameterKey.SUBSCRIPTION_EMAIL_DEFAULT_BODY.getKey());
    }

    @Override
    public EmailBodyEntity findEmailBodyEntity(SubscriptionEntity subscription) {
        return subscriptionDAO.findEmailBodyEntity(subscription.getId());
    }

    private String findDefaultParamValue(String key) {
        try {
            return parameterService.getParamValueById(key);
        } catch (ConfigServiceException e) {
            throw new EmailException(e);
        }
    }
}
