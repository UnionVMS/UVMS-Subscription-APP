package eu.europa.ec.fisheries.uvms.subscription.service.email

import eu.europa.ec.fisheries.uvms.config.exception.ConfigServiceException
import eu.europa.ec.fisheries.uvms.config.service.ParameterService
import eu.europa.ec.fisheries.uvms.subscription.service.config.ParameterKey
import eu.europa.ec.fisheries.uvms.subscription.service.dao.SubscriptionDao
import eu.europa.ec.fisheries.uvms.subscription.service.domain.EmailBodyEntity
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity
import eu.europa.fisheries.uvms.subscription.model.exceptions.EmailException
import spock.lang.Specification

class EmailServiceImplSpec extends Specification {

    private ParameterService parameterService
    private SubscriptionDao subscriptionDao
    private EmailSender emailSender

    private EmailService emailService

    void setup() {
        parameterService = Mock(ParameterService)
        subscriptionDao = Mock(SubscriptionDao)
        emailSender = Mock(EmailSender)
        emailService = new EmailServiceImpl(subscriptionDao, parameterService, emailSender)
    }

    def "Send"() {
        given:
            EmailData data = new EmailData(
                    body: "email-body",
                    receivers: ["email1", "email2"],
                    mimeType: "text/plain",
                    emailAttachmentList: [Mock(EmailAttachment)],
                    password: "pass",
                    zipAttachments: true)

            String subjectKey = ParameterKey.SUBSCRIPTION_EMAIL_DEFAULT_SUBJECT.getKey()
            String templateSubject = "an-email-subject-template"

            String senderKey = ParameterKey.SUBSCRIPTION_EMAIL_DEFAULT_SENDER.getKey()
            String sender = "a-sender-email-addr"

        when:
            emailService.send(data)

        then:
            1 * parameterService.getParamValueById(subjectKey) >> templateSubject
            1 * parameterService.getParamValueById(senderKey) >> sender
            1 * emailSender.send(templateSubject, sender, data.body, data.mimeType, data.receivers, data.zipAttachments, data.password, data.emailAttachmentList)

        and:
            notThrown()
    }

    def "FindEmailTemplateBodyValue"() {
        given:
            String parameterKey = ParameterKey.SUBSCRIPTION_EMAIL_DEFAULT_BODY.getKey()
            String templateBody = "an-email-body-template"

        when:
            String result = emailService.findEmailTemplateBodyValue()

        then:
            1 * parameterService.getParamValueById(parameterKey) >> templateBody

        and:
            result == templateBody
    }

    def "FindEmailTemplateBodyValue throws exception"() {
        given:
            String parameterKey = ParameterKey.SUBSCRIPTION_EMAIL_DEFAULT_BODY.getKey()

        when:
            emailService.findEmailTemplateBodyValue()

        then:
            1 * parameterService.getParamValueById(parameterKey) >> { throw new ConfigServiceException("error") }

        and:
            thrown(EmailException)
    }

    def "FindEmailBodyEntity"() {
        given:
            long subscriptionId = 1234L
            SubscriptionEntity subscriptionEntity = new SubscriptionEntity(id: subscriptionId)
            EmailBodyEntity emailBodyEntity = Mock(EmailBodyEntity)

        when:
            EmailBodyEntity result = emailService.findEmailBodyEntity(subscriptionEntity)

        then:
            1 * subscriptionDao.findEmailBodyEntity(subscriptionId) >> emailBodyEntity

        and:
            result == emailBodyEntity
    }

    def "Test empty constructor"() {
        when:
            emailService = new EmailServiceImpl()
        then:
            notThrown()
    }
}
