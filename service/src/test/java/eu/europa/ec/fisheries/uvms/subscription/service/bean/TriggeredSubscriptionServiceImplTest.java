/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2020.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.service.bean;

import static eu.europa.fisheries.uvms.subscription.model.enums.TriggeredSubscriptionStatus.ACTIVE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import eu.europa.ec.fisheries.uvms.subscription.service.dao.TriggeredSubscriptionDao;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionDataEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.ActivityCriterion;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.AreaCriterion;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.TriggeredSubscriptionSearchCriteria;
import eu.europa.ec.fisheries.uvms.subscription.service.trigger.StopConditionCriteria;
import eu.europa.ec.fisheries.wsdl.subscription.module.AreaType;
import eu.europa.fisheries.uvms.subscription.model.enums.SubscriptionFaReportDocumentType;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for the {@link TriggeredSubscriptionServiceImpl}.
 */
@EnableAutoWeld
@ExtendWith(MockitoExtension.class)
public class TriggeredSubscriptionServiceImplTest {

	private static final String CONNECT_ID1 = UUID.randomUUID().toString();
	private static final Long AREA_GID1 = 111L;

	@Produces @Mock
	private TriggeredSubscriptionDao triggeredSubscriptionDao;

	@Inject
	private TriggeredSubscriptionServiceImpl sut;

	@Test
	void testEmptyConstructor() {
		TriggeredSubscriptionService sut = new TriggeredSubscriptionServiceImpl();
		assertNotNull(sut);
	}

	@Test
	void testSave() {
		TriggeredSubscriptionEntity entity = new TriggeredSubscriptionEntity();
		sut.save(entity);
		verify(triggeredSubscriptionDao).create(eq(entity));
	}

	@Test
	void testIsDuplicate() {
		SubscriptionEntity subscription = new SubscriptionEntity();
		TriggeredSubscriptionEntity entity = new TriggeredSubscriptionEntity();
		entity.setSubscription(subscription);
		Set<TriggeredSubscriptionDataEntity > dataForDuplicates = new HashSet<>();
		assertFalse(sut.isDuplicate(entity, dataForDuplicates));
		verify(triggeredSubscriptionDao).activeExists(subscription, dataForDuplicates);
	}

	@Test
	void testFindAlreadyActivated() {
		SubscriptionEntity subscription = new SubscriptionEntity();
		TriggeredSubscriptionEntity entity = new TriggeredSubscriptionEntity();
		entity.setSubscription(subscription);
		Set<TriggeredSubscriptionDataEntity > dataForDuplicates = new HashSet<>();
		Stream<TriggeredSubscriptionEntity> output = Stream.empty();
		when(triggeredSubscriptionDao.findAlreadyActivated(any(), any())).thenReturn(output);
		Stream<TriggeredSubscriptionEntity> result = sut.findAlreadyActivated(entity, dataForDuplicates);
		assertSame(output, result);
		verify(triggeredSubscriptionDao).findAlreadyActivated(subscription, dataForDuplicates);
	}

	@Test
	void testFindByStopConditionCriteria() {
		StopConditionCriteria criteria = new StopConditionCriteria();
		criteria.setConnectId(CONNECT_ID1);
		criteria.setActivities(Collections.singleton(new ActivityCriterion(SubscriptionFaReportDocumentType.DECLARATION, "AREA_ENTRY")));
		criteria.setAreas(Collections.singleton(new AreaCriterion(AreaType.USERAREA, AREA_GID1)));
		TriggeredSubscriptionEntity result = new TriggeredSubscriptionEntity();
		when(triggeredSubscriptionDao.find(any())).thenReturn(Stream.of(result));
		List<TriggeredSubscriptionEntity> results = sut.findByStopConditionCriteria(criteria).collect(Collectors.toList());
		assertEquals(1, results.size());
		assertSame(result, results.get(0));
		ArgumentCaptor<TriggeredSubscriptionSearchCriteria> areaCriteriaCaptor = ArgumentCaptor.forClass(TriggeredSubscriptionSearchCriteria.class);
		verify(triggeredSubscriptionDao).find(areaCriteriaCaptor.capture());
		TriggeredSubscriptionSearchCriteria areaCriteria = areaCriteriaCaptor.getValue();
		assertEquals(CONNECT_ID1, areaCriteria.getTriggeredSubscriptionData().get("connectId"));
		assertEquals(criteria.getAreas(), areaCriteria.getNotInAreas());
		assertEquals(EnumSet.of(ACTIVE), areaCriteria.getWithStatus());
		assertTrue(areaCriteria.getSubscriptionQuitArea());
	}
}
