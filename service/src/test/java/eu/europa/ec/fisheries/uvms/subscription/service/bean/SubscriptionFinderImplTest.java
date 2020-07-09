package eu.europa.ec.fisheries.uvms.subscription.service.bean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.ActivityCriterion;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.SubscriptionSearchCriteria;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.SubscriptionSearchCriteria.AssetCriterion;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.SubscriptionSearchCriteria.SenderCriterion;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.AreaCriterion;

import eu.europa.ec.fisheries.uvms.subscription.service.dao.SubscriptionDao;
import eu.europa.ec.fisheries.wsdl.subscription.module.AreaType;
import eu.europa.fisheries.uvms.subscription.model.enums.AssetType;
import eu.europa.fisheries.uvms.subscription.model.enums.SubscriptionFaReportDocumentType;
import eu.europa.fisheries.uvms.subscription.model.enums.TriggerType;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for the {@link SubscriptionFinderImpl}.
 */
@EnableAutoWeld
@ExtendWith(MockitoExtension.class)
public class SubscriptionFinderImplTest {

	private static final Long ID = 1234L;

	@Produces @Mock
	private SubscriptionDao dao;

	@Inject
	private SubscriptionFinderImpl sut;

	@Test
	void testEmptyConstructor() {
		SubscriptionFinderImpl sut = new SubscriptionFinderImpl();
		assertNotNull(sut);
	}

