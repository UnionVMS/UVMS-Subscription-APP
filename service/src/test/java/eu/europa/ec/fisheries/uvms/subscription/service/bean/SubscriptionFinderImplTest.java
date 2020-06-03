package eu.europa.ec.fisheries.uvms.subscription.service.bean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.SubscriptionSearchCriteria;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.SubscriptionSearchCriteria.AreaCriterion;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.SubscriptionSearchCriteria.AssetCriterion;

import eu.europa.ec.fisheries.uvms.subscription.service.dao.SubscriptionDao;
import eu.europa.ec.fisheries.wsdl.subscription.module.AreaType;
import eu.europa.fisheries.uvms.subscription.model.enums.AssetType;
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
		assertTrue(sut.findTriggeredSubscriptions(null, null, ZonedDateTime.now(), null).isEmpty());
		assertTrue(sut.findTriggeredSubscriptions(Collections.emptyList(), Collections.emptyList(), ZonedDateTime.now(), null).isEmpty());
		verifyNoMoreInteractions(dao);
	}

	@Test
	void testFindSubscriptionsTriggeredByAreasAndAssets() {
		List<AreaCriterion> areas = Arrays.asList(new AreaCriterion(AreaType.USERAREA, 111L), new AreaCriterion(AreaType.PORT, 222L));
		List<AssetCriterion> assets = Arrays.asList(new AssetCriterion(AssetType.ASSET, "guid1"), new AssetCriterion(AssetType.VGROUP, "guid2"));
		ZonedDateTime validAt = ZonedDateTime.now();
		List<SubscriptionEntity> mockResult = Collections.emptyList();
		when(dao.listSubscriptions(any(SubscriptionSearchCriteria.class))).thenReturn(mockResult);
		List<SubscriptionEntity> result = sut.findTriggeredSubscriptions(areas, assets, validAt, Collections.singleton(TriggerType.SCHEDULER));
		assertSame(mockResult, result);
		ArgumentCaptor<SubscriptionSearchCriteria> criteriaCaptor = ArgumentCaptor.forClass(SubscriptionSearchCriteria.class);
		verify(dao).listSubscriptions(criteriaCaptor.capture());
		assertTrue(criteriaCaptor.getValue().getActive());
		assertEquals(areas, criteriaCaptor.getValue().getInAnyArea());
		assertEquals(assets, criteriaCaptor.getValue().getWithAnyAsset());
		assertEquals(validAt, criteriaCaptor.getValue().getValidAt());
		assertEquals(1, criteriaCaptor.getValue().getWithAnyTriggerType().size());
		assertEquals(TriggerType.SCHEDULER, criteriaCaptor.getValue().getWithAnyTriggerType().iterator().next());
	}

	@Test
	void testFindSubscriptionsTriggeredByAreas() {
		List<AreaCriterion> areas = Arrays.asList(new AreaCriterion(AreaType.USERAREA, 111L), new AreaCriterion(AreaType.PORT, 222L));
		ZonedDateTime validAt = ZonedDateTime.now();
		List<SubscriptionEntity> mockResult = Collections.emptyList();
		when(dao.listSubscriptions(any(SubscriptionSearchCriteria.class))).thenReturn(mockResult);
		List<SubscriptionEntity> result = sut.findTriggeredSubscriptions(areas,  Collections.emptyList(), validAt, Collections.singleton(TriggerType.SCHEDULER));
		assertSame(mockResult, result);
		ArgumentCaptor<SubscriptionSearchCriteria> criteriaCaptor = ArgumentCaptor.forClass(SubscriptionSearchCriteria.class);
		verify(dao).listSubscriptions(criteriaCaptor.capture());
		assertTrue(criteriaCaptor.getValue().getActive());
		assertEquals(areas, criteriaCaptor.getValue().getInAnyArea());
		assertEquals(validAt, criteriaCaptor.getValue().getValidAt());
		assertEquals(1, criteriaCaptor.getValue().getWithAnyTriggerType().size());
		assertEquals(TriggerType.SCHEDULER, criteriaCaptor.getValue().getWithAnyTriggerType().iterator().next());
	}

	@Test
	void testFindSubscriptionsTriggeredByAssets() {
		List<AssetCriterion> assets = Arrays.asList(new AssetCriterion(AssetType.ASSET, "guid1"), new AssetCriterion(AssetType.VGROUP, "guid2"));
		ZonedDateTime validAt = ZonedDateTime.now();
		List<SubscriptionEntity> mockResult = Collections.emptyList();
		when(dao.listSubscriptions(any(SubscriptionSearchCriteria.class))).thenReturn(mockResult);
		List<SubscriptionEntity> result = sut.findTriggeredSubscriptions(Collections.emptyList(), assets, validAt, Collections.singleton(TriggerType.SCHEDULER));
		assertSame(mockResult, result);
		ArgumentCaptor<SubscriptionSearchCriteria> criteriaCaptor = ArgumentCaptor.forClass(SubscriptionSearchCriteria.class);
		verify(dao).listSubscriptions(criteriaCaptor.capture());
		assertTrue(criteriaCaptor.getValue().getActive());
		assertEquals(assets, criteriaCaptor.getValue().getWithAnyAsset());
		assertEquals(validAt, criteriaCaptor.getValue().getValidAt());
		assertEquals(1, criteriaCaptor.getValue().getWithAnyTriggerType().size());
		assertEquals(TriggerType.SCHEDULER, criteriaCaptor.getValue().getWithAnyTriggerType().iterator().next());
	}
}
