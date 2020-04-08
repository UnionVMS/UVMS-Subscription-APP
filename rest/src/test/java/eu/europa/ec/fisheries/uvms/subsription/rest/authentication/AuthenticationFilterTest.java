package eu.europa.ec.fisheries.uvms.subsription.rest.authentication;

import static javax.ws.rs.Priorities.AUTHENTICATION;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;
import java.io.StringReader;
import java.security.Principal;
import java.util.Collections;
import java.util.Set;

import eu.europa.ec.fisheries.uvms.constants.AuthConstants;
import eu.europa.ec.fisheries.uvms.subscription.service.authentication.AuthenticationContext;
import eu.europa.ec.fisheries.uvms.subscription.service.authentication.SubscriptionUser;
import eu.europa.ec.fisheries.uvms.subsription.rest.ObjectMapperContextResolver;
import eu.europa.ec.fisheries.uvms.subsription.rest.exceptionmap.CustomMockDispatcherFactory;
import org.jboss.resteasy.cdi.ResteasyCdiExtension;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.jboss.resteasy.plugins.server.resourcefactory.SingletonResource;
import org.jboss.resteasy.spi.Dispatcher;
import org.jboss.resteasy.spi.metadata.DefaultResourceClass;
import org.jboss.weld.junit5.auto.ActivateScopes;
import org.jboss.weld.junit5.auto.AddBeanClasses;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Testing the {@link AuthenticationFilter}.
 */
@EnableAutoWeld
@AddBeanClasses({AuthenticationFilter.class, AuthenticationFilterTest.SpyResource.class, AuthenticationFilterTest.FakeAuthenticator.class, AuthenticationContextImpl.class})
@AddExtensions(ResteasyCdiExtension.class)
@ActivateScopes({RequestScoped.class})
public class AuthenticationFilterTest {

	private static final String USERNAME = "user";
	private static final String ROLE = "role";

	private Dispatcher dispatcher;

	private MockHttpResponse response;

	@Inject
	private SpyResource spyResource;

	@Inject
	private FakeAuthenticator fakeAuthenticator;

	@BeforeEach
	void init() {
		dispatcher = CustomMockDispatcherFactory.createDispatcher(ObjectMapperContextResolver.class, AuthenticationFilter.class, FakeAuthenticator.class);
		SingletonResource resourceFactory = new SingletonResource(spyResource, new DefaultResourceClass(SpyResource.class, null));
		dispatcher.getRegistry().addResourceFactory(resourceFactory, "/");
		response = new MockHttpResponse();
	}

	@Test
	void testNotAuthenticated() throws Exception {
		fakeAuthenticator.setData(null, null);
		MockHttpRequest request = MockHttpRequest.get("/dummy");
		dispatcher.invoke(request, response);
		assertEquals(204, response.getStatus()); // 204 is NO CONTENT, i.e. null
	}

	@Test
	void testAuthenticated() throws Exception {
		fakeAuthenticator.setData(USERNAME, Collections.singleton(ROLE));
		MockHttpRequest request = MockHttpRequest.get("/dummy");
		dispatcher.invoke(request, response);
		assertEquals(200, response.getStatus());
		JsonReader jsonReader = Json.createReader(new StringReader(response.getContentAsString()));
		JsonObject jobj = jsonReader.readObject();
		assertEquals(USERNAME, jobj.getString("userName"));
		JsonArray roles = jobj.getJsonArray("roles");
		assertEquals(1, roles.size());
		assertEquals(ROLE, roles.getString(0));
	}

	@Path("dummy")
	public static class SpyResource {

		@Inject
		private AuthenticationContext authenticationContext;

		@GET
		@Produces(APPLICATION_JSON)
		public SubscriptionUser get() {
			return authenticationContext.getUserPrincipal();
		}
	}

	@ApplicationScoped
	@Provider
	@Priority(AUTHENTICATION - 1)
	public static class FakeAuthenticator implements ContainerRequestFilter {
		private String username;
		private Set<String> roles;

		public void setData(String username, Set<String> roles) {
			this.username = username;
			this.roles = roles;
		}

		@Override
		public void filter(ContainerRequestContext requestContext) {
			if (username != null) {
				Principal principal = mock(Principal.class);
				when(principal.getName()).thenReturn(username);
				requestContext.setProperty(AuthConstants.HTTP_REQUEST_ROLES_ATTRIBUTE, roles);
				requestContext.setSecurityContext(new JaxRsSecurityContextImpl(principal, false, "FAKE"));
			}
		}
	}
}
