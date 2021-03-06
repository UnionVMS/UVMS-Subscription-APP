/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */

package eu.europa.ec.fisheries.uvms.subsription.rest.resource;

import static eu.europa.ec.fisheries.uvms.rest.security.UnionVMSFeature.MANAGE_SUBSCRIPTION;
import static eu.europa.ec.fisheries.uvms.rest.security.UnionVMSFeature.VIEW_SUBSCRIPTION;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import eu.europa.ec.fisheries.uvms.commons.rest.resource.UnionVMSResource;
import eu.europa.ec.fisheries.uvms.commons.service.interceptor.ValidationInterceptor;
import eu.europa.ec.fisheries.uvms.rest.security.RequiresFeature;
import eu.europa.ec.fisheries.uvms.subscription.service.bean.SubscriptionServiceBean;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.SubscriptionDto;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.SubscriptionListQueryDto;
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
@SuppressWarnings("javadoc")
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
     * @param dto criteria to listSubscriptions on
     * @return @responseType eu.europa.ec.fisheries.uvms.subscription.service.dto.SubscriptionListResponseDto
     */
    @POST
    @Consumes(value = {APPLICATION_JSON})
    @Produces(value = {APPLICATION_JSON})
    @Path("list")
    @Interceptors(ValidationInterceptor.class)
    @RequiresFeature(VIEW_SUBSCRIPTION)
    public Response listSubscriptions(@NotNull SubscriptionListQueryDto dto) {
        return createSuccessResponse(service.listSubscriptions(dto.getQueryParameters(), dto.getPagination(), dto.getOrderBy(), scopeName, roleName,servletRequest.getRemoteUser()));
    }

    /**
     * Is subscription name unique
     *
     * @param name the name of the subscription
     *
     */
    @GET
    @Path("/{name}")
    @Produces(APPLICATION_JSON)
    @RequiresFeature(VIEW_SUBSCRIPTION)
    @Interceptors(ValidationInterceptor.class)
    public Response findByName(@NotNull @PathParam(value = "name") String name) {
        return createSuccessResponse(service.findSubscriptionByName(name));
    }

    /**
     * Create new subscription.
     *
     * @param subscription subscription to create
     * @return subscription
     */
    @POST
    @Consumes(value = {APPLICATION_JSON})
    @Produces(value = {APPLICATION_JSON})
    @RequiresFeature(MANAGE_SUBSCRIPTION)
    @Interceptors(ValidationInterceptor.class)
    public Response create(@NotNull SubscriptionDto subscription) {
        return createSuccessResponse(service.create(subscription, servletRequest.getRemoteUser()));
    }

    /**
     * Update subscription.
     *
     * @param subscription subscription to update
     * @return updated subscription
     */
    @PUT
    @Consumes(value = {APPLICATION_JSON})
    @Produces(value = {APPLICATION_JSON})
    @RequiresFeature(MANAGE_SUBSCRIPTION)
    @Interceptors(ValidationInterceptor.class)
    public Response update(@NotNull SubscriptionDto subscription) {
        return createSuccessResponse(service.update(subscription, servletRequest.getRemoteUser()));
    }

    /**
     * Delete subscription.
     *
     * @param id the subscription id
     *
     */
    @DELETE
    @Path("/{id}")
    @Produces(APPLICATION_JSON)
    @RequiresFeature(MANAGE_SUBSCRIPTION)
    @Interceptors(ValidationInterceptor.class)
    public Response delete(@NotNull @PathParam("id") Long id) {
        service.delete(id, servletRequest.getRemoteUser());
        return createSuccessResponse();
    }
}