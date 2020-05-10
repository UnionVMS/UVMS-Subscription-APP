package eu.europa.fisheries.uvms.subscription.model.enums;

import static org.junit.jupiter.api.Assertions.*;

import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.Test;

/**
 * Test for {@link SubscriptionTimeUnit}.
 */
class SubscriptionTimeUnitTest {

    @Test
    void testGetTemporalUnit() {
        assertEquals(ChronoUnit.DAYS, SubscriptionTimeUnit.DAYS.getTemporalUnit());
        assertEquals(ChronoUnit.WEEKS, SubscriptionTimeUnit.WEEKS.getTemporalUnit());
        assertEquals(ChronoUnit.MONTHS, SubscriptionTimeUnit.MONTHS.getTemporalUnit());
    }
}