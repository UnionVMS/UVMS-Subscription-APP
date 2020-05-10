package eu.europa.ec.fisheries.uvms.subscription.activity.communication;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link UsmSenderImpl}
 */
class UsmSenderImplTest {

    @Test
    void testCreateUsingEmptyConstructor() {
        UsmSender usmSender = new UsmSenderImpl();
        assertNotNull(usmSender);
    }
}