package eu.europa.ec.fisheries.uvms.subscription.service.trigger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.SubscriptionSearchCriteria;
import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link SenderInformation}.
 */
public class SenderInformationTest {
	@Test
	void testFromProperties() {
		assertNull(SenderInformation.fromProperties(null, null));
		assertNull(SenderInformation.fromProperties("DF", null));
		assertNull(SenderInformation.fromProperties(null, "SR"));
		SenderInformation sut = SenderInformation.fromProperties("DF", "SR");
		assertEquals("DF", sut.getDataflow());
		assertEquals("SR", sut.getSenderOrReceiver());
	}
}
