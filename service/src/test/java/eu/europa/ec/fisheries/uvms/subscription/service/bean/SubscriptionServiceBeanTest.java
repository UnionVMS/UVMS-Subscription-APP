package eu.europa.ec.fisheries.uvms.subscription.service.bean;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.validation.ValidationException;
import java.util.Date;

import eu.europa.ec.fisheries.uvms.commons.message.api.MessageException;
import eu.europa.ec.fisheries.uvms.subscription.helper.SubscriptionTestHelper;
import eu.europa.ec.fisheries.uvms.subscription.service.authentication.AuthenticationContext;
import eu.europa.ec.fisheries.uvms.subscription.service.authentication.SubscriptionUser;
import eu.europa.ec.fisheries.uvms.subscription.service.dao.SubscriptionDao;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.EmailBodyEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionOutput;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.SubscriptionDto;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.SubscriptionExecutionDto;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.SubscriptionOutputDto;
import eu.europa.ec.fisheries.uvms.subscription.service.mapper.SubscriptionMapper;
import eu.europa.ec.fisheries.uvms.subscription.service.mapper.SubscriptionMapperImpl;
import eu.europa.ec.fisheries.uvms.subscription.service.messaging.SubscriptionAuditProducer;
import eu.europa.ec.fisheries.uvms.subscription.service.messaging.SubscriptionProducerBean;
import eu.europa.fisheries.uvms.subscription.model.enums.HistoryUnit;
import eu.europa.fisheries.uvms.subscription.model.enums.OutgoingMessageType;
import eu.europa.fisheries.uvms.subscription.model.enums.TriggerType;
import eu.europa.fisheries.uvms.subscription.model.exceptions.EntityDoesNotExistException;
import org.hibernate.validator.cdi.ValidationExtension;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.BeforeEach;
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
	private static final String EMAIL_BODY = "lorem ipsum";
	private static final String PASSWORD = "1234";

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

	@Produces @Mock
	private AuthenticationContext mockAuthenticationContext;

	@Inject
	private SubscriptionServiceBean sut;

	@BeforeEach
	void beforeEach() {
		SubscriptionUser principal = mock(SubscriptionUser.class);
		lenient().when(principal.getName()).thenReturn(CURRENT_USER_NAME);
		lenient().when(mockAuthenticationContext.getUserPrincipal()).thenReturn(principal);
	}

	@Test
	void testFindByIdNonExisting() {
		when(subscriptionDAO.findById(SUBSCR_ID)).thenReturn(null);
		assertThrows(EntityDoesNotExistException.class, () -> sut.findById(SUBSCR_ID));
	}

	@Test
	void testFindById() {
		SubscriptionEntity subscription = new SubscriptionEntity();
		subscription.setId(SUBSCR_ID);
		subscription.setName(SUBSCR_NAME);
		when(subscriptionDAO.findById(SUBSCR_ID)).thenReturn(subscription);
		SubscriptionDto result = sut.findById(SUBSCR_ID);
		assertNotNull(result);
		assertEquals(SUBSCR_NAME, result.getName());
	}

	@Test
	void testFindByIdWithEmail() {
		SubscriptionEntity subscription = new SubscriptionEntity();
		subscription.setId(SUBSCR_ID);
		subscription.setName(SUBSCR_NAME);
		subscription.setOutput(new SubscriptionOutput());
		when(subscriptionDAO.findById(SUBSCR_ID)).thenReturn(subscription);
		when(subscriptionDAO.findEmailBodyEntity(SUBSCR_ID)).thenReturn(EmailBodyEntity.builder().subscription(subscription).body(EMAIL_BODY).build());
		SubscriptionDto result = sut.findById(SUBSCR_ID);
		assertNotNull(result);
		assertEquals(EMAIL_BODY, result.getOutput().getEmailConfiguration().getBody());
	}

	@Test
	void testCreateWithInvalidArguments() {
		SubscriptionDto subscription = new SubscriptionDto();
		assertThrows(ValidationException.class, () -> sut.create(null));
		assertThrows(ValidationException.class, () -> sut.create(subscription));
	}

	@Test
	void testCreateWithValidArguments() {
		SubscriptionDto dto = SubscriptionTestHelper.createSubscriptionDto( SUBSCR_ID, SUBSCR_NAME, Boolean.TRUE, OutgoingMessageType.FA_QUERY, false,
				ORGANISATION_ID, ENDPOINT_ID, CHANNEL_ID, true, 1,  HistoryUnit.DAYS, true, TriggerType.SCHEDULER, 1, "12:00", new Date(), new Date());
		when(subscriptionDAO.createEntity(any())).thenAnswer(iom -> iom.getArgument(0));
		assertDoesNotThrow(() -> sut.create(dto));
	}

	@Test
	void createSubscriptionWithNullName() {
		SubscriptionDto subscription = SubscriptionTestHelper.createSubscriptionDto( SUBSCR_ID, null, Boolean.TRUE, OutgoingMessageType.NONE, false,
				null, null, null, null, null, null, null, TriggerType.MANUAL, null, null, null, null);
		assertThrows(ValidationException.class, () -> sut.create(subscription));
	}
	@Test
	void createSubscriptionWithEmptyName() {
		SubscriptionDto subscription = SubscriptionTestHelper.createSubscriptionDto( SUBSCR_ID, "", Boolean.TRUE, OutgoingMessageType.NONE, false,
				null, null, null, null, null, null, null, TriggerType.MANUAL, null, null, null, null);
		assertThrows(ValidationException.class, () -> sut.create(subscription));
	}

	@Test
	void createSubscriptionWithNullActive() {
		SubscriptionDto subscription = SubscriptionTestHelper.createSubscriptionDto( SUBSCR_ID, SUBSCR_NAME, null, OutgoingMessageType.NONE, false,
				null, null, null, null, null, null,null, TriggerType.MANUAL, null, null, null, null);
		assertThrows(ValidationException.class, () -> sut.create(subscription));
	}

	@Test
	void createSubscriptionWithNullMessageType() {
		SubscriptionDto subscription = SubscriptionTestHelper.createSubscriptionDto( SUBSCR_ID, SUBSCR_NAME, Boolean.TRUE, null, false,
				null, null, null, null, null, null,null, TriggerType.MANUAL, null, null, null, null);
		assertThrows(ValidationException.class, () -> sut.create(subscription));
	}

	@Test
	void createSubscriptionWithFAQueryMessageTypeAndInvalidSubscriber() {
		SubscriptionDto subscription = SubscriptionTestHelper.createSubscriptionDto( SUBSCR_ID, SUBSCR_NAME, Boolean.TRUE, OutgoingMessageType.FA_QUERY, false,
				null, null, null, true, 1, HistoryUnit.DAYS, true, TriggerType.SCHEDULER, 1, "12:00", new Date(), new Date());
		assertThrows(ValidationException.class, () -> sut.create(subscription));
	}

	@Test
	void createSubscriptionWithFAQueryMessageTypeAndInvalidOutput() {
		SubscriptionDto subscription = SubscriptionTestHelper.createSubscriptionDto( SUBSCR_ID, SUBSCR_NAME, Boolean.TRUE, OutgoingMessageType.FA_QUERY, false,
				ORGANISATION_ID, ENDPOINT_ID, CHANNEL_ID, null, null, null,null, TriggerType.SCHEDULER, 1, "12:00", new Date(), new Date());
		assertThrows(ValidationException.class, () -> sut.create(subscription));
	}

	@Test
	void createSubscriptionWithFAQueryMessageTypeAndSchedulerTriggerTypeAndInvalidExecution() {
		SubscriptionDto subscription = SubscriptionTestHelper.createSubscriptionDto( SUBSCR_ID, SUBSCR_NAME, Boolean.TRUE, OutgoingMessageType.FA_QUERY, false,
				ORGANISATION_ID, ENDPOINT_ID, CHANNEL_ID, true, 1,  HistoryUnit.DAYS,true, TriggerType.SCHEDULER, null, null, null, null);
		assertThrows(ValidationException.class, () -> sut.create(subscription));
	}

	@Test
	void createSubscriptionWithFAQueryMessageTypeAndInvalidNegativeHistory() {
		SubscriptionDto subscription = SubscriptionTestHelper.createSubscriptionDto( SUBSCR_ID, SUBSCR_NAME, Boolean.TRUE, OutgoingMessageType.FA_QUERY, false,
				ORGANISATION_ID, ENDPOINT_ID, CHANNEL_ID, true, -1, HistoryUnit.DAYS, true, TriggerType.SCHEDULER, 1, "12:00", null, null);
		assertThrows(ValidationException.class, () -> sut.create(subscription));
	}

	@Test
	void createSubscriptionWithTriggerTypeSchedulerAndInvalidTimeExpression() {
		SubscriptionDto subscription = SubscriptionTestHelper.createSubscriptionDto( SUBSCR_ID, SUBSCR_NAME, Boolean.TRUE, OutgoingMessageType.FA_QUERY, false,
				ORGANISATION_ID, ENDPOINT_ID, CHANNEL_ID, true, 1,  HistoryUnit.DAYS,true, TriggerType.SCHEDULER, 1, "120:00", null, null);
		assertThrows(ValidationException.class, () -> sut.create(subscription));
	}

	@Test
	void createValidSubscriptionWithNoneMessageTypeAndNullSubscriber() {
		SubscriptionDto dto = SubscriptionTestHelper.createSubscriptionDto( SUBSCR_ID, SUBSCR_NAME, Boolean.TRUE, OutgoingMessageType.NONE, false,
				null, null, null, true, 1,  HistoryUnit.DAYS,true, TriggerType.SCHEDULER, 1, "12:00", new Date(), new Date());

		when(subscriptionDAO.createEntity(any())).thenAnswer(iom -> iom.getArgument(0));

		assertDoesNotThrow(() -> sut.create(dto));
	}

	@Test
	void checkAvailableNameForExistingEntityWithThatName() {
		SubscriptionEntity entity = new SubscriptionEntity();
		entity.setId(SUBSCR_ID);
		entity.setName(SUBSCR_NAME);
		when(subscriptionDAO.findSubscriptionByName(SUBSCR_NAME)).thenReturn(entity);
		assertFalse(sut.checkNameAvailability(SUBSCR_NAME, null));
		assertFalse(sut.checkNameAvailability(SUBSCR_NAME, 2L));
		assertTrue(sut.checkNameAvailability(SUBSCR_NAME, SUBSCR_ID));
	}

	@Test
	void checkAvailableNameForNoEntityWithThatName(){
		when(subscriptionDAO.findSubscriptionByName(any(String.class))).thenReturn(null);
		assertTrue(sut.checkNameAvailability(SUBSCR_NAME, null));
		assertTrue(sut.checkNameAvailability("name", 2L));
	}

	@Test
	void testCreate() {
		SubscriptionDto dto = SubscriptionTestHelper.createSubscriptionDtoWithEmailConfig( SUBSCR_ID, SUBSCR_NAME, Boolean.TRUE, OutgoingMessageType.FA_QUERY, true,
				ORGANISATION_ID, ENDPOINT_ID, CHANNEL_ID, true, 1, HistoryUnit.DAYS,true, TriggerType.SCHEDULER, 1, "12:00", new Date(), new Date(),
				EMAIL_BODY, true, true, PASSWORD, false, false);

		when(subscriptionDAO.createEntity(any())).thenAnswer(iom -> iom.getArgument(0));
		when(subscriptionDAO.createEmailBodyEntity(any())).thenAnswer(iom -> iom.getArgument(0));

		SubscriptionDto result = sut.create(dto);

		assertNotNull(result);
		assertEquals(SUBSCR_ID, result.getId());
		assertEquals(SUBSCR_NAME, result.getName());
		assertEquals(Boolean.TRUE, result.getActive());
		assertEquals(ORGANISATION_ID, result.getOutput().getSubscriber().getOrganisationId());
		assertEquals(ENDPOINT_ID, result.getOutput().getSubscriber().getEndpointId());
		assertEquals(CHANNEL_ID, result.getOutput().getSubscriber().getChannelId());
		assertEquals(TriggerType.SCHEDULER, result.getExecution().getTriggerType());
		assertEquals(OutgoingMessageType.FA_QUERY, result.getOutput().getMessageType());
		assertEquals(HistoryUnit.DAYS, result.getOutput().getHistoryUnit());
		//assertEquals(0,dto.getStartDate().toInstant().truncatedTo(ChronoUnit.SECONDS).compareTo(result.getStartDate().toInstant().truncatedTo(ChronoUnit.SECONDS)));
		assertEquals(dto.getStartDate(),result.getStartDate());
		assertEquals(EMAIL_BODY, result.getOutput().getEmailConfiguration().getBody());
		assertEquals(true, result.getOutput().getEmailConfiguration().getIsPdf());
		assertEquals(true, result.getOutput().getEmailConfiguration().getHasAttachments());
		assertEquals(PASSWORD, result.getOutput().getEmailConfiguration().getPassword());
		assertEquals(true, result.getOutput().getEmailConfiguration().getPasswordIsPlaceholder());
		assertEquals(false, result.getOutput().getEmailConfiguration().getIsXml());
	}

	@Test
	void testUpdateNonExistingEntity() {
		SubscriptionDto dto = makeSubscriptionDtoForUpdate();
		when(subscriptionDAO.findById(SUBSCR_ID)).thenReturn(null);
		assertThrows(EntityDoesNotExistException.class, () -> sut.update(dto));
	}

	@Test
	void testUpdate() throws MessageException {
		SubscriptionDto dto = makeSubscriptionDtoForUpdate();
		SubscriptionEntity subscription = new SubscriptionEntity();
		subscription.setId(SUBSCR_ID);
		when(subscriptionDAO.findById(SUBSCR_ID)).thenReturn(subscription);
		when(subscriptionDAO.update(subscription)).thenReturn(subscription);
		SubscriptionDto result = sut.update(dto);
		assertNotNull(result);
		verify(subscriptionDAO).update(subscription);
		verify(auditProducer).sendModuleMessage(any(), any());
	}

	private SubscriptionDto makeSubscriptionDtoForUpdate() {
		SubscriptionDto dto = new SubscriptionDto();
		dto.setId(SUBSCR_ID);
		dto.setName(SUBSCR_NAME);
		dto.setActive(Boolean.TRUE);
		dto.setExecution(new SubscriptionExecutionDto());
		dto.setOutput(SubscriptionOutputDto.builder()
				.messageType(OutgoingMessageType.NONE)
				.hasEmail(false)
				.build()
		);
		return dto;
	}

	@Test
	void testDelete() throws MessageException {
		sut.delete(SUBSCR_ID);
		verify(subscriptionDAO).delete(SUBSCR_ID);
		verify(auditProducer).sendModuleMessage(any(), any());
	}
}
