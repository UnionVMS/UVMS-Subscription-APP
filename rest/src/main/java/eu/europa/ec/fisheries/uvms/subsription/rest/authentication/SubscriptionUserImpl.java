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
import java.util.Set;

import eu.europa.ec.fisheries.uvms.subscription.service.authentication.SubscriptionUser;

/**
 * A concrete implementation of a user for the Subscriptions module.
 */
@Vetoed
class SubscriptionUserImpl implements SubscriptionUser {

	private String userName;
	private Set<String> roles;

	/**
	 * Create an instance using all members.
	 *
	 * @param userName The username
	 * @param roles The roles
	 */
	public SubscriptionUserImpl(String userName, Set<String> roles) {
		this.userName = userName;
		this.roles = roles;
	}

	@Override
	public String getUserName() {
		return userName;
	}

	@Override
	public Set<String> getRoles() {
		return roles;
	}

	@Override
	public String getName() {
		return userName;
	}
}
