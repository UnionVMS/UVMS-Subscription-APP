package eu.europa.ec.fisheries.uvms.subscription.service.bean;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.validation.ValidationException;
import java.util.Date;

import eu.europa.ec.fisheries.uvms.subscription.helper.SubscriptionTestHelper;
import eu.europa.ec.fisheries.uvms.subscription.service.dao.SubscriptionDao;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.SubscriptionDto;
import eu.europa.ec.fisheries.uvms.subscription.service.mapper.SubscriptionMapper;
import eu.europa.ec.fisheries.uvms.subscription.service.mapper.SubscriptionMapperImpl;
import eu.europa.ec.fisheries.uvms.subscription.service.messaging.SubscriptionAuditProducer;
import eu.europa.ec.fisheries.uvms.subscription.service.messaging.SubscriptionProducerBean;
import eu.europa.fisheries.uvms.subscription.model.enums.OutgoingMessageType;
import eu.europa.fisheries.uvms.subscription.model.enums.TriggerType;
import org.hibernate.validator.cdi.ValidationExtension;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for the {@link SubscriptionServiceBean}.
 */
@EnableAutoWeld
@AddExtensions(ValidationExtension.class)
@ExtendWith(MockitoExtension.class)
public class SubscriptionServiceBeanTest {

	private static final Long SUBSCR_ID = 17L;
	private static final String SUBSCR_NAME = "Name";
	private static final Long ORGANISATION_ID = 117L;
	private static final Long ENDPOINT_ID = 53L;
	private static final Long CHANNEL_ID = 67L;
	private static final String CURRENT_USER_NAME = "curuser";

	@Produces @Mock
	private SubscriptionDao subscriptionDAO;

	@Produces @Mock
	private SubscriptionUserConsumerBean subscriptionUserConsumerBean;

	@Produces @Mock
	private SubscriptionUserProducerBean subscriptionUserProducerBean;

	@Produces
	private SubscriptionMapper mapper = new SubscriptionMapperImpl();

	@Produces @Mock
	private SubscriptionAuditProducer auditProducer;

	@Produces @Mock
	private SubscriptionProducerBean subscriptionProducer;

	@Inject
	private SubscriptionServiceBean sut;

	@Test
	void testCreateWithInvalidArguments() {
		SubscriptionDto subscription = new SubscriptionDto();
		assertThrows(ValidationException.class, () -> sut.create(null, null));
		assertThrows(ValidationException.class, () -> sut.create(subscription, null));
		assertThrows(ValidationException.class, () -> sut.create(null, "something"));
		assertThrows(ValidationException.class, () -> sut.create(subscription, "something"));
	}

	@Test
	void testCreateWithValidArguments() {
		SubscriptionDto dto = SubscriptionTestHelper.createSubsriptionDto( SUBSCR_ID, SUBSCR_NAME, Boolean.TRUE, OutgoingMessageType.FA_QUERY,
				ORGANISATION_ID, ENDPOINT_ID, CHANNEL_ID, true, 1, true, TriggerType.SCHEDULER, 1, "time", new Date(), new Date());
		when(subscriptionDAO.createEntity(any())).thenAnswer(iom -> iom.getArgument(0));
		assertDoesNotThrow(() -> sut.create(dto, "something"));
	}

	@Test
	void createSubscriptionWithNullName() {
		SubscriptionDto subscription = SubscriptionTestHelper.createSubsriptionDto( SUBSCR_ID, null, Boolean.TRUE, OutgoingMessageType.NONE,
				null, null, null, null, null, null, TriggerType.MANUAL, null, null, null, null);
		assertThrows(ValidationException.class, () -> sut.create(subscription, "something"));
	}
	@Test
	void createSubscriptionWithEmptyName() {
		SubscriptionDto subscription = SubscriptionTestHelper.createSubsriptionDto( SUBSCR_ID, "", Boolean.TRUE, OutgoingMessageType.NONE,
				null, null, null, null, null, null, TriggerType.MANUAL, null, null, null, null);
		assertThrows(ValidationException.class, () -> sut.create(subscription, "something"));
	}

