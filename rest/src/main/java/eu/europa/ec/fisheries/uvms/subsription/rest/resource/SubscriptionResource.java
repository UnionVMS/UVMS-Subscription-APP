/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */

package eu.europa.ec.fisheries.uvms.subsription.rest.resource;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import eu.europa.ec.fisheries.uvms.commons.rest.resource.UnionVMSResource;
import eu.europa.ec.fisheries.uvms.commons.service.interceptor.ValidationInterceptor;
import eu.europa.ec.fisheries.uvms.subscription.service.bean.SubscriptionServiceBean;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.SubscriptionDto;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.SubscriptionListPayload;
import eu.europa.ec.fisheries.uvms.subsription.rest.filter.SubscriptionServiceExceptionHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @implicitParam roleName|string|header|true||||||
 * @implicitParam scopeName|string|header|true|EC|||||
 * @implicitParam authorization|string|header|true||||||jwt token
 */
@Path("/subscription")
@Stateless
@Slf4j
@Interceptors(SubscriptionServiceExceptionHandler.class)
public class SubscriptionResource extends UnionVMSResource {

    @HeaderParam("authorization")
    private String authorization;

    @HeaderParam("scopeName")
    private String scopeName;

    @HeaderParam("roleName")
    private String roleName;

    @Context
    private HttpServletRequest servletRequest;

    @EJB
    private SubscriptionServiceBean service;

    /**
     * Search for subscription matching the given criteria.
     *
     * @param filters criteria to list on
     * @return found subscription. An empty list when nothing found.
     */
    @POST
    @Consumes(value = {MediaType.APPLICATION_JSON})
    @Produces(value = {MediaType.APPLICATION_JSON})
    @Path("list")
   // @RequiresFeature(UnionVMSFeature.viewVesselsAndMobileTerminals) // TODO change permissions
    public Response list(final SubscriptionListPayload filters) {
        return createSuccessResponse(service.list(filters));
    }

    /**
     * Create new subscription.
     *
     * @param subscription subscription to create
     * @return subscription
     */
    @POST
    @Consumes(value = {MediaType.APPLICATION_JSON})
    @Produces(value = {MediaType.APPLICATION_JSON})
    //@RequiresFeature(UnionVMSFeature.viewVesselsAndMobileTerminals) // TODO change permissions
    @Interceptors(ValidationInterceptor.class)
    public Response create(final SubscriptionDto subscription) {
        return createSuccessResponse(service.create(subscription));
    }

    /**
     * Update subscription.
     *
     * @param subscription subscription to update
     * @return updated subscription
     */
    @PUT
    @Consumes(value = {MediaType.APPLICATION_JSON})
    @Produces(value = {MediaType.APPLICATION_JSON})
    //@RequiresFeature(UnionVMSFeature.viewVesselsAndMobileTerminals) // TODO change permissions
    public Response update(final SubscriptionDto subscription) {
        return createSuccessResponse(service.update(subscription));
    }

    @DELETE
    @Path("/{id}")
    @Produces(APPLICATION_JSON)
    //@RequiresFeature(UnionVMSFeature.viewVesselsAndMobileTerminals) // TODO change permissions
    @Interceptors(ValidationInterceptor.class)
    public Response deleteSubscription(@PathParam("id") @NotNull Long id) {
        service.delete(id);
        return createSuccessResponse();
    }
}