	@Test
	void testFindSubscriptionsTriggeredByAreasNullOrEmpty() {
		assertTrue(sut.findTriggeredSubscriptions(null, null, null, null, ZonedDateTime.now(), null).isEmpty());
		assertTrue(sut.findTriggeredSubscriptions(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), null, ZonedDateTime.now(), null).isEmpty());
		verifyNoMoreInteractions(dao);
	}

	@Test
	void testFindSubscriptionsTriggeredByAreasAndAssets() {
		List<AreaCriterion> areas = Arrays.asList(new AreaCriterion(AreaType.USERAREA, 111L), new AreaCriterion(AreaType.PORT, 222L));
		List<AssetCriterion> assets = Arrays.asList(new AssetCriterion(AssetType.ASSET, "guid1"), new AssetCriterion(AssetType.VGROUP, "guid2"));
		SubscriptionSearchCriteria.SenderCriterion senders = new SubscriptionSearchCriteria.SenderCriterion(1L, 2L, 3L);
		ZonedDateTime validAt = ZonedDateTime.now();
		when(dao.listSubscriptions(any(SubscriptionSearchCriteria.class))).thenAnswer(iom -> {
			SubscriptionEntity subscription = new SubscriptionEntity();
			subscription.setId(456L);
			return Collections.singletonList(subscription);
		});
		List<SubscriptionEntity> result = sut.findTriggeredSubscriptions(areas, assets, Collections.emptyList(), senders, validAt, Collections.singleton(TriggerType.SCHEDULER));
		assertEquals(1, result.size());
		assertEquals(456L, result.get(0).getId());
		ArgumentCaptor<SubscriptionSearchCriteria> criteriaCaptor = ArgumentCaptor.forClass(SubscriptionSearchCriteria.class);
		verify(dao, times(3)).listSubscriptions(criteriaCaptor.capture());
		Map<String, SubscriptionSearchCriteria> classifiedCriteria = criteriaCaptor.getAllValues().stream().collect(Collectors.toMap(this::classifyCriteria, Function.identity()));

		SubscriptionSearchCriteria areasCriteria = classifiedCriteria.get("AREAS");
		assertTrue(areasCriteria.getActive());
		assertEquals(areas, areasCriteria.getInAnyArea());
		assertNull(areasCriteria.getAllowWithNoArea());
		assertEquals(assets, areasCriteria.getWithAnyAsset());
		assertTrue(areasCriteria.getAllowWithNoAsset());
		assertEquals(validAt, areasCriteria.getValidAt());
		assertEquals(senders, areasCriteria.getSender());
		assertEquals(1, areasCriteria.getWithAnyTriggerType().size());
		assertEquals(TriggerType.SCHEDULER, areasCriteria.getWithAnyTriggerType().iterator().next());

		SubscriptionSearchCriteria assetsCriteria = classifiedCriteria.get("ASSETS");
		assertTrue(assetsCriteria.getActive());
		assertEquals(areas, assetsCriteria.getInAnyArea());
		assertTrue(criteriaCaptor.getValue().getAllowWithNoArea());
		assertEquals(assets, assetsCriteria.getWithAnyAsset());
		assertNull(assetsCriteria.getAllowWithNoAsset());
		assertEquals(validAt, assetsCriteria.getValidAt());
		assertEquals(senders, assetsCriteria.getSender());
		assertEquals(1, assetsCriteria.getWithAnyTriggerType().size());
		assertEquals(TriggerType.SCHEDULER, assetsCriteria.getWithAnyTriggerType().iterator().next());

		SubscriptionSearchCriteria sendersCriteria = classifiedCriteria.get("SENDERS");
		assertTrue(sendersCriteria.getActive());
		assertEquals(areas, assetsCriteria.getInAnyArea());
		assertTrue(criteriaCaptor.getValue().getAllowWithNoArea());
		assertEquals(assets, assetsCriteria.getWithAnyAsset());
		assertNull(assetsCriteria.getAllowWithNoAsset());
		assertEquals(validAt, assetsCriteria.getValidAt());
		assertEquals(1, assetsCriteria.getWithAnyTriggerType().size());
		assertEquals(TriggerType.SCHEDULER, assetsCriteria.getWithAnyTriggerType().iterator().next());

	}

	private String classifyCriteria(SubscriptionSearchCriteria c) {
		if (c.getAllowWithNoArea() == null) {
			return "AREAS";
		} else if (c.getAllowWithNoAsset() == null) {
			return "ASSETS";
		} else if (c.getAllowWithNoSenders() == null) {
			return "SENDERS";
		} else {
			throw new IllegalArgumentException();
		}
	}

	@Test
	void testFindSubscriptionsTriggeredByAreas() {
		List<AreaCriterion> areas = Arrays.asList(new AreaCriterion(AreaType.USERAREA, 111L), new AreaCriterion(AreaType.PORT, 222L));
		ZonedDateTime validAt = ZonedDateTime.now();
		List<SubscriptionEntity> mockResult = Collections.emptyList();
		when(dao.listSubscriptions(any(SubscriptionSearchCriteria.class))).thenReturn(mockResult);
		List<SubscriptionEntity> result = sut.findTriggeredSubscriptions(areas, Collections.emptyList(),Collections.emptyList(), null, validAt, Collections.singleton(TriggerType.SCHEDULER));
		assertTrue(result.isEmpty());
		ArgumentCaptor<SubscriptionSearchCriteria> criteriaCaptor = ArgumentCaptor.forClass(SubscriptionSearchCriteria.class);
		verify(dao).listSubscriptions(criteriaCaptor.capture());
		assertTrue(criteriaCaptor.getValue().getActive());
		assertEquals(areas, criteriaCaptor.getValue().getInAnyArea());
		assertEquals(validAt, criteriaCaptor.getValue().getValidAt());
		assertEquals(1, criteriaCaptor.getValue().getWithAnyTriggerType().size());
		assertEquals(TriggerType.SCHEDULER, criteriaCaptor.getValue().getWithAnyTriggerType().iterator().next());
		assertNull(criteriaCaptor.getValue().getAllowWithNoArea());
		assertTrue(criteriaCaptor.getValue().getAllowWithNoStartActivity());
		assertTrue(criteriaCaptor.getValue().getAllowWithNoAsset());
	}

	@Test
	void testFindSubscriptionsTriggeredByAssets() {
		List<AssetCriterion> assets = Arrays.asList(new AssetCriterion(AssetType.ASSET, "guid1"), new AssetCriterion(AssetType.VGROUP, "guid2"));
		ZonedDateTime validAt = ZonedDateTime.now();
		List<SubscriptionEntity> mockResult = Collections.emptyList();
		when(dao.listSubscriptions(any(SubscriptionSearchCriteria.class))).thenReturn(mockResult);
		List<SubscriptionEntity> result = sut.findTriggeredSubscriptions(Collections.emptyList(), assets, Collections.emptyList(), null, validAt, Collections.singleton(TriggerType.SCHEDULER));
		assertTrue(result.isEmpty());
		ArgumentCaptor<SubscriptionSearchCriteria> criteriaCaptor = ArgumentCaptor.forClass(SubscriptionSearchCriteria.class);
		verify(dao).listSubscriptions(criteriaCaptor.capture());
		assertTrue(criteriaCaptor.getValue().getActive());
		assertEquals(assets, criteriaCaptor.getValue().getWithAnyAsset());
		assertEquals(validAt, criteriaCaptor.getValue().getValidAt());
		assertEquals(1, criteriaCaptor.getValue().getWithAnyTriggerType().size());
		assertEquals(TriggerType.SCHEDULER, criteriaCaptor.getValue().getWithAnyTriggerType().iterator().next());
		assertTrue(criteriaCaptor.getValue().getAllowWithNoArea());
		assertTrue(criteriaCaptor.getValue().getAllowWithNoStartActivity());
		assertNull(criteriaCaptor.getValue().getAllowWithNoAsset());
	}

	@Test
	void testFindSubscriptionsTriggeredBySenders() {
		SenderCriterion senders = new SenderCriterion(1L, 2L, 3L);
		ZonedDateTime validAt = ZonedDateTime.now();
		List<SubscriptionEntity> mockResult = Collections.emptyList();
		when(dao.listSubscriptions(any(SubscriptionSearchCriteria.class))).thenReturn(mockResult);
		List<SubscriptionEntity> result = sut.findTriggeredSubscriptions(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), senders, validAt, Collections.singleton(TriggerType.SCHEDULER));
		assertTrue(result.isEmpty());
		ArgumentCaptor<SubscriptionSearchCriteria> criteriaCaptor = ArgumentCaptor.forClass(SubscriptionSearchCriteria.class);
		verify(dao).listSubscriptions(criteriaCaptor.capture());
		assertTrue(criteriaCaptor.getValue().getActive());
		assertEquals(senders, criteriaCaptor.getValue().getSender());
		assertEquals(validAt, criteriaCaptor.getValue().getValidAt());
		assertEquals(1, criteriaCaptor.getValue().getWithAnyTriggerType().size());
		assertEquals(TriggerType.SCHEDULER, criteriaCaptor.getValue().getWithAnyTriggerType().iterator().next());
		assertTrue(criteriaCaptor.getValue().getAllowWithNoArea());
	}

	@Test
	void testFindSubscriptionByIdDelegatesToDao() {
		SubscriptionEntity subscription = new SubscriptionEntity();
		when(dao.findById(ID)).thenReturn(subscription);
		SubscriptionEntity result = sut.findSubscriptionById(ID);
		verify(dao).findById(ID);
		assertSame(subscription, result);
	}

	@Test
	void testFindSubscriptionsTriggeredByStartActivities() {
		List<ActivityCriterion> startActivityCriteria = Arrays.asList(new ActivityCriterion(SubscriptionFaReportDocumentType.DECLARATION, "ARRIVAL"), new ActivityCriterion(SubscriptionFaReportDocumentType.DECLARATION, "AREA_ENTRY"));
		ZonedDateTime validAt = ZonedDateTime.now();
		List<SubscriptionEntity> mockResult = Collections.emptyList();
		when(dao.listSubscriptions(any(SubscriptionSearchCriteria.class))).thenReturn(mockResult);
		List<SubscriptionEntity> result = sut.findTriggeredSubscriptions(Collections.emptyList(), Collections.emptyList(), startActivityCriteria, null, validAt, Collections.singleton(TriggerType.SCHEDULER));
		assertTrue(result.isEmpty());
		ArgumentCaptor<SubscriptionSearchCriteria> criteriaCaptor = ArgumentCaptor.forClass(SubscriptionSearchCriteria.class);
		verify(dao).listSubscriptions(criteriaCaptor.capture());
		assertTrue(criteriaCaptor.getValue().getActive());
		assertEquals(startActivityCriteria, criteriaCaptor.getValue().getWithAnyStartActivity());
		assertEquals(validAt, criteriaCaptor.getValue().getValidAt());
		assertEquals(2, criteriaCaptor.getValue().getWithAnyStartActivity().size());
		assertEquals("ARRIVAL", criteriaCaptor.getValue().getWithAnyStartActivity().iterator().next().getValue());
		assertTrue(criteriaCaptor.getValue().getAllowWithNoArea());
		assertTrue(criteriaCaptor.getValue().getAllowWithNoAsset());
		assertNull(criteriaCaptor.getValue().getAllowWithNoStartActivity());
	}
}