	@Test
	void createSubscriptionWithNullActive() {
		SubscriptionDto subscription = SubscriptionTestHelper.createSubsriptionDto( SUBSCR_ID, SUBSCR_NAME, null, OutgoingMessageType.NONE,
				null, null, null, null, null, null, TriggerType.MANUAL, null, null, null, null);
		assertThrows(ValidationException.class, () -> sut.create(subscription, "something"));
	}

	@Test
	void createSubscriptionWithNullMessageType() {
		SubscriptionDto subscription = SubscriptionTestHelper.createSubsriptionDto( SUBSCR_ID, SUBSCR_NAME, Boolean.TRUE, null,
				null, null, null, null, null, null, TriggerType.MANUAL, null, null, null, null);
		assertThrows(ValidationException.class, () -> sut.create(subscription, "something"));
	}

	@Test
	void createSubscriptionWithFAQueryMessageTypeAndInvalidSubscriber() {
		SubscriptionDto subscription = SubscriptionTestHelper.createSubsriptionDto( SUBSCR_ID, SUBSCR_NAME, Boolean.TRUE, OutgoingMessageType.FA_QUERY,
				null, null, null, true, 1, true, TriggerType.SCHEDULER, 1, "time", new Date(), new Date());
		assertThrows(ValidationException.class, () -> sut.create(subscription, "something"));
	}

	@Test
	void createSubscriptionWithFAQueryMessageTypeAndInvalidOutput() {
		SubscriptionDto subscription = SubscriptionTestHelper.createSubsriptionDto( SUBSCR_ID, SUBSCR_NAME, Boolean.TRUE, OutgoingMessageType.FA_QUERY,
				ORGANISATION_ID, ENDPOINT_ID, CHANNEL_ID, null, null, null, TriggerType.SCHEDULER, 1, "time", new Date(), new Date());
		assertThrows(ValidationException.class, () -> sut.create(subscription, "something"));
	}

	@Test
	void createSubscriptionWithFAQueryMessageTypeAndSchedulerTriggerTypeAndInvalidExecution() {
		SubscriptionDto subscription = SubscriptionTestHelper.createSubsriptionDto( SUBSCR_ID, SUBSCR_NAME, Boolean.TRUE, OutgoingMessageType.FA_QUERY,
				ORGANISATION_ID, ENDPOINT_ID, CHANNEL_ID, true, 1, true, TriggerType.SCHEDULER, null, null, null, null);
		assertThrows(ValidationException.class, () -> sut.create(subscription, "something"));
	}

	@Test
	void testCreate() {
		SubscriptionDto dto = SubscriptionTestHelper.createSubsriptionDto( SUBSCR_ID, SUBSCR_NAME, Boolean.TRUE, OutgoingMessageType.FA_QUERY,
				ORGANISATION_ID, ENDPOINT_ID, CHANNEL_ID, true, 1, true, TriggerType.SCHEDULER, 1, "time", new Date(), new Date());

		when(subscriptionDAO.createEntity(any())).thenAnswer(iom -> iom.getArgument(0));

		SubscriptionDto result = sut.create(dto, CURRENT_USER_NAME);

		assertNotNull(result);
		assertEquals(SUBSCR_ID, result.getId());
		assertEquals(SUBSCR_NAME, result.getName());
		assertEquals(Boolean.TRUE, result.getActive());
		assertEquals(ORGANISATION_ID, result.getOutput().getSubscriber().getOrganisationId());
		assertEquals(ENDPOINT_ID, result.getOutput().getSubscriber().getEndpointId());
		assertEquals(CHANNEL_ID, result.getOutput().getSubscriber().getChannelId());
		assertEquals(TriggerType.SCHEDULER, result.getExecution().getTriggerType());
		assertEquals(OutgoingMessageType.FA_QUERY, result.getOutput().getMessageType());
		assertEquals(dto.getStartDate(), result.getStartDate());
	}
}
