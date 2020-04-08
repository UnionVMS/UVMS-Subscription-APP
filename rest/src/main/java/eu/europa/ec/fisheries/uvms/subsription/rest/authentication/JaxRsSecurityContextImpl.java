/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subsription.rest.authentication;

import javax.enterprise.inject.Vetoed;
import javax.ws.rs.core.SecurityContext;
import java.security.Principal;

import eu.europa.ec.fisheries.uvms.subscription.service.authentication.SubscriptionUser;

/**
 * Simple implementation of {@code javax.ws.rs.core.SecurityContext}.
 */
@Vetoed
class JaxRsSecurityContextImpl implements SecurityContext {

	private Principal principal;
	private boolean secure;
	private String authenticationScheme;

	public JaxRsSecurityContextImpl(Principal principal, boolean secure, String authenticationScheme) {
		this.principal = principal;
		this.secure = secure;
		this.authenticationScheme = authenticationScheme;
	}

	@Override
	public Principal getUserPrincipal() {
		return principal;
	}

	@Override
	public boolean isUserInRole(String role) {
		return principal instanceof SubscriptionUser && ((SubscriptionUser) principal).getRoles().contains(role);
	}

	@Override
	public boolean isSecure() {
		return secure;
	}

	@Override
	public String getAuthenticationScheme() {
		return authenticationScheme;
	}
}
