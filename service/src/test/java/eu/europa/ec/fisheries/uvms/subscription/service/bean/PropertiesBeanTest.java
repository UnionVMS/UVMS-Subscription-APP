package eu.europa.ec.fisheries.uvms.subscription.service.bean;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.inject.Inject;

import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link PropertiesBean}.
 */
@EnableAutoWeld
public class PropertiesBeanTest {
	@Inject
	private PropertiesBean sut;

	@Test
	void test() {
		assertEquals("test value", sut.getProperty("test.property"));
	}
}
