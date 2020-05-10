package eu.europa.ec.fisheries.uvms.subscription.service.util;

import static org.junit.jupiter.api.Assertions.*;

import javax.inject.Inject;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Test;

/**
 * Tests for DateTimeServiceImpl
 */
@EnableAutoWeld
class DateTimeServiceImplTest {

    @Inject
    private DateTimeServiceImpl sut;

    @Test
    void testGetNow() {
        LocalDateTime now = sut.getNow();
        assertTrue(!LocalDateTime.now().isBefore(now));
    }

    @Test
    void testGetNowAsDate() {
        Date nowAsDate = sut.getNowAsDate();
        Date now = new Date();
        assertTrue(!now.before(nowAsDate));
    }

    @Test
    void testGetNowAsInstant() {
        Instant nowAsInstant = sut.getNowAsInstant();
        assertTrue(!Instant.now().isBefore(nowAsInstant));
    }

    @Test
    void testGetToday() {
        LocalDate today = sut.getToday();
        assertTrue(!LocalDate.now().isBefore(today));
    }

    @Test
    void testCurrentTimeMillis() {
        long currentTimeMillis = sut.currentTimeMillis();
        assertTrue(System.currentTimeMillis() >= currentTimeMillis);
    }
}