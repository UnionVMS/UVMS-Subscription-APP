package eu.europa.ec.fisheries.uvms.subscription.activity.bean;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import java.util.Collections;
import java.util.List;

import eu.europa.ec.fisheries.uvms.subscription.activity.communication.ActivitySender;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for the {@link SubscriptionActivityServiceBean}.
 */
@EnableAutoWeld
@ExtendWith(MockitoExtension.class)
public class SubscriptionActivityServiceBeanTest {

	@Produces @Mock
	private ActivitySender activitySender;

	@Inject
	private SubscriptionActivityServiceBean sut;

	@Test
	void testDefaultConstructor() {
		assertDoesNotThrow(() -> new SubscriptionActivityServiceBean());
	}

	@Test
	void test() {
		List<String> reportIds = Collections.singletonList("report id 1");
		String assetGuid = "asset guid";
		List<String> returnedValue = Collections.emptyList();
		when(activitySender.findMovementGuidsByReportIdsAndAssetGuid(reportIds, assetGuid)).thenReturn(returnedValue);
		List<String> result = sut.findMovementGuidsByReportIdsAndAssetGuid(reportIds, assetGuid);
		assertSame(returnedValue, result);
	}
}
