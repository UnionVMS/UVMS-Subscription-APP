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

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import eu.europa.ec.fisheries.uvms.commons.rest.resource.UnionVMSResource;
import eu.europa.ec.fisheries.uvms.subscription.service.bean.SubscriptionService;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.SubscriptionDto;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.search.SubscriptionListQueryImpl;
import eu.europa.ec.fisheries.uvms.subsription.rest.IUserRoleInterceptor;
import eu.europa.ec.fisheries.wsdl.subscription.module.SubscriptionFeaturesEnum;
import lombok.extern.slf4j.Slf4j;


@Path("/subscription")
@ApplicationScoped
@Slf4j
public class SubscriptionResource extends UnionVMSResource {

    @Inject
    private SubscriptionService service;

    /**
     * Search for subscription matching the given criteria.
     *
     * @param queryParams criteria to listSubscriptions on
     * @return @responseType eu.europa.ec.fisheries.uvms.subscription.service.dto.list.SubscriptionListResponseDto
     */
    @POST
    @Consumes(value = {APPLICATION_JSON})
    @Produces(value = {APPLICATION_JSON})
    @Path("list")
    @IUserRoleInterceptor(requiredUserRole = {SubscriptionFeaturesEnum.VIEW_SUBSCRIPTION})
    public Response listSubscriptions(@Context HttpServletRequest request,
                                      @Valid SubscriptionListQueryImpl queryParams,
                                      @HeaderParam("scopeName") String scopeName,
                                      @HeaderParam("roleName") String roleName) {
        return createSuccessResponse(service.listSubscriptions(queryParams, scopeName, roleName, request.getRemoteUser()));
    }

    /**
     * Is subscription name unique
     *
     * @param name the name of the subscription
     *
     */
    @GET
    @Path("/exists")
    @Produces(APPLICATION_JSON)
    @IUserRoleInterceptor(requiredUserRole = {SubscriptionFeaturesEnum.VIEW_SUBSCRIPTION})
    public Response exists(@Context HttpServletRequest request, @QueryParam("name") String name) {
        return createSuccessResponse(service.valueExists(name));
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
    @IUserRoleInterceptor(requiredUserRole = {SubscriptionFeaturesEnum.MANAGE_SUBSCRIPTION})
    public Response create(@Context HttpServletRequest request, @Valid @NotNull SubscriptionDto subscription) {
        return createSuccessResponse(service.create(subscription, request.getRemoteUser()));
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
    @IUserRoleInterceptor(requiredUserRole = {SubscriptionFeaturesEnum.MANAGE_SUBSCRIPTION})
    public Response update(@Context HttpServletRequest request, @Valid @NotNull SubscriptionDto subscription) {
        return createSuccessResponse(service.update(subscription, request.getRemoteUser()));
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
    @IUserRoleInterceptor(requiredUserRole = {SubscriptionFeaturesEnum.MANAGE_SUBSCRIPTION})
    public Response delete(@Context HttpServletRequest request, @Valid @NotNull @PathParam("id") Long id) {
        service.delete(id, request.getRemoteUser());
        return createSuccessResponse();
    }
}
