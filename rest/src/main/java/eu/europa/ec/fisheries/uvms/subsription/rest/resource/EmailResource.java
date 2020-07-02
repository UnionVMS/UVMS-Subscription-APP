/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subsription.rest.resource;

import eu.europa.ec.fisheries.uvms.commons.rest.resource.UnionVMSResource;
import eu.europa.ec.fisheries.uvms.subscription.service.email.EmailService;
import eu.europa.fisheries.uvms.subscription.model.exceptions.EmailException;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/email")
@ApplicationScoped
@Slf4j
public class EmailResource extends UnionVMSResource {

    @Inject
    private EmailService service;

    /**
     * Get subscription having given id.
     * @return @responseType eu.europa.ec.fisheries.uvms.subscription.service.dto.SubscriptionDto
     */
    @GET
    @Produces(value = {APPLICATION_JSON})
    @Path("/defaultBodyTemplate")
    public Response getDefaultBodyTemplate() throws EmailException {
        return createSuccessResponse(service.findEmailTemplateBodyValue());
    }
}
