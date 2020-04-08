/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.service.authorisation;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import java.util.Optional;

import eu.europa.ec.fisheries.uvms.subscription.service.authentication.AuthenticationContext;
import eu.europa.ec.fisheries.uvms.subscription.service.authentication.SubscriptionUser;
import eu.europa.ec.fisheries.wsdl.subscription.module.SubscriptionFeaturesEnum;
import eu.europa.fisheries.uvms.subscription.model.exceptions.NotAuthorisedException;
import lombok.extern.slf4j.Slf4j;

/**
 * Implement the logic of {@code &064;AllowedRoles} to throw a {@link NotAuthorisedException} if there is no
 * current user or if the user does not possess at least one of the specified roles.
 */
@Slf4j
@Interceptor
@AllowedRoles
@Priority(Interceptor.Priority.APPLICATION)
class AllowedRolesInterceptor {

	@Inject
	private AuthenticationContext authenticationContext;

	@AroundInvoke
	Object interceptRequest(final InvocationContext ic) throws Exception {
		SubscriptionUser user = Optional.ofNullable(authenticationContext.getUserPrincipal()).orElseThrow(NotAuthorisedException::new);
		AllowedRoles allowedRolesAnnotation = Optional.ofNullable(ic.getMethod().getAnnotation(AllowedRoles.class))
				.orElseGet(() -> ic.getMethod().getDeclaringClass().getAnnotation(AllowedRoles.class));
		if (allowedRolesAnnotation == null ) {
			throw new IllegalStateException("Did not find @AllowedRoles in " + ic.getMethod().getName() + " declared in " + ic.getMethod().getDeclaringClass().getCanonicalName());
		}
		for (SubscriptionFeaturesEnum feature : allowedRolesAnnotation.value()) {
			if (user.getRoles().contains(feature.name())) {
				return ic.proceed();
			}
		}
		throw new NotAuthorisedException();
	}
}
