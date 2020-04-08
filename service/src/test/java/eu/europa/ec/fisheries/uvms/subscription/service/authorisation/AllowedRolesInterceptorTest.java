/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.service.authorisation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Collections;

import eu.europa.ec.fisheries.uvms.subscription.service.authentication.AuthenticationContext;
import eu.europa.ec.fisheries.uvms.subscription.service.authentication.SubscriptionUser;
import eu.europa.ec.fisheries.wsdl.subscription.module.SubscriptionFeaturesEnum;
import eu.europa.fisheries.uvms.subscription.model.exceptions.NotAuthorisedException;
import org.jboss.weld.junit5.auto.AddBeanClasses;
import org.jboss.weld.junit5.auto.AddEnabledInterceptors;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for the {@link AllowedRolesInterceptor}.
 */
@EnableAutoWeld
@AddEnabledInterceptors(AllowedRolesInterceptor.class)
@AddBeanClasses({AllowedRolesInterceptorTest.DummyBean.class, AllowedRolesInterceptorTest.DummyAuthenticationContext.class, AllowedRolesInterceptorTest.DummyBeanWithAllowedRolesInClass.class})
@ExtendWith(MockitoExtension.class)
public class AllowedRolesInterceptorTest {

	private static final String USERNAME = "username";
	private static final String SUCCESS_MESSAGE = "SUCCESS!";

	@Inject
	private DummyBean dummy;

	@Inject
	private DummyBeanWithAllowedRolesInClass dummyBeanWithAllowedRolesInClass;

	@Inject
	private DummyAuthenticationContext dummyAuthenticationContext;

	@Test
	void testNotAuthenticated() {
		dummyAuthenticationContext.unauthenticate();
		try {
			dummy.methodThatRequiresView();
			fail("should not be authorised");
		}
		catch (NotAuthorisedException nae) {
			// expected
		}
	}

	@Test
	void testNotAuthorised() {
		try {
			dummy.methodThatRequiresManage();
			fail("should not be authorised");
		}
		catch (NotAuthorisedException nae) {
			// expected
		}
		try {
			dummyBeanWithAllowedRolesInClass.methodThatRequiresManage();
			fail("should not be authorised");
		}
		catch (NotAuthorisedException nae) {
			// expected
		}
	}

	@Test
	void testAuthorised() {
		String result = dummy.methodThatRequiresView();
		assertEquals(SUCCESS_MESSAGE, result);
	}

	@ApplicationScoped
	public static class DummyBean {
		@AllowedRoles(SubscriptionFeaturesEnum.MANAGE_SUBSCRIPTION)
		public void methodThatRequiresManage() {
			// NOOP
		}

		@AllowedRoles(SubscriptionFeaturesEnum.VIEW_SUBSCRIPTION)
		public String methodThatRequiresView() {
			return SUCCESS_MESSAGE;
		}
	}

	@ApplicationScoped
	@AllowedRoles(SubscriptionFeaturesEnum.MANAGE_SUBSCRIPTION)
	public static class DummyBeanWithAllowedRolesInClass {
		public void methodThatRequiresManage() {
			// NOOP
		}
	}

	@ApplicationScoped
	public static class DummyAuthenticationContext implements AuthenticationContext {

		private SubscriptionUser user;

		public DummyAuthenticationContext() {
			user = mock(SubscriptionUser.class);
			lenient().when(user.getUserName()).thenReturn(USERNAME);
			lenient().when(user.getName()).thenReturn(USERNAME);
			lenient().when(user.getRoles()).thenReturn(Collections.singleton(SubscriptionFeaturesEnum.VIEW_SUBSCRIPTION.value()));
		}

		public void unauthenticate() {
			user = null;
		}

		@Override
		public SubscriptionUser getUserPrincipal() {
			return user;
		}
	}
}
