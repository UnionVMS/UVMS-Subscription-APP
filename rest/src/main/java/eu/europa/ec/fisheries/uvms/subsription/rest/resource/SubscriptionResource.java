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
import javax.validation.Valid;
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
import javax.ws.rs.core.Response;

import eu.europa.ec.fisheries.uvms.commons.rest.resource.UnionVMSResource;
import eu.europa.ec.fisheries.uvms.subscription.service.bean.SubscriptionService;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.SubscriptionDto;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.search.SubscriptionListQueryImpl;
import eu.europa.ec.fisheries.uvms.subscription.service.readiness.ReadinessCheck;
import lombok.extern.slf4j.Slf4j;


@Path("/subscription")
@ApplicationScoped
@ReadinessCheck
@Slf4j
public class SubscriptionResource extends UnionVMSResource {

    @Inject
    private SubscriptionService service;

    /**
     * Get subscription having given id.
     *
     * @param id the subscription id
     * @return @responseType eu.europa.ec.fisheries.uvms.subscription.service.dto.SubscriptionDto
     */
    @GET
    @Produces(value = {APPLICATION_JSON})
    @Path("/{id}")
    public Response get(@PathParam("id") Long id) {
        return createSuccessResponse(service.findById(id));
    }

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
    public Response listSubscriptions(@Valid SubscriptionListQueryImpl queryParams,
                                      @HeaderParam("scopeName") String scopeName,
                                      @HeaderParam("roleName") String roleName) {
        return createSuccessResponse(service.listSubscriptions(queryParams, scopeName, roleName));
    }

    /**
     * Is subscription name unique
     *
     * @param name the name of the subscription
     *
     */
    @GET
    @Path("/available")
    @Produces(APPLICATION_JSON)
    public Response available(@QueryParam("name") String name, @QueryParam("id") Long id) {
        return createSuccessResponse(service.checkNameAvailability(name, id));
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
    public Response create(SubscriptionDto subscription) {
        return createSuccessResponse(service.create(subscription));
    }

    /**
     * Update subscription.
     *
     * @param id the subscription id
     * @param subscription subscription to update
     * @return updated subscription
     */
    @PUT
    @Path("/{id}")
    @Consumes(value = {APPLICATION_JSON})
    @Produces(value = {APPLICATION_JSON})
    public Response update(@PathParam("id") Long id, SubscriptionDto subscription) {
        subscription.setId(id);
        return createSuccessResponse(service.update(subscription));
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
    public Response delete(@PathParam("id") Long id) {
        service.delete(id);
        return createSuccessResponse();
    }

    /**
     * Create a new subscription of manual trigger type.
     *
     * @param subscription subscription to create
     * @return subscription
     */
    @POST
    @Consumes(value = {APPLICATION_JSON})
    @Produces(value = {APPLICATION_JSON})
    @Path("/create-manual")
    public Response createManual(SubscriptionDto subscription) {
        SubscriptionDto manualSubscriptionDto = service.prepareManualRequest(subscription);
        return createSuccessResponse(service.createManual(manualSubscriptionDto));
    }
}
