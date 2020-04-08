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

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import java.io.StringReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import eu.europa.ec.fisheries.uvms.subsription.rest.ObjectMapperContextResolver;
import eu.europa.fisheries.uvms.subscription.model.exceptions.ApplicationException;
import lombok.Data;
import org.hibernate.validator.cdi.ValidationExtension;
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
 * Tests for the {@link ConstraintViolationExceptionMapper}.
 */
@EnableAutoWeld
@AddBeanClasses({ConstraintViolationExceptionMapperTest.DummyResource.class, ConstraintViolationExceptionMapperTest.DummyBean.class})
@AddExtensions(ResteasyCdiExtension.class)
@AddExtensions(ValidationExtension.class)
@ActivateScopes({RequestScoped.class})
public class ConstraintViolationExceptionMapperTest {

	private Dispatcher dispatcher;

	private MockHttpResponse response;

	@Inject
	private DummyResource dummyResource;

	@BeforeEach
	void init() {
		dispatcher = CustomMockDispatcherFactory.createDispatcher(ObjectMapperContextResolver.class, ConstraintViolationExceptionMapper.class);
		SingletonResource resourceFactory = new SingletonResource(dummyResource, new DefaultResourceClass(ConstraintViolationExceptionMapperTest.DummyResource.class, null));
		dispatcher.getRegistry().addResourceFactory(resourceFactory, "/");
		response = new MockHttpResponse();
	}

	@Test
	void test() throws Exception {
		MockHttpRequest request = MockHttpRequest.post("/dummy")
				.contentType(MediaType.APPLICATION_JSON_TYPE)
				.accept(MediaType.APPLICATION_JSON_TYPE)
				.content("{\"name\":\"\", \"nested\":{\"value\":1},\"dummies\":[{\"value\":0}],\"map\":{\"key1\":{\"value\":1}}}".getBytes());
		dispatcher.invoke(request, response);
		assertEquals(400, response.getStatus());
		JsonReader jsonReader = Json.createReader(new StringReader(response.getContentAsString()));
		JsonObject jobj = jsonReader.readObject();
		JsonArray violations = jobj.getJsonArray("data");
		assertEquals(5, violations.size());
		Set<String> paths = violations.stream()
				.map(jv -> jv.asJsonObject().getJsonArray("path"))
				.map(ja -> ja.stream().map(this::jsonValueToString).collect(Collectors.joining("/")))
				.collect(Collectors.toSet());
		assertEquals(new HashSet<>(Arrays.asList("name", "nested/value", "dummies/0", "invalidMap", "map/key1")), paths);
	}

	private String jsonValueToString(JsonValue jv) {
		if (jv instanceof JsonString) {
			return ((JsonString) jv).getString();
		}
		else if (jv instanceof JsonNumber) {
			return Integer.toString(((JsonNumber) jv).intValue());
		}
		else {
			throw new IllegalArgumentException(jv != null ? jv.toString() : "null");
		}
	}

	@Data
	public static class DummyNestedDto {
		@Min(2)
		private int value;
	}

	@Data
	public static class DummyDto {
		@NotEmpty
		private String name;

		@Valid
		private DummyNestedDto nested;

		@Valid
		private List<DummyNestedDto> dummies;

//		@Valid
		private Map<String, @Valid DummyNestedDto> map;

		@NotEmpty
		private Map<String, String> invalidMap;
	}

	@Path("dummy")
	public static class DummyResource {
		@Inject
		private DummyBean dummy;

		@POST
		@Produces(APPLICATION_JSON)
		@Consumes(APPLICATION_JSON)
		public void post(DummyDto dto) {
			dummy.test(dto);
		}
	}

	@ApplicationScoped
	public static class DummyBean {
		public void test(@Valid @SuppressWarnings("unused") DummyDto dto) {
			throw new ApplicationException("Should have failed the validation");
		}
	}
}
