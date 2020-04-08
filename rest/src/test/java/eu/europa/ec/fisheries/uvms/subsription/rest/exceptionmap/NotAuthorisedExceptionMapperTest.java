/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subsription.rest.exceptionmap;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.io.StringReader;

import eu.europa.ec.fisheries.uvms.subsription.rest.ObjectMapperContextResolver;
import eu.europa.fisheries.uvms.subscription.model.exceptions.NotAuthorisedException;
import org.jboss.resteasy.cdi.ResteasyCdiExtension;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.jboss.resteasy.plugins.server.resourcefactory.SingletonResource;
import org.jboss.resteasy.spi.Dispatcher;
import org.jboss.resteasy.spi.metadata.DefaultResourceClass;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link NotAuthorisedExceptionMapper}.
 */
@EnableAutoWeld
@AddExtensions(ResteasyCdiExtension.class)
public class NotAuthorisedExceptionMapperTest {

	private static final String MESSAGE = "the exception message";

	private Dispatcher dispatcher;

	private MockHttpResponse response;

	@BeforeEach
	void init() {
		dispatcher = CustomMockDispatcherFactory.createDispatcher(ObjectMapperContextResolver.class, NotAuthorisedExceptionMapper.class);
		SingletonResource resourceFactory = new SingletonResource(new DummyResource(), new DefaultResourceClass(DummyResource.class, null));
		dispatcher.getRegistry().addResourceFactory(resourceFactory, "/");
		response = new MockHttpResponse();
	}

	@Test
	void test() throws Exception {
		MockHttpRequest request = MockHttpRequest.get("/dummy");
		dispatcher.invoke(request, response);
		assertEquals(403, response.getStatus());
		JsonReader jsonReader = Json.createReader(new StringReader(response.getContentAsString()));
		JsonObject jobj = jsonReader.readObject();
		assertEquals(MESSAGE, jobj.getString("data"));
	}

	@Path("dummy")
	public static class DummyResource {
		@GET
		@Produces(APPLICATION_JSON)
		public String get() {
			throw new NotAuthorisedException(MESSAGE);
		}
	}
}
