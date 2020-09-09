package eu.europa.ec.fisheries.uvms.subscription.service.bean;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import eu.europa.ec.fisheries.uvms.commons.message.api.MessageException;
import eu.europa.ec.fisheries.uvms.subscription.helper.DateTimeServiceTestImpl;
import eu.europa.ec.fisheries.uvms.subscription.helper.SubscriptionTestHelper;
import eu.europa.ec.fisheries.uvms.subscription.service.authentication.AuthenticationContext;
import eu.europa.ec.fisheries.uvms.subscription.service.authentication.SubscriptionUser;
import eu.europa.ec.fisheries.uvms.subscription.service.dao.SubscriptionDao;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.AreaEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.AssetEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.AssetGroupEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.EmailBodyEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionOutput;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.SubscriptionListQuery;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.SubscriptionSearchCriteria;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.AreaDto;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.AssetDto;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.SubscriptionDto;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.SubscriptionExecutionDto;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.SubscriptionFishingActivityDto;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.SubscriptionOutputDto;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.SubscriptionSubscriberDto;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.list.SubscriptionListResponseDto;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.search.OrderByDataImpl;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.search.PaginationDataImpl;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.search.SubscriptionListQueryImpl;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.search.SubscriptionSearchCriteriaImpl;
import eu.europa.ec.fisheries.uvms.subscription.service.mapper.SubscriptionMapper;
import eu.europa.ec.fisheries.uvms.subscription.service.mapper.SubscriptionMapperImpl;
import eu.europa.ec.fisheries.uvms.subscription.service.messaging.AssetPageRetrievalMessage;
import eu.europa.ec.fisheries.uvms.subscription.service.messaging.SubscriptionAuditProducer;
import eu.europa.ec.fisheries.uvms.subscription.service.messaging.SubscriptionProducerBean;
import eu.europa.ec.fisheries.uvms.subscription.service.messaging.usm.UsmClient;
import eu.europa.ec.fisheries.uvms.subscription.service.messaging.SubscriptionSender;
import eu.europa.ec.fisheries.uvms.subscription.service.messaging.asset.AssetSender;
import eu.europa.ec.fisheries.wsdl.asset.types.AssetHistGuidIdWithVesselIdentifiers;
import eu.europa.ec.fisheries.wsdl.asset.types.VesselIdentifiersHolder;
import eu.europa.ec.fisheries.wsdl.subscription.module.AreaType;
import eu.europa.fisheries.uvms.subscription.model.enums.AssetType;
import eu.europa.fisheries.uvms.subscription.model.enums.OutgoingMessageType;
import eu.europa.fisheries.uvms.subscription.model.enums.SubscriptionFaReportDocumentType;
import eu.europa.fisheries.uvms.subscription.model.enums.SubscriptionTimeUnit;
import eu.europa.fisheries.uvms.subscription.model.enums.TriggerType;
import eu.europa.fisheries.uvms.subscription.model.exceptions.ApplicationException;
import eu.europa.fisheries.uvms.subscription.model.exceptions.EntityDoesNotExistException;
import org.hibernate.validator.cdi.ValidationExtension;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
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
	private static final String PASSWORD_PLACEHOLDER = "********";

	private static final Long ASSET_ID = 111L;
	private static final String ASSET_GUID = "GUID";
	private static final String ASSET_NAME = "asset name";
	private static final String CFR = "cfr";
	private static final String IRCS = "ircs";
	private static final String ICCAT = "iccat";
	private static final String EXT_MARK = "EXT_MARK";
	private static final String UVI = "UVI";

	private static final String ASSET_NEW_GUID = "NEW GUID";
	private static final String ASSET_NEW_NAME = "NEW NAME";
	private static final String NEW_CFR = "NEW cfr";
	private static final String NEW_IRCS = "NEW ircs";
	private static final String NEW_ICCAT = "NEW iccat";
	private static final String NEW_EXT_MARK = "NEW EXT_MARK";
	private static final String NEW_UVI = "NEW UVI";

	private static final Long ASSET_GROUP_ID = 222L;
	private static final String ASSET_GROUP_GUID = "GROUP GUID";
	private static final String ASSET_GROUP_NAME = "GROUP NAME";

	private final LocalDate todayLD = LocalDate.now();
	private final Date today = Date.from(todayLD.atStartOfDay(ZoneId.systemDefault()).toInstant());
	private final Date tomorrow = Date.from(todayLD.plus(1, ChronoUnit.DAYS).atStartOfDay(ZoneId.systemDefault()).toInstant());

	@Produces @Mock
	private SubscriptionDao subscriptionDAO;

	@Produces @Mock
	private UsmClient usmClient;

	@Produces @Mock
	private AssetSender assetSender;

	@Produces
	private SubscriptionMapper mapper = new SubscriptionMapperImpl();

	@Produces
	private DateTimeServiceTestImpl dateTimeService = new DateTimeServiceTestImpl();

	@Produces @Mock
	private SubscriptionAuditProducer auditProducer;

	@Produces @Mock
	private SubscriptionProducerBean subscriptionProducer;

	@Produces @Mock
	private SubscriptionSender subscriptionSender;

	@Produces @Mock
	private AuthenticationContext mockAuthenticationContext;

	@Inject
	private SubscriptionServiceBean sut;

	@BeforeEach
	void beforeEach() {
		LocalDateTime now = LocalDateTime.parse("2020-05-05T11:00:00");
		dateTimeService.setNow(now);
		SubscriptionUser principal = mock(SubscriptionUser.class);
		lenient().when(principal.getName()).thenReturn(CURRENT_USER_NAME);
		lenient().when(mockAuthenticationContext.getUserPrincipal()).thenReturn(principal);
		lenient().doThrow(ApplicationException.class).when(assetSender).findMultipleVesselIdentifiers(argThat(arg -> arg == null || arg.isEmpty()));
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

	@ParameterizedTest
	@MethodSource("triggerTypeCriteria")
	void testListSubscriptions(Collection<TriggerType> triggerTypes, Collection<TriggerType> exceptedTriggerTypes) {
		PaginationDataImpl paginationData = mock(PaginationDataImpl.class);
		when(paginationData.getPageSize()).thenReturn(10);
		when(paginationData.getOffset()).thenReturn(0);
		OrderByDataImpl orderByData = mock(OrderByDataImpl.class);
		SubscriptionSearchCriteriaImpl criteria = new SubscriptionSearchCriteriaImpl();
		criteria.setWithAnyTriggerType(triggerTypes);
		SubscriptionListQuery queryParams = new SubscriptionListQueryImpl(paginationData, orderByData, criteria);

		SubscriptionEntity subscription = new SubscriptionEntity();
		subscription.setId(SUBSCR_ID);
		subscription.setName(SUBSCR_NAME);
		subscription.setOutput(new SubscriptionOutput());

		when(subscriptionDAO.count(queryParams.getCriteria())).thenReturn(1L);
		when(subscriptionDAO.listSubscriptions(queryParams)).thenReturn(Collections.singletonList(subscription));
		when(usmClient.getAllOrganisations("DG_MARE", "rep_power_role", "curuser")).thenReturn(null);

		SubscriptionListResponseDto result = sut.listSubscriptions(queryParams, "DG_MARE", "rep_power_role");

		ArgumentCaptor<SubscriptionSearchCriteria> criteriaCaptor = ArgumentCaptor.forClass(SubscriptionSearchCriteria.class);
		verify(subscriptionDAO).count(criteriaCaptor.capture());
		assertEquals(exceptedTriggerTypes.size(), criteriaCaptor.getValue().getWithAnyTriggerType().size());
		assertTrue(criteriaCaptor.getValue().getWithAnyTriggerType().containsAll(exceptedTriggerTypes));
		assertNotNull(result);
	}

	protected static Stream<Arguments> triggerTypeCriteria() {
		List<TriggerType> triggerTypesExcludingManual = Arrays.stream(TriggerType.values()).filter(t -> t != TriggerType.MANUAL).collect(Collectors.toList());
		return Stream.of(
				Arguments.of(null, triggerTypesExcludingManual),
				Arguments.of(Collections.emptyList(), triggerTypesExcludingManual),
				Arguments.of(Collections.singletonList(TriggerType.INC_FA_REPORT), Collections.singletonList(TriggerType.INC_FA_REPORT)),
				Arguments.of(Arrays.asList(TriggerType.MANUAL, TriggerType.INC_POSITION), Collections.singletonList(TriggerType.INC_POSITION)),
				Arguments.of(Collections.singletonList(TriggerType.MANUAL), triggerTypesExcludingManual)
		);
	}

	@Test
	void testCreateWithInvalidArguments() {
		SubscriptionDto subscription = new SubscriptionDto();
		assertThrows(ValidationException.class, () -> sut.create(null));
		assertThrows(ValidationException.class, () -> sut.create(subscription));
	}

	@Test
	void createSubscriptionWithNullOutput() {
		SubscriptionDto subscription = new SubscriptionDto();
		subscription.setName(SUBSCR_NAME);
		subscription.setActive(true);
		SubscriptionExecutionDto execution = new SubscriptionExecutionDto();
		execution.setTriggerType(TriggerType.MANUAL);
		subscription.setExecution(execution);
		assertThrows(ConstraintViolationException.class, () -> sut.create(subscription));
	}

	@Test
	void createManualSubscriptionWithoutQueryPeriodOrHistory() {
		SubscriptionDto subscription = SubscriptionTestHelper.createManualSubscriptionDto( SUBSCR_ID, SUBSCR_NAME, Boolean.TRUE, OutgoingMessageType.NONE, false,
				null, null, null, null, null, null, null, null, null, null,
				null, null, null, null);
		assertThrows(ConstraintViolationException.class, () -> sut.create(subscription));
	}

	@Test
	void createManualSubscriptionWithoutWithInvalidHistory() {
		SubscriptionDto subscription = SubscriptionTestHelper.createManualSubscriptionDto( SUBSCR_ID, SUBSCR_NAME, Boolean.TRUE, OutgoingMessageType.NONE, false,
				null, null, null, null, -1, null, null, null, null, null,
				null, null, null, null);
		assertThrows(ConstraintViolationException.class, () -> sut.create(subscription));
	}

	@Test
	void createManualSubscriptionWithQueryPeriodWithoutHistory() {
		SubscriptionDto subscription = SubscriptionTestHelper.createManualSubscriptionDto( SUBSCR_ID, SUBSCR_NAME, Boolean.TRUE, OutgoingMessageType.NONE, false,
				null, null, null, null, null, null, today, tomorrow, null, null,
				null, null, null, null);
		when(subscriptionDAO.createEntity(any())).thenAnswer(iom -> iom.getArgument(0));
		assertDoesNotThrow(() -> sut.create(subscription));
	}

	@Test
	void createManualSubscriptionWithQueryPeriodWithoutHistoryForFAQueryType() {
		SubscriptionDto subscription = SubscriptionTestHelper.createManualSubscriptionDto( SUBSCR_ID, SUBSCR_NAME, Boolean.TRUE, OutgoingMessageType.FA_QUERY, false,
				ORGANISATION_ID, ENDPOINT_ID, CHANNEL_ID,false, null, null, today, tomorrow, false, null,
				null, null, null, null);
		when(subscriptionDAO.createEntity(any())).thenAnswer(iom -> iom.getArgument(0));
		assertDoesNotThrow(() -> sut.create(subscription));
	}

	@Test
	void createManualSubscriptionWithoutQueryPeriodWithoutHistoryForFAQueryType() {
		SubscriptionDto subscription = SubscriptionTestHelper.createManualSubscriptionDto( SUBSCR_ID, SUBSCR_NAME, Boolean.TRUE, OutgoingMessageType.FA_QUERY, false,
				ORGANISATION_ID, ENDPOINT_ID, CHANNEL_ID,false, null, null, null, null, false, null,
				null, null, null, null);
		assertThrows(ConstraintViolationException.class, () -> sut.create(subscription));
	}

	@Test
	void createSubscriptionWithMessageTypeNoneAndSubscriber() {
		SubscriptionDto subscription = new SubscriptionDto();
		subscription.setName(SUBSCR_NAME);
		subscription.setActive(true);
		SubscriptionOutputDto output = new SubscriptionOutputDto();
		output.setMessageType(OutgoingMessageType.NONE);
		output.setHasEmail(false);
		SubscriptionSubscriberDto subscriber = new SubscriptionSubscriberDto();
		subscriber.setOrganisationId(1L);
		subscriber.setEndpointId(1L);
		subscriber.setChannelId(1L);
		output.setSubscriber(subscriber);
		subscription.setOutput(output);
		SubscriptionExecutionDto execution = new SubscriptionExecutionDto();
		execution.setTriggerType(TriggerType.MANUAL);
		subscription.setExecution(execution);
		assertThrows(ConstraintViolationException.class, () -> sut.create(subscription));
	}

	@Test
	void testCreateWithValidArgumentsAndMessageTypeFA_QUERY() {
		SubscriptionDto dto = SubscriptionTestHelper.createSubscriptionDto( SUBSCR_ID, SUBSCR_NAME, Boolean.TRUE, OutgoingMessageType.FA_QUERY, false,
				ORGANISATION_ID, ENDPOINT_ID, CHANNEL_ID, true, 1, SubscriptionTimeUnit.DAYS, true, TriggerType.SCHEDULER, 1, SubscriptionTimeUnit.DAYS, "12:00", new Date(), new Date());
		when(subscriptionDAO.createEntity(any())).thenAnswer(iom -> iom.getArgument(0));
		assertDoesNotThrow(() -> sut.create(dto));
	}

	@Test
	void testCreateWithInvalidArgumentsAndMessageTypePOSITION() {
		SubscriptionDto dto = SubscriptionTestHelper.createSubscriptionDto(SUBSCR_ID, SUBSCR_NAME, Boolean.TRUE, OutgoingMessageType.POSITION, false,
				ORGANISATION_ID, ENDPOINT_ID, CHANNEL_ID, true, 1, SubscriptionTimeUnit.DAYS, true, TriggerType.SCHEDULER, 1, SubscriptionTimeUnit.DAYS, "12:00", new Date(), new Date());
		assertThrows(ConstraintViolationException.class, () -> sut.create(dto));
	}

	@Test
	void testCreateWithValidArgumentsAndMessageTypePOSITION() {
		SubscriptionDto dto = SubscriptionTestHelper.createSubscriptionDto(SUBSCR_ID, SUBSCR_NAME, Boolean.TRUE, OutgoingMessageType.POSITION, false,
				ORGANISATION_ID, ENDPOINT_ID, CHANNEL_ID, true, 1, SubscriptionTimeUnit.DAYS, true, TriggerType.INC_POSITION, 1, SubscriptionTimeUnit.DAYS, "12:00", new Date(), new Date());
		when(subscriptionDAO.createEntity(any())).thenAnswer(iom -> iom.getArgument(0));
		assertDoesNotThrow(() -> sut.create(dto));
	}

	@Test
	void testCreateWithValidArgumentsAndMessageTypeFA_REPORT() {
		SubscriptionDto dto = SubscriptionTestHelper.createSubscriptionDto( SUBSCR_ID, SUBSCR_NAME, Boolean.TRUE, OutgoingMessageType.FA_REPORT, false,
				ORGANISATION_ID, ENDPOINT_ID, CHANNEL_ID, true, 1, SubscriptionTimeUnit.DAYS, true, TriggerType.SCHEDULER, 1, SubscriptionTimeUnit.DAYS, "12:00", new Date(), new Date());
		when(subscriptionDAO.createEntity(any())).thenAnswer(iom -> iom.getArgument(0));
		assertDoesNotThrow(() -> sut.create(dto));
	}

	@Test
	void createSubscriptionWithNullName() {
		SubscriptionDto subscription = SubscriptionTestHelper.createManualSubscriptionDto( SUBSCR_ID, null, Boolean.TRUE, OutgoingMessageType.NONE, false,
				null, null, null, null, 1, null, today, tomorrow, null, null,
				null, null, null, null);
		assertThrows(ConstraintViolationException.class, () -> sut.create(subscription));
	}

	@Test
	void createSubscriptionWithEmptyName() {
		SubscriptionDto subscription = SubscriptionTestHelper.createManualSubscriptionDto( SUBSCR_ID, "", Boolean.TRUE, OutgoingMessageType.NONE, false,
				null, null, null, null, null, null, today, tomorrow, null, null, null, null,null,null);
		assertThrows(ConstraintViolationException.class, () -> sut.create(subscription));
	}

	@Test
	void createSubscriptionWithNullActive() {
		SubscriptionDto subscription = SubscriptionTestHelper.createManualSubscriptionDto( SUBSCR_ID, SUBSCR_NAME, null, OutgoingMessageType.NONE, false,
				null, null, null, null, null, null,today, tomorrow, null, null, null, null , null, null);
		assertThrows(ConstraintViolationException.class, () -> sut.create(subscription));
	}

	@Test
	void createSubscriptionWithNullMessageType() {
		SubscriptionDto subscription = SubscriptionTestHelper.createManualSubscriptionDto( SUBSCR_ID, SUBSCR_NAME, Boolean.TRUE, null, false,
				null, null, null, null, null, null,today, tomorrow, null, null, null, null, null, null);
		assertThrows(ConstraintViolationException.class, () -> sut.create(subscription));
	}

	@Test
	void createSubscriptionWithFAQueryMessageTypeAndInvalidSubscriber() {
		SubscriptionDto subscription = SubscriptionTestHelper.createSubscriptionDto( SUBSCR_ID, SUBSCR_NAME, Boolean.TRUE, OutgoingMessageType.FA_QUERY, false,
				null, null, null, true, 1, SubscriptionTimeUnit.DAYS, true, TriggerType.SCHEDULER, 1, SubscriptionTimeUnit.DAYS, "12:00", new Date(), new Date());
		assertThrows(ConstraintViolationException.class, () -> sut.create(subscription));
	}

	@Test
	void createSubscriptionWithFAQueryMessageTypeAndInvalidOutput() {
		SubscriptionDto subscription = SubscriptionTestHelper.createSubscriptionDto( SUBSCR_ID, SUBSCR_NAME, Boolean.TRUE, OutgoingMessageType.FA_QUERY, false,
				ORGANISATION_ID, ENDPOINT_ID, CHANNEL_ID, null, null, null,null, TriggerType.SCHEDULER, 1, SubscriptionTimeUnit.DAYS, "12:00", new Date(), new Date());
		assertThrows(ConstraintViolationException.class, () -> sut.create(subscription));
	}

	@Test
	void createSubscriptionWithFAQueryMessageTypeAndSchedulerTriggerTypeAndInvalidExecution() {
		SubscriptionDto subscription = SubscriptionTestHelper.createSubscriptionDto( SUBSCR_ID, SUBSCR_NAME, Boolean.TRUE, OutgoingMessageType.FA_QUERY, false,
				ORGANISATION_ID, ENDPOINT_ID, CHANNEL_ID, true, 1, SubscriptionTimeUnit.DAYS,true, TriggerType.SCHEDULER, null, null, null, null, null);
		assertThrows(ConstraintViolationException.class, () -> sut.create(subscription));
	}

	@Test
	void createSubscriptionWithFAQueryMessageTypeAndInvalidNegativeHistory() {
		SubscriptionDto subscription = SubscriptionTestHelper.createSubscriptionDto( SUBSCR_ID, SUBSCR_NAME, Boolean.TRUE, OutgoingMessageType.FA_QUERY, false,
				ORGANISATION_ID, ENDPOINT_ID, CHANNEL_ID, true, -1, SubscriptionTimeUnit.DAYS, true, TriggerType.SCHEDULER, 1, SubscriptionTimeUnit.DAYS, "12:00", null, null);
		assertThrows(ConstraintViolationException.class, () -> sut.create(subscription));
	}

	@Test
	void createSubscriptionWithFAReportMessageTypeAndInvalidSubscriber() {
		SubscriptionDto subscription = SubscriptionTestHelper.createSubscriptionDto( SUBSCR_ID, SUBSCR_NAME, Boolean.TRUE, OutgoingMessageType.FA_REPORT, false,
				null, null, null, true, 1, SubscriptionTimeUnit.DAYS, true, TriggerType.SCHEDULER, 1, SubscriptionTimeUnit.DAYS, "12:00", new Date(), new Date());
		assertThrows(ConstraintViolationException.class, () -> sut.create(subscription));
	}

	@Test
	void createSubscriptionWithFAReportMessageTypeAndInvalidOutput() {
		SubscriptionDto subscription = SubscriptionTestHelper.createSubscriptionDto( SUBSCR_ID, SUBSCR_NAME, Boolean.TRUE, OutgoingMessageType.FA_REPORT, false,
				ORGANISATION_ID, ENDPOINT_ID, CHANNEL_ID, null, null, null,null, TriggerType.SCHEDULER, 1, SubscriptionTimeUnit.DAYS, "12:00", new Date(), new Date());
		assertThrows(ConstraintViolationException.class, () -> sut.create(subscription));
	}

	@Test
	void createSubscriptionWithFAReportMessageTypeAndSchedulerTriggerTypeAndInvalidExecution() {
		SubscriptionDto subscription = SubscriptionTestHelper.createSubscriptionDto( SUBSCR_ID, SUBSCR_NAME, Boolean.TRUE, OutgoingMessageType.FA_REPORT, false,
				ORGANISATION_ID, ENDPOINT_ID, CHANNEL_ID, true, 1, SubscriptionTimeUnit.DAYS,true, TriggerType.SCHEDULER, null, null, null, null, null);
		assertThrows(ConstraintViolationException.class, () -> sut.create(subscription));
	}

	@Test
	void createSubscriptionWithFAReportMessageTypeAndInvalidNegativeHistory() {
		SubscriptionDto subscription = SubscriptionTestHelper.createSubscriptionDto( SUBSCR_ID, SUBSCR_NAME, Boolean.TRUE, OutgoingMessageType.FA_REPORT, false,
				ORGANISATION_ID, ENDPOINT_ID, CHANNEL_ID, true, -1, SubscriptionTimeUnit.DAYS, true, TriggerType.SCHEDULER, 1, SubscriptionTimeUnit.DAYS, "12:00", null, null);
		assertThrows(ConstraintViolationException.class, () -> sut.create(subscription));
	}

	@Test
	void createSubscriptionWithTriggerTypeSchedulerAndInvalidTimeExpression() {
		SubscriptionDto subscription = SubscriptionTestHelper.createSubscriptionDto( SUBSCR_ID, SUBSCR_NAME, Boolean.TRUE, OutgoingMessageType.FA_QUERY, false,
				ORGANISATION_ID, ENDPOINT_ID, CHANNEL_ID, true, 1, SubscriptionTimeUnit.DAYS,true, TriggerType.SCHEDULER, 1, SubscriptionTimeUnit.DAYS, "120:00", null, null);
		assertThrows(ConstraintViolationException.class, () -> sut.create(subscription));
	}

	@Test
	void createValidSubscriptionWithNoneMessageTypeAndNullSubscriber() {
		SubscriptionDto dto = SubscriptionTestHelper.createSubscriptionDto( SUBSCR_ID, SUBSCR_NAME, Boolean.TRUE, OutgoingMessageType.NONE, false,
				null, null, null, true, 1, SubscriptionTimeUnit.DAYS,true, TriggerType.SCHEDULER, 1, SubscriptionTimeUnit.DAYS, "12:00", new Date(), new Date());

		when(subscriptionDAO.createEntity(any())).thenAnswer(iom -> iom.getArgument(0));

		assertDoesNotThrow(() -> sut.create(dto));
	}

	@Test
    void createSubscriptionWithValidEmailConfig() {
        SubscriptionDto subscription = SubscriptionTestHelper.createManualSubscriptionDtoWithEmailConfig( SUBSCR_ID, SUBSCR_NAME, Boolean.TRUE, OutgoingMessageType.NONE, true,
                null, null, null, null, 12, SubscriptionTimeUnit.HOURS, null, null, null, null,
				null, null, null, null, EMAIL_BODY, false, true, PASSWORD, true, false);

        when(subscriptionDAO.createEntity(any())).thenAnswer(iom -> iom.getArgument(0));

        assertDoesNotThrow(() -> sut.create(subscription));
    }

	@Test
    void createSubscriptionWithHasEmailNull() {
        SubscriptionDto subscription = SubscriptionTestHelper.createManualSubscriptionDtoWithEmailConfig( SUBSCR_ID, SUBSCR_NAME, Boolean.TRUE, OutgoingMessageType.NONE, null,
                null, null, null, null, 12, SubscriptionTimeUnit.HOURS, null, null, null, null,
                null, null, null, null, null , null , null, null , null, null);
        assertThrows(ConstraintViolationException.class, () -> sut.create(subscription));
    }

	@Test
    void createSubscriptionWithHasEmailTrueAndNullEmailConfiguration() {
        SubscriptionDto subscription = SubscriptionTestHelper.createManualSubscriptionDtoWithEmailConfig( SUBSCR_ID, SUBSCR_NAME, Boolean.TRUE, OutgoingMessageType.NONE, true,
                null, null, null, null, 12, SubscriptionTimeUnit.HOURS,null,  null, null, null, null,
                null, null, null, null, null , null , null , null, null);
        assertThrows(ConstraintViolationException.class, () -> sut.create(subscription));
    }

	@Test
    void createSubscriptionWithHasEmailTrueAndNullEmailBody() {
        SubscriptionDto subscription = SubscriptionTestHelper.createManualSubscriptionDtoWithEmailConfig( SUBSCR_ID, SUBSCR_NAME, Boolean.TRUE, OutgoingMessageType.NONE, true,
                null, null, null, null, 12, SubscriptionTimeUnit.HOURS, null,null, null, null, null, null, null, null,
                null, null, null, null, null , null);
        assertThrows(ConstraintViolationException.class, () -> sut.create(subscription));
    }

    @Test
    void createSubscriptionWithHasEmailFalseAndNullEmailConfiguration() {
		SubscriptionDto subscription = SubscriptionTestHelper.createManualSubscriptionDtoWithEmailConfig( SUBSCR_ID, SUBSCR_NAME, Boolean.TRUE, OutgoingMessageType.NONE, false,
				null, null, null, null, 12, SubscriptionTimeUnit.HOURS, null,null, null, null, null, null, null, null,
				null, null, null, null, null , null);

		when(subscriptionDAO.createEntity(any())).thenAnswer(iom -> iom.getArgument(0));

		assertDoesNotThrow(() -> sut.create(subscription));
    }


	@Test
	void createSubscriptionWithFAQueryMessageTypeAndInvalidOutputAndWithHasEmailFalseAndNullEmailConfiguration() {
		SubscriptionDto subscription = SubscriptionTestHelper.createSubscriptionDtoWithEmailConfig( SUBSCR_ID, SUBSCR_NAME, Boolean.TRUE, OutgoingMessageType.FA_QUERY, false,
				ORGANISATION_ID, ENDPOINT_ID, CHANNEL_ID, null, null, null,null, TriggerType.SCHEDULER, 1, SubscriptionTimeUnit.DAYS, "12:00", new Date(), new Date(),
		null, null, null, null, null , null);
		assertThrows(ConstraintViolationException.class, () -> sut.create(subscription));
	}



	@Test
	void createSubscriptionWithFAReportMessageTypeAndInvalidOutputAndWithHasEmailFalseAndNullEmailConfiguration() {
		SubscriptionDto subscription = SubscriptionTestHelper.createSubscriptionDtoWithEmailConfig( SUBSCR_ID, SUBSCR_NAME, Boolean.TRUE, OutgoingMessageType.FA_REPORT, false,
				ORGANISATION_ID, ENDPOINT_ID, CHANNEL_ID, null, null, null,null, TriggerType.SCHEDULER, 1, SubscriptionTimeUnit.DAYS, "12:00", new Date(), new Date(),
		null, null, null, null, null , null);
		assertThrows(ConstraintViolationException.class, () -> sut.create(subscription));
	}

    @Test
    void createSubscriptionWithHasEmailFalseAndNullEmailBody() {
		SubscriptionDto subscription = SubscriptionTestHelper.createManualSubscriptionDtoWithEmailConfig( SUBSCR_ID, SUBSCR_NAME, Boolean.TRUE, OutgoingMessageType.NONE, true,
				null, null, null, null, 15, SubscriptionTimeUnit.HOURS,  null, null, null, null,
				null, null, null, null, null , null , null ,null, null , null);
		assertThrows(ConstraintViolationException.class, () -> sut.create(subscription));
    }

    @Test
    void createSubscriptionWithHasEmailTrueAndNullPassword() {
		SubscriptionDto subscription = SubscriptionTestHelper.createManualSubscriptionDtoWithEmailConfig( SUBSCR_ID, SUBSCR_NAME, Boolean.TRUE, OutgoingMessageType.NONE, true,
				null, null, null, null, 12, SubscriptionTimeUnit.HOURS,null,null, null, null, null, null, null, null,
				EMAIL_BODY, false, true, null, true, false );

		when(subscriptionDAO.createEntity(any())).thenAnswer(iom -> iom.getArgument(0));

		assertDoesNotThrow(() -> sut.create(subscription));
    }

    @Test
    void createSubscriptionWithHasEmailTrueAndEmptyPassword() {
		SubscriptionDto subscription = SubscriptionTestHelper.createManualSubscriptionDtoWithEmailConfig( SUBSCR_ID, SUBSCR_NAME, Boolean.TRUE, OutgoingMessageType.NONE, true,
				null, null, null, null, 12, SubscriptionTimeUnit.HOURS, null, null, null,null, null, null, null, null,
				EMAIL_BODY, false, true, "", true, false );

		when(subscriptionDAO.createEntity(any())).thenAnswer(iom -> iom.getArgument(0));

		assertDoesNotThrow(() -> sut.create(subscription));
    }
    
    @Test
	void activateDeactivatedSubscription() {
		SubscriptionDto subscription = SubscriptionTestHelper.createManualSubscriptionDtoWithEmailConfig( SUBSCR_ID, SUBSCR_NAME, Boolean.FALSE, OutgoingMessageType.NONE, true,
				null, null, null, null, 12, SubscriptionTimeUnit.HOURS, null, null, null,null, null, null, null, null,
				EMAIL_BODY, false, true, "", true, false );

		SubscriptionEntity subscriptionEntity = mapper.mapDtoToEntity(subscription);
		subscriptionEntity.setActive(false);
		when(subscriptionDAO.findById(any())).thenReturn(subscriptionEntity);
		sut.setSubscriptionActive(subscriptionEntity.getId(), true);
		
		assertTrue(subscriptionEntity.isActive());
	}
    
    @Test
	void deactivateActivatedSubscription() {
		SubscriptionDto subscription = SubscriptionTestHelper.createManualSubscriptionDtoWithEmailConfig( SUBSCR_ID, SUBSCR_NAME, Boolean.FALSE, OutgoingMessageType.NONE, true,
				null, null, null, null, 12, SubscriptionTimeUnit.HOURS, null, null, null,null, null, null, null, null,
				EMAIL_BODY, false, true, "", true, false );

		SubscriptionEntity subscriptionEntity = mapper.mapDtoToEntity(subscription);
		subscriptionEntity.setActive(true);
		when(subscriptionDAO.findById(any())).thenReturn(subscriptionEntity);
		sut.setSubscriptionActive(subscriptionEntity.getId(), false);
		
		assertFalse(subscriptionEntity.isActive());
	}
    
    @Test
	void deactivateDeactivatedSubscription() {
		SubscriptionDto subscription = SubscriptionTestHelper.createManualSubscriptionDtoWithEmailConfig( SUBSCR_ID, SUBSCR_NAME, Boolean.FALSE, OutgoingMessageType.NONE, true,
				null, null, null, null, 12, SubscriptionTimeUnit.HOURS, null, null, null,null, null, null, null, null,
				EMAIL_BODY, false, true, "", true, false );

		SubscriptionEntity subscriptionEntity = mapper.mapDtoToEntity(subscription);
		subscriptionEntity.setActive(false);
		when(subscriptionDAO.findById(any())).thenReturn(subscriptionEntity);
		sut.setSubscriptionActive(subscriptionEntity.getId(), false);
		
		assertFalse(subscriptionEntity.isActive());
	}

	@Test
	void activateActivatedSubscription() {
		SubscriptionDto subscription = SubscriptionTestHelper.createManualSubscriptionDtoWithEmailConfig( SUBSCR_ID, SUBSCR_NAME, Boolean.FALSE, OutgoingMessageType.NONE, true,
				null, null, null, null, 12, SubscriptionTimeUnit.HOURS, null, null, null,null, null, null, null, null,
				EMAIL_BODY, false, true, "", true, false );

		SubscriptionEntity subscriptionEntity = mapper.mapDtoToEntity(subscription);
		subscriptionEntity.setActive(true);
		when(subscriptionDAO.findById(any())).thenReturn(subscriptionEntity);
		sut.setSubscriptionActive(subscriptionEntity.getId(), true);

		assertTrue(subscriptionEntity.isActive());
	}
	
    @Test
	void createScheduledSubscription() {
		SubscriptionDto subscription = SubscriptionTestHelper.createManualSubscriptionDtoWithEmailConfig( SUBSCR_ID, SUBSCR_NAME, Boolean.TRUE, OutgoingMessageType.NONE, true,
				null, null, null, null, 12, SubscriptionTimeUnit.HOURS, null, null, null,null, null, null, null, null,
				EMAIL_BODY, false, true, "", true, false );
		when(subscriptionDAO.createEntity(any())).thenAnswer(iom -> iom.getArgument(0));
		subscription.getExecution().setTriggerType(TriggerType.SCHEDULER);
		subscription.getExecution().setFrequency(1);
		subscription.getExecution().setFrequencyUnit(SubscriptionTimeUnit.DAYS);
		subscription.getExecution().setTimeExpression("22:00");
		assertDoesNotThrow(() -> sut.create(subscription));

		subscription.getExecution().setFrequency(null);
		assertThrows(ConstraintViolationException.class, () -> sut.create(subscription));
		subscription.getExecution().setFrequency(1);

		subscription.getExecution().setFrequencyUnit(null);
		assertThrows(ConstraintViolationException.class, () -> sut.create(subscription));
		subscription.getExecution().setFrequencyUnit(SubscriptionTimeUnit.DAYS);

		subscription.getExecution().setFrequency(0);
		assertThrows(ConstraintViolationException.class, () -> sut.create(subscription));
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

	@ParameterizedTest
	@MethodSource("testCreateWithExpressionParamsInput")
	void testCreate(String timeExpression, String expectedNextScheduledExecution) {
		SubscriptionDto dto = SubscriptionTestHelper.createSubscriptionDtoWithEmailConfig( SUBSCR_ID, SUBSCR_NAME, Boolean.TRUE, OutgoingMessageType.FA_QUERY, true,
				ORGANISATION_ID, ENDPOINT_ID, CHANNEL_ID, true, 1, SubscriptionTimeUnit.DAYS,true, TriggerType.SCHEDULER, 1, SubscriptionTimeUnit.DAYS, timeExpression, new Date(), new Date(),
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
		assertEquals(SubscriptionTimeUnit.DAYS, result.getOutput().getHistoryUnit());
		//assertEquals(0,dto.getStartDate().toInstant().truncatedTo(ChronoUnit.SECONDS).compareTo(result.getStartDate().toInstant().truncatedTo(ChronoUnit.SECONDS)));
		assertEquals(dto.getStartDate(),result.getStartDate());
		assertEquals(EMAIL_BODY, result.getOutput().getEmailConfiguration().getBody());
		assertEquals(true, result.getOutput().getEmailConfiguration().getIsPdf());
		assertEquals(true, result.getOutput().getEmailConfiguration().getZipAttachments());
		assertEquals(PASSWORD_PLACEHOLDER, result.getOutput().getEmailConfiguration().getPassword());
		assertEquals(true, result.getOutput().getEmailConfiguration().getPasswordIsPlaceholder());
		assertEquals(false, result.getOutput().getEmailConfiguration().getIsXml());

		ArgumentCaptor<SubscriptionEntity> captor = ArgumentCaptor.forClass(SubscriptionEntity.class);
		verify(subscriptionDAO).createEntity(captor.capture());
		SubscriptionEntity subscription = captor.getValue();
		assertFalse(subscription.getHasAreas());
		assertFalse(subscription.getHasAssets());
		assertFalse(subscription.getHasSenders());
		assertFalse(subscription.getHasStartActivities());
		assertFalse(subscription.getHasStopActivities());
		assertNotNull(subscription.getExecution().getNextScheduledExecution());
		assert(subscription.getExecution().getNextScheduledExecution().toInstant().equals(LocalDateTime.parse(expectedNextScheduledExecution).toInstant(ZoneOffset.UTC)));
	}

	protected static Stream<Arguments> testCreateWithExpressionParamsInput() {
		return Stream.of(
				Arguments.of("12:00", "2020-05-05T12:00:00"), // expected date/time is after todays current time (dateTimeService.getNowAsInstant())
				Arguments.of("10:00", "2020-05-06T10:00:00") // expected date/time is before todays current time (dateTimeService.getNowAsInstant())
		);
	}

	@Test
	void testCreateWithNonEmptyStartConditions() {
		SubscriptionDto dto = SubscriptionTestHelper.createSubscriptionDtoWithEmailConfig( SUBSCR_ID, SUBSCR_NAME, Boolean.TRUE, OutgoingMessageType.FA_QUERY, true,
				ORGANISATION_ID, ENDPOINT_ID, CHANNEL_ID, true, 1, SubscriptionTimeUnit.DAYS,true, TriggerType.SCHEDULER, 1, SubscriptionTimeUnit.DAYS, "12:00", new Date(), new Date(),
				EMAIL_BODY, true, true, PASSWORD, false, false);
		dto.setAssets(Collections.singleton(new AssetDto(null, "guid", "name", AssetType.ASSET)));
		dto.setAreas(Collections.singleton(new AreaDto(null, 1L, AreaType.USERAREA)));
		dto.setStartActivities(Collections.singleton(new SubscriptionFishingActivityDto(SubscriptionFaReportDocumentType.DECLARATION, "val")));
		dto.setStopActivities(Collections.singleton(new SubscriptionFishingActivityDto(SubscriptionFaReportDocumentType.DECLARATION, "val1")));
		dto.setSenders(Collections.singleton(new SubscriptionSubscriberDto(1L,2L,3L)));
		when(subscriptionDAO.createEntity(any())).thenAnswer(iom -> iom.getArgument(0));
		when(subscriptionDAO.createEmailBodyEntity(any())).thenAnswer(iom -> iom.getArgument(0));

		sut.create(dto);

		ArgumentCaptor<SubscriptionEntity> captor = ArgumentCaptor.forClass(SubscriptionEntity.class);
		verify(subscriptionDAO).createEntity(captor.capture());
		SubscriptionEntity subscription = captor.getValue();
		assertTrue(subscription.getHasAreas());
		assertTrue(subscription.getHasAssets());
		assertTrue(subscription.getHasSenders());
		assertTrue(subscription.getHasStartActivities());
		assertTrue(subscription.getHasStopActivities());
	}

	@Test
	void createSubscriptionWithAssetCriteria() {
		SubscriptionDto dto = SubscriptionTestHelper.createSubscriptionDtoWithEmailConfig( SUBSCR_ID, SUBSCR_NAME, Boolean.TRUE, OutgoingMessageType.FA_QUERY, true,
				ORGANISATION_ID, ENDPOINT_ID, CHANNEL_ID, true, 1, SubscriptionTimeUnit.DAYS,true, TriggerType.SCHEDULER, 1, SubscriptionTimeUnit.DAYS, "12:00", new Date(), new Date(),
				EMAIL_BODY, true, true, PASSWORD, false, false);

		Set<AssetDto> assetDtoSet = new HashSet<>();
		assetDtoSet.add(new AssetDto(null,"0001", "name1", AssetType.ASSET));
		assetDtoSet.add(new AssetDto(null,"0002", "name2", AssetType.ASSET));
		assetDtoSet.add(new AssetDto(null,"0003", "name3", AssetType.VGROUP));
		assetDtoSet.add(new AssetDto(null,"0004", "name4", AssetType.VGROUP));
		assetDtoSet.add(new AssetDto(null,"0005", "name5", AssetType.VGROUP));
		dto.setAssets(assetDtoSet);

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
		assertEquals(SubscriptionTimeUnit.DAYS, result.getOutput().getHistoryUnit());
		//assertEquals(0,dto.getStartDate().toInstant().truncatedTo(ChronoUnit.SECONDS).compareTo(result.getStartDate().toInstant().truncatedTo(ChronoUnit.SECONDS)));
		assertEquals(dto.getStartDate(),result.getStartDate());
		assertEquals(EMAIL_BODY, result.getOutput().getEmailConfiguration().getBody());
		assertEquals(true, result.getOutput().getEmailConfiguration().getIsPdf());
		assertEquals(true, result.getOutput().getEmailConfiguration().getZipAttachments());
		assertEquals(PASSWORD_PLACEHOLDER, result.getOutput().getEmailConfiguration().getPassword());
		assertEquals(true, result.getOutput().getEmailConfiguration().getPasswordIsPlaceholder());
		assertEquals(false, result.getOutput().getEmailConfiguration().getIsXml());
		assertEquals(2, result.getAssets().stream().filter(assetDto -> assetDto.getType().equals(AssetType.ASSET)).count());
		assertEquals(3, result.getAssets().stream().filter(assetDto -> assetDto.getType().equals(AssetType.VGROUP)).count());
	}

	@Test
	void createSubscriptionWithEmailConfigAndNoPassword() {
		SubscriptionDto dto = SubscriptionTestHelper.createSubscriptionDtoWithEmailConfig( SUBSCR_ID, SUBSCR_NAME, Boolean.TRUE, OutgoingMessageType.FA_QUERY, true,
				ORGANISATION_ID, ENDPOINT_ID, CHANNEL_ID, true, 1, SubscriptionTimeUnit.DAYS,true, TriggerType.SCHEDULER, 1, SubscriptionTimeUnit.DAYS, "12:00", new Date(), new Date(),
				EMAIL_BODY, true, true, null, false, false);

		when(subscriptionDAO.createEntity(any())).thenAnswer(iom -> iom.getArgument(0));
		when(subscriptionDAO.createEmailBodyEntity(any())).thenAnswer(iom -> iom.getArgument(0));

		SubscriptionDto result = sut.create(dto);

		assertNull(result.getOutput().getEmailConfiguration().getPassword());
		assertEquals(true, result.getOutput().getEmailConfiguration().getPasswordIsPlaceholder());
	}

	@Test
	void testPrepareManualSubscriptionRequest() {
		SubscriptionDto subscription = SubscriptionTestHelper.createSubscriptionDto( SUBSCR_ID, null, null, OutgoingMessageType.FA_QUERY, null,
				ORGANISATION_ID, ENDPOINT_ID, CHANNEL_ID, null, null, null,null, null, null, null, null, new Date(), new Date());
		SubscriptionDto manualSubscriptionDto = sut.prepareManualRequest(subscription);
		assertEquals(Boolean.TRUE, manualSubscriptionDto.getActive());
		assertEquals(Boolean.FALSE, manualSubscriptionDto.getOutput().getHasEmail());
		assertEquals(Boolean.FALSE, manualSubscriptionDto.getOutput().getLogbook());
		assert(manualSubscriptionDto.getName() != null && !manualSubscriptionDto.getName().isEmpty());
	}

	@ParameterizedTest
	@MethodSource("testCreateManualSubscriptionParams")
	void testCreateManualSubscription(boolean includeAssets, boolean includeAssetGroups, boolean includeAreas) {
		SubscriptionDto dto = SubscriptionTestHelper.createManualSubscriptionDto(SUBSCR_ID, SUBSCR_NAME, Boolean.TRUE, OutgoingMessageType.FA_QUERY,
				ORGANISATION_ID, ENDPOINT_ID, CHANNEL_ID, true, 1, SubscriptionTimeUnit.DAYS, true, new Date(), new Date(),
				includeAssets, includeAssetGroups);

		when(subscriptionDAO.createEntity(any())).thenAnswer(iom -> iom.getArgument(0));

		List<String> guids = dto.getAssets().stream().filter(a -> "ASSET".equals(a.getType().toString())).map(AssetDto::getGuid).sorted(Comparator.naturalOrder()).collect(Collectors.toList());
		if (includeAssets) {
			AssetHistGuidIdWithVesselIdentifiers mockIds = mock(AssetHistGuidIdWithVesselIdentifiers.class);
			when(assetSender.findMultipleVesselIdentifiers(guids)).thenReturn(Collections.singletonList(mockIds));
		}
		if (includeAreas) {
			dto.setAreas(Collections.singleton(new AreaDto(33L, 11L, AreaType.GFCM)));
		}

		SubscriptionDto result = sut.createManual(dto);

		assertNotNull(result);
		assertEquals(SUBSCR_ID, result.getId());
		assertEquals(SUBSCR_NAME, result.getName());
		assertEquals(Boolean.TRUE, result.getActive());
		assertEquals(ORGANISATION_ID, result.getOutput().getSubscriber().getOrganisationId());
		assertEquals(ENDPOINT_ID, result.getOutput().getSubscriber().getEndpointId());
		assertEquals(CHANNEL_ID, result.getOutput().getSubscriber().getChannelId());
		assertEquals(TriggerType.MANUAL, result.getExecution().getTriggerType());
		assertEquals(0, result.getExecution().getFrequency());
		assertEquals(true, result.getExecution().getImmediate());
		assertEquals(SubscriptionTimeUnit.DAYS, result.getExecution().getFrequencyUnit());
		assertEquals(OutgoingMessageType.FA_QUERY, result.getOutput().getMessageType());
		assertEquals(0, Instant.now().truncatedTo(ChronoUnit.DAYS).compareTo(result.getStartDate().toInstant().truncatedTo(ChronoUnit.DAYS)));
		assertEquals(0, Instant.now().plus(365, ChronoUnit.DAYS).truncatedTo(ChronoUnit.DAYS).compareTo(result.getEndDate().toInstant().truncatedTo(ChronoUnit.DAYS)));

		if (includeAssets) {
			@SuppressWarnings("unchecked")
			ArgumentCaptor<List<String>> captorForVesselGuids = ArgumentCaptor.forClass(List.class);
			verify(assetSender).findMultipleVesselIdentifiers(captorForVesselGuids.capture());
			List<String> sentGuids = captorForVesselGuids.getValue();
			assert (sentGuids.containsAll(guids));
			assert (sentGuids.size() == 2);
		}
		ArgumentCaptor<SubscriptionEntity> captor = ArgumentCaptor.forClass(SubscriptionEntity.class);
		verify(subscriptionDAO).createEntity(captor.capture());
		SubscriptionEntity subscription = captor.getValue();
		assertEquals(SUBSCR_ID, subscription.getId());
		assertEquals(includeAssets || includeAssetGroups, Boolean.TRUE.equals(subscription.getHasAssets()));
		assertEquals(includeAreas, Boolean.TRUE.equals(subscription.getHasAreas()));

		if (includeAssets || includeAssetGroups) {
			int numberOfInvocations = (includeAssets ? 1 : 0) + (includeAssetGroups ? 2 : 0);
			ArgumentCaptor<AssetPageRetrievalMessage> captor1 = ArgumentCaptor.forClass(AssetPageRetrievalMessage.class);
			verify(subscriptionSender, times(numberOfInvocations)).sendAssetPageRetrievalMessageSameTx(captor1.capture());
			Iterator<AssetPageRetrievalMessage> messages = captor1.getAllValues().iterator();
			if (includeAssetGroups) {
				verifyMessageContents(messages.next(), "11ac3628-9a2e-4a21-a9eb-3931e5a1ce53", 1L);
				verifyMessageContents(messages.next(), "22ac3628-9a2e-4a21-a9eb-3931e5a1ce54", 1L);
			}
			if (includeAssets) {
				verifyMainAssets(messages.next(), 1L);
			}
			assertFalse(messages.hasNext());
		}

		if (includeAreas) {
			assertEquals(1, subscription.getAreas().size());
			assertEquals(33L, subscription.getAreas().iterator().next().getId());
			assertEquals(11L, subscription.getAreas().iterator().next().getGid());
			assertEquals(AreaType.GFCM, subscription.getAreas().iterator().next().getAreaType());
		}
	}

	protected static Stream<Arguments> testCreateManualSubscriptionParams() {
		return Stream.of(
				Arguments.of(false, false, false),
				Arguments.of(true, false, false),
				Arguments.of(false, true, true),
				Arguments.of(true, true, true)
		);
	}

	private void verifyMainAssets(AssetPageRetrievalMessage message, long pageNumber) {
		assertFalse(message.isGroup());
		assertEquals("mainAssets", message.getAssetGroupGuid());
		assertEquals(pageNumber, message.getPageNumber());
		assertTrue(message.getPageSize() > 1L);
	}

	private void verifyMessageContents(AssetPageRetrievalMessage message, String assetGroupGuid, long pageNumber) {
		assertEquals(assetGroupGuid, message.getAssetGroupGuid());
		assertEquals(pageNumber, message.getPageNumber());
		assertTrue(message.getPageSize() > 1L);
	}

	@Test
	void updateSubscriptionWithEmailConfigAndPasswordIsPlaceHolderTrueAndStoredPasswordNotEmpty() {
		SubscriptionDto dto = SubscriptionTestHelper.createSubscriptionDtoWithEmailConfig( SUBSCR_ID, SUBSCR_NAME, Boolean.TRUE, OutgoingMessageType.FA_QUERY, true,
				ORGANISATION_ID, ENDPOINT_ID, CHANNEL_ID, true, 1, SubscriptionTimeUnit.DAYS,true, TriggerType.SCHEDULER, 1, SubscriptionTimeUnit.DAYS, "12:00", new Date(), new Date(),
				EMAIL_BODY, true, true, "xxx", true, false);

		when(subscriptionDAO.findById(17L)).thenReturn(new SubscriptionEntity());
		when(subscriptionDAO.update(any())).thenAnswer(iom -> iom.getArgument(0));
		when(subscriptionDAO.updateEmailBodyEntity(any())).thenAnswer(iom -> iom.getArgument(0));
		when(subscriptionDAO.getEmailConfigurationPassword(any())).thenReturn("abcd1234");

		SubscriptionDto result = sut.update(dto);

		assertEquals("********", result.getOutput().getEmailConfiguration().getPassword());
		assertEquals(true, result.getOutput().getEmailConfiguration().getPasswordIsPlaceholder());
	}


	@Test
	void updateSubscriptionWithEmailConfigAndPasswordIsPlaceHolderTrueAndStoredPasswordNull() {
		SubscriptionDto dto = SubscriptionTestHelper.createSubscriptionDtoWithEmailConfig( SUBSCR_ID, SUBSCR_NAME, Boolean.TRUE, OutgoingMessageType.FA_QUERY, true,
				ORGANISATION_ID, ENDPOINT_ID, CHANNEL_ID, true, 1, SubscriptionTimeUnit.DAYS,true, TriggerType.SCHEDULER, 1, SubscriptionTimeUnit.DAYS, "12:00", new Date(), new Date(),
				EMAIL_BODY, true, true, "******", true, false);

		when(subscriptionDAO.findById(17L)).thenReturn(new SubscriptionEntity());
		when(subscriptionDAO.update(any())).thenAnswer(iom -> iom.getArgument(0));
		when(subscriptionDAO.updateEmailBodyEntity(any())).thenAnswer(iom -> iom.getArgument(0));
		when(subscriptionDAO.getEmailConfigurationPassword(any())).thenReturn(null);

		SubscriptionDto result = sut.update(dto);

		assertNull(result.getOutput().getEmailConfiguration().getPassword());
		assertEquals(true, result.getOutput().getEmailConfiguration().getPasswordIsPlaceholder());
	}

	@Test
	void updateSubscriptionWithEmailConfigAndPasswordIsPlaceHolderFalse() {
		SubscriptionDto dto = SubscriptionTestHelper.createSubscriptionDtoWithEmailConfig( SUBSCR_ID, SUBSCR_NAME, Boolean.TRUE, OutgoingMessageType.FA_QUERY, true,
				ORGANISATION_ID, ENDPOINT_ID, CHANNEL_ID, true, 1, SubscriptionTimeUnit.DAYS,true, TriggerType.SCHEDULER, 1, SubscriptionTimeUnit.DAYS, "12:00", new Date(), new Date(),
				EMAIL_BODY, true, true, "new_pass", false, false);

		when(subscriptionDAO.findById(17L)).thenReturn(new SubscriptionEntity());
		when(subscriptionDAO.update(any())).thenAnswer(iom -> iom.getArgument(0));
		when(subscriptionDAO.updateEmailBodyEntity(any())).thenAnswer(iom -> iom.getArgument(0));

		SubscriptionDto result = sut.update(dto);

		assertEquals("********", result.getOutput().getEmailConfiguration().getPassword());
		assertEquals(true, result.getOutput().getEmailConfiguration().getPasswordIsPlaceholder());
	}

	@Test
	void updateSubscriptionWithEmailConfigAndPasswordIsPlaceHolderFalseAndNewPasswordEmpty() {
		SubscriptionDto dto = SubscriptionTestHelper.createSubscriptionDtoWithEmailConfig( SUBSCR_ID, SUBSCR_NAME, Boolean.TRUE, OutgoingMessageType.FA_QUERY, true,
				ORGANISATION_ID, ENDPOINT_ID, CHANNEL_ID, true, 1, SubscriptionTimeUnit.DAYS,true, TriggerType.SCHEDULER, 1, SubscriptionTimeUnit.DAYS, "12:00", new Date(), new Date(),
				EMAIL_BODY, true, true, "", false, false);

		when(subscriptionDAO.findById(17L)).thenReturn(new SubscriptionEntity());
		when(subscriptionDAO.update(any())).thenAnswer(iom -> iom.getArgument(0));
		when(subscriptionDAO.updateEmailBodyEntity(any())).thenAnswer(iom -> iom.getArgument(0));

		SubscriptionDto result = sut.update(dto);

		assertNull(result.getOutput().getEmailConfiguration().getPassword());
		assertEquals(true, result.getOutput().getEmailConfiguration().getPasswordIsPlaceholder());
	}

	@Test
	void updateSubscriptionWithEmailConfigAndPasswordIsPlaceHolderFalseAndNewPasswordNull() {
		SubscriptionDto dto = SubscriptionTestHelper.createSubscriptionDtoWithEmailConfig( SUBSCR_ID, SUBSCR_NAME, Boolean.TRUE, OutgoingMessageType.FA_QUERY, true,
				ORGANISATION_ID, ENDPOINT_ID, CHANNEL_ID, true, 1, SubscriptionTimeUnit.DAYS,true, TriggerType.SCHEDULER, 1, SubscriptionTimeUnit.DAYS, "12:00", new Date(), new Date(),
				EMAIL_BODY, true, true, null, false, false);

		when(subscriptionDAO.findById(17L)).thenReturn(new SubscriptionEntity());
		when(subscriptionDAO.update(any())).thenAnswer(iom -> iom.getArgument(0));
		when(subscriptionDAO.updateEmailBodyEntity(any())).thenAnswer(iom -> iom.getArgument(0));

		SubscriptionDto result = sut.update(dto);

		assertNull(result.getOutput().getEmailConfiguration().getPassword());
		assertEquals(true, result.getOutput().getEmailConfiguration().getPasswordIsPlaceholder());
	}

	@Test
	void updateSubscriptionWithEmailConfigAndPasswordIsPlaceHolderNull() {
		SubscriptionDto dto = SubscriptionTestHelper.createSubscriptionDtoWithEmailConfig( SUBSCR_ID, SUBSCR_NAME, Boolean.TRUE, OutgoingMessageType.FA_QUERY, true,
				ORGANISATION_ID, ENDPOINT_ID, CHANNEL_ID, true, 1, SubscriptionTimeUnit.DAYS,true, TriggerType.SCHEDULER, 1, SubscriptionTimeUnit.DAYS, "12:00", new Date(), new Date(),
				EMAIL_BODY, true, true, null, null, false);

		when(subscriptionDAO.findById(17L)).thenReturn(new SubscriptionEntity());
		when(subscriptionDAO.update(any())).thenAnswer(iom -> iom.getArgument(0));
		when(subscriptionDAO.updateEmailBodyEntity(any())).thenAnswer(iom -> iom.getArgument(0));

		SubscriptionDto result = sut.update(dto);

		assertNull(result.getOutput().getEmailConfiguration().getPassword());
		assertEquals(true, result.getOutput().getEmailConfiguration().getPasswordIsPlaceholder());
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
		AreaEntity oldArea = new AreaEntity();
		oldArea.setGid(111L);
		oldArea.setAreaType(AreaType.USERAREA);
		subscription.getAreas().add(oldArea);
		AssetEntity originalAsset = makeAsset(ASSET_ID, ASSET_GUID, ASSET_NAME, CFR, IRCS, ICCAT, EXT_MARK, UVI);
		subscription.getAssets().add(originalAsset);
		subscription.getAssetGroups().add(makeAssetGroup(ASSET_GROUP_ID, ASSET_GROUP_GUID, ASSET_GROUP_NAME));
		when(subscriptionDAO.findById(SUBSCR_ID)).thenReturn(subscription);
		when(subscriptionDAO.update(subscription)).thenReturn(subscription);
		AssetHistGuidIdWithVesselIdentifiers ids = new AssetHistGuidIdWithVesselIdentifiers();
		ids.setAssetHistGuid(ASSET_NEW_GUID);
		VesselIdentifiersHolder idHolder = new VesselIdentifiersHolder();
		idHolder.setCfr(NEW_CFR);
		idHolder.setIrcs(NEW_IRCS);
		idHolder.setIccat(NEW_ICCAT);
		idHolder.setExtMark(NEW_EXT_MARK);
		idHolder.setUvi(NEW_UVI);
		ids.setIdentifiers(idHolder);
		doReturn(Collections.singletonList(ids)).when(assetSender).findMultipleVesselIdentifiers(eq(Collections.singletonList(ASSET_NEW_GUID)));

		SubscriptionDto result = sut.update(dto);

		assertNotNull(result);
		verify(subscriptionDAO).update(subscription);
		assertTrue(subscription.getHasAreas());
		assertTrue(subscription.getHasAssets());
		assertTrue(subscription.getHasSenders());
		assertFalse(subscription.getHasStartActivities());
		assertFalse(subscription.getHasStopActivities());
		verify(auditProducer).sendModuleMessage(any(), any());
		assertTrue(result.getAreas().stream().map(AreaDto::getGid).anyMatch(Long.valueOf(111L)::equals));
		assertEquals(2, subscription.getAssets().size());
		assertTrue(subscription.getAssets().stream().noneMatch(a -> a == originalAsset));
		AssetEntity updatedAsset = subscription.getAssets().stream().filter(a -> a.getId().equals(ASSET_ID)).findFirst().get();
		assertEquals(ASSET_GUID, updatedAsset.getGuid());
		assertEquals(ASSET_NAME, updatedAsset.getName());
		assertEquals(CFR, updatedAsset.getCfr());
		assertEquals(IRCS, updatedAsset.getIrcs());
		assertEquals(ICCAT, updatedAsset.getIccat());
		assertEquals(EXT_MARK, updatedAsset.getExtMark());
		assertEquals(UVI, updatedAsset.getUvi());
		AssetEntity addedAsset = subscription.getAssets().stream().filter(a -> a.getId() == null).findFirst().get();
		assertEquals(ASSET_NEW_GUID, addedAsset.getGuid());
		assertEquals(ASSET_NEW_NAME, addedAsset.getName());
		assertEquals(NEW_CFR, addedAsset.getCfr());
		assertEquals(NEW_IRCS, addedAsset.getIrcs());
		assertEquals(NEW_ICCAT, addedAsset.getIccat());
		assertEquals(NEW_EXT_MARK, addedAsset.getExtMark());
		assertEquals(NEW_UVI, addedAsset.getUvi());
		assertEquals(1, subscription.getAssetGroups().size());
		assertEquals(ASSET_GROUP_ID, subscription.getAssetGroups().iterator().next().getId());
		assertEquals(ASSET_GROUP_GUID, subscription.getAssetGroups().iterator().next().getGuid());
		assertEquals(ASSET_GROUP_NAME, subscription.getAssetGroups().iterator().next().getName());
	}

	@Test
	void testCreateSubscriptionWithNullAssetsAndNullAreas() {
		when(subscriptionDAO.createEntity(any())).thenAnswer(iom -> iom.getArgument(0));

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
		dto.setAreas(null);
		dto.setAssets(null);

		SubscriptionDto createdSubscription = sut.create(dto);
		assertEquals(Collections.emptySet(), createdSubscription.getAreas());
		assertEquals(Collections.emptySet(), createdSubscription.getAssets());
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
		dto.setAreas(new HashSet<>());
		AreaDto existingArea = new AreaDto();
		existingArea.setAreaType(AreaType.USERAREA);
		existingArea.setGid(111L);
		dto.getAreas().add(existingArea);
		AreaDto newArea = new AreaDto();
		newArea.setAreaType(AreaType.USERAREA);
		newArea.setGid(222L);
		dto.getAreas().add(newArea);
		dto.setAssets(new HashSet<>());
		dto.getAssets().add(new AssetDto(ASSET_ID, ASSET_GUID, ASSET_NAME, AssetType.ASSET));
		dto.getAssets().add(new AssetDto(null, ASSET_NEW_GUID, ASSET_NEW_NAME, AssetType.ASSET));
		dto.getAssets().add(new AssetDto(ASSET_GROUP_ID, ASSET_GROUP_GUID, ASSET_GROUP_NAME, AssetType.VGROUP));
		dto.setSenders(new HashSet<>());
		dto.getSenders().add(new SubscriptionSubscriberDto(1L,2L,3L));
		return dto;
	}

	@Test
	void testDelete() throws MessageException {
		sut.delete(SUBSCR_ID);
		verify(subscriptionDAO).delete(SUBSCR_ID);
		verify(auditProducer).sendModuleMessage(any(), any());
	}

	private AssetEntity makeAsset(Long id, String guid, String name, String cfr, String ircs, String iccat, String extMark, String uvi) {
		AssetEntity a = new AssetEntity();
		a.setId(id);
		a.setGuid(guid);
		a.setName(name);
		a.setCfr(cfr);
		a.setIrcs(ircs);
		a.setIccat(iccat);
		a.setExtMark(extMark);
		a.setUvi(uvi);
		return a;
	}

	private AssetGroupEntity makeAssetGroup(Long id, String guid, String name) {
		AssetGroupEntity group = new AssetGroupEntity();
		group.setId(id);
		group.setGuid(guid);
		group.setName(name);
		return group;
	}
}
