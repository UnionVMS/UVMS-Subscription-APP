/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */

package eu.europa.ec.fisheries.uvms.subsription.rest;

import eu.europa.ec.fisheries.uvms.commons.rest.filter.EncodingResponseFilter;
import eu.europa.ec.fisheries.uvms.subsription.rest.authentication.AuthenticationFilter;
import eu.europa.ec.fisheries.uvms.subsription.rest.exceptionmap.ConstraintViolationExceptionMapper;
import eu.europa.ec.fisheries.uvms.subsription.rest.exceptionmap.EmailExceptionMapper;
import eu.europa.ec.fisheries.uvms.subsription.rest.exceptionmap.EntityDoesNotExistExceptionMapper;
import eu.europa.ec.fisheries.uvms.subsription.rest.exceptionmap.NotAuthorisedExceptionMapper;
import eu.europa.ec.fisheries.uvms.subsription.rest.filter.SubscriptionServiceExceptionHandler;
import eu.europa.ec.fisheries.uvms.subsription.rest.resource.EmailResource;
import eu.europa.ec.fisheries.uvms.subsription.rest.resource.SubscriptionResource;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

@ApplicationPath("/rest")
public class SubscriptionActivator extends Application {

    private final Set<Object> singletons = new HashSet<>();
    private final Set<Class<?>> set = new HashSet<>();

    public SubscriptionActivator() {
        set.add(SubscriptionResource.class);
        set.add(EmailResource.class);
        set.add(EmailExceptionMapper.class);
        set.add(SubscriptionServiceExceptionHandler.class);
        set.add(EncodingResponseFilter.class);
        set.add(ConstraintViolationExceptionMapper.class);
        set.add(ObjectMapperContextResolver.class);
        set.add(AuthenticationFilter.class);
        set.add(NotAuthorisedExceptionMapper.class);
        set.add(EntityDoesNotExistExceptionMapper.class);
    }

    @Override
    public Set<Class<?>> getClasses() {
        return set;
    }

    @Override
    public Set<Object> getSingletons() {
        return singletons;
    }
}
