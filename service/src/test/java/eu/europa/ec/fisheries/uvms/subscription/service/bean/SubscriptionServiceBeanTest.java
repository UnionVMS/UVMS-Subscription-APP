package eu.europa.ec.fisheries.uvms.subscription.service.bean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.validation.ConstraintViolationException;

import java.util.Date;

import eu.europa.ec.fisheries.uvms.commons.service.exception.ServiceException;
import eu.europa.ec.fisheries.uvms.subscription.service.dao.SubscriptionDao;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.AccessibilityType;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionType;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggerType;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.SubscriptionDto;
import eu.europa.ec.fisheries.uvms.subscription.service.mapper.SubscriptionMapper;
import eu.europa.ec.fisheries.uvms.subscription.service.mapper.SubscriptionMapperImpl;
import eu.europa.ec.fisheries.uvms.subscription.service.messaging.SubscriptionAuditProducer;
import eu.europa.ec.fisheries.uvms.subscription.service.messaging.SubscriptionProducerBean;
import eu.europa.ec.fisheries.wsdl.subscription.module.MessageType;
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
		assertThrows(ConstraintViolationException.class, () -> sut.create(null, null));
		assertThrows(ConstraintViolationException.class, () -> sut.create(subscription, null));
		assertThrows(ConstraintViolationException.class, () -> sut.create(null, "something"));
		assertThrows(ConstraintViolationException.class, () -> sut.create(subscription, "something"));
	}

	@Test
	void testCreate() throws ServiceException {
		SubscriptionDto s = new SubscriptionDto();
		// make sure it is valid
		s.setId(SUBSCR_ID);
		s.setName(SUBSCR_NAME);
		s.setAccessibility(AccessibilityType.PRIVATE);
		s.setActive(Boolean.TRUE);
		s.setOrganisation(ORGANISATION_ID);
		s.setEndPoint(ENDPOINT_ID);
		s.setChannel(CHANNEL_ID);
		s.setTriggerType(TriggerType.SCHEDULER);
		s.setMessageType(MessageType.FLUX_FA_QUERY_MESSAGE);
		s.setSubscriptionType(SubscriptionType.TX_PULL);
		s.setStartDate(new Date());

		when(subscriptionDAO.createEntity(any())).thenAnswer(iom -> iom.getArgument(0));

		SubscriptionDto result = sut.create(s, CURRENT_USER_NAME);

		assertNotNull(result);
		assertEquals(SUBSCR_ID, result.getId());
		assertEquals(SUBSCR_NAME, result.getName());
		assertEquals(AccessibilityType.PRIVATE, result.getAccessibility());
		assertEquals(Boolean.TRUE, result.getActive());
		assertEquals(ORGANISATION_ID, result.getOrganisation());
		assertEquals(ENDPOINT_ID, result.getEndPoint());
		assertEquals(CHANNEL_ID, result.getChannel());
		assertEquals(TriggerType.SCHEDULER, result.getTriggerType());
		assertEquals(MessageType.FLUX_FA_QUERY_MESSAGE, result.getMessageType());
		assertEquals(SubscriptionType.TX_PULL, result.getSubscriptionType());
		assertEquals(s.getStartDate(), result.getStartDate());
	}
}
