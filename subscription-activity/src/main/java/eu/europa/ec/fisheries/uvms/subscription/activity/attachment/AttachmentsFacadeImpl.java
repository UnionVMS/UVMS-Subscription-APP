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
package eu.europa.ec.fisheries.uvms.subscription.activity.attachment;

import eu.europa.ec.fisheries.uvms.activity.model.schemas.AttachmentResponseObject;
import eu.europa.ec.fisheries.uvms.subscription.activity.communication.ActivitySender;
import eu.europa.ec.fisheries.uvms.subscription.service.email.EmailAttachment;
import eu.europa.ec.fisheries.uvms.subscription.service.email.AttachmentsFacade;
import eu.europa.ec.fisheries.uvms.subscription.service.util.DateTimeService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class AttachmentsFacadeImpl implements AttachmentsFacade {

    private DateTimeService dateTimeService;
    private ActivitySender activitySender;

    @Inject
    public AttachmentsFacadeImpl(ActivitySender activitySender, DateTimeService dateTimeService){
        this.dateTimeService = dateTimeService;
        this.activitySender = activitySender;
    }

    /**
     * Constructor for frameworks.
     */
    @SuppressWarnings("unused")
    AttachmentsFacadeImpl() {
        // NOOP
    }

    @Override
    public List<EmailAttachment> findAttachmentsForGuidAndQueryPeriod(String guid, Date startDate, Date endDate, boolean pdf, boolean xml, boolean consolidated) {
        List<AttachmentResponseObject> responseObjects = activitySender.createAndSendRequestForAttachments(guid,dateTimeService.toXMLGregorianCalendar(startDate),dateTimeService.toXMLGregorianCalendar(endDate),pdf,xml);
        return responseObjects.stream().map(AttachmentsFacadeImpl::transform).collect(Collectors.toList());
    }

    private static EmailAttachment transform(AttachmentResponseObject source){
        return new EmailAttachment(source.getTripId(),source.getType().name(),source.getContent());
    }

}
