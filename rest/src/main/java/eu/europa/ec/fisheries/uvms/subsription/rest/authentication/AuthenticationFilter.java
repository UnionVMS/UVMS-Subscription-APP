/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subsription.rest.authentication;

import static javax.ws.rs.Priorities.AUTHENTICATION;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;
import java.security.Principal;
import java.util.Optional;
import java.util.Set;

import eu.europa.ec.fisheries.uvms.constants.AuthConstants;

/**
 * Extract the user information from the request and set the JAX-RS {@code SecurityContext}
 * as well as the {@link eu.europa.ec.fisheries.uvms.subscription.service.authentication.AuthenticationContext}
 * of this application.
 */
@Provider
@Priority(AUTHENTICATION)
public class AuthenticationFilter implements ContainerRequestFilter {

	@Inject
	private AuthenticationContextImpl authenticationContext;

	@Override
	public void filter(ContainerRequestContext requestContext) {
		Optional.ofNullable(requestContext.getSecurityContext()).ifPresent(securityContext -> {
			SubscriptionUserImpl user = Optional.ofNullable(securityContext.getUserPrincipal())
					.map(Principal::getName)
					.map(username -> {
						@SuppressWarnings("unchecked")
						Set<String> roles = (Set<String>) requestContext.getProperty(AuthConstants.HTTP_REQUEST_ROLES_ATTRIBUTE);
						return new SubscriptionUserImpl(username,roles);
					})
					.orElse(null);
			authenticationContext.setUserPrincipal(user);
			requestContext.setSecurityContext(new JaxRsSecurityContextImpl(
					user,
					requestContext.getSecurityContext().isSecure(),
					requestContext.getSecurityContext().getAuthenticationScheme()
			));
		});
	}
}
