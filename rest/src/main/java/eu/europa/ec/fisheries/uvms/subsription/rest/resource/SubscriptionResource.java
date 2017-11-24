/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */

package eu.europa.ec.fisheries.uvms.subsription.rest.resource;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.List;

import eu.europa.ec.fisheries.uvms.rest.security.RequiresFeature;
import eu.europa.ec.fisheries.uvms.rest.security.UnionVMSFeature;
import eu.europa.ec.fisheries.uvms.subscription.service.bean.SubscriptionServiceBean;
import eu.europa.ec.fisheries.uvms.subsription.rest.dto.request.SubscriptionListRequestDTO;
import eu.europa.ec.fisheries.uvms.subsription.rest.dto.respond.ResponseCode;
import eu.europa.ec.fisheries.uvms.subsription.rest.dto.respond.ResponseDto;
import eu.europa.ec.fisheries.wsdl.subscription.module.Subscription;
import lombok.extern.slf4j.Slf4j;

@Path("/subscription")
@Stateless
@Slf4j
public class SubscriptionResource {

    @EJB
    private SubscriptionServiceBean service;

    @Context
    private HttpServletRequest servletRequest;

    @POST
    @Consumes(value = { MediaType.APPLICATION_JSON })
    @Produces(value = { MediaType.APPLICATION_JSON })
    @Path("list")
    @RequiresFeature(UnionVMSFeature.viewVesselsAndMobileTerminals)
    public ResponseDto getSubscriptionList(final SubscriptionListRequestDTO request) {
        try {

            List<Subscription> subscription = service.listSubscriptions(null);
            return new ResponseDto(subscription, ResponseCode.OK);
        } catch (Exception e) {
            log.error("[ Error when getting subscription list. ] {} {}", e.getLocalizedMessage(), e.getStackTrace());
            return new ResponseDto(e.getMessage(), ResponseCode.ERROR);
        }
    }

}