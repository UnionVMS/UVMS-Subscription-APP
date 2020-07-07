/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.service.dao;

import static com.ninja_squad.dbsetup.Operations.sequenceOf;
import static eu.europa.fisheries.uvms.subscription.model.enums.SubscriptionExecutionStatusType.PENDING;
import static eu.europa.fisheries.uvms.subscription.model.enums.TriggeredSubscriptionStatus.ACTIVE;
import static eu.europa.fisheries.uvms.subscription.model.enums.TriggeredSubscriptionStatus.INACTIVE;
import static eu.europa.fisheries.uvms.subscription.model.enums.TriggeredSubscriptionStatus.STOPPED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import com.ninja_squad.dbsetup.DbSetup;
import com.ninja_squad.dbsetup.destination.DataSourceDestination;
import com.ninja_squad.dbsetup.operation.Operation;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.AreaEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionExecutionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionDataEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.AreaCriterion;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.TriggeredSubscriptionSearchCriteria;
import eu.europa.ec.fisheries.wsdl.subscription.module.AreaType;
import eu.europa.fisheries.uvms.subscription.model.enums.SubscriptionExecutionStatusType;
import eu.europa.fisheries.uvms.subscription.model.exceptions.EntityDoesNotExistException;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link TriggeredSubscriptionDao}.
 */
@EnableAutoWeld
public class TriggeredSubscriptionDaoImplTest extends BaseSubscriptionInMemoryTest {

	private static final String CONNECT_ID1 = UUID.randomUUID().toString();
	private static final String CONNECT_ID2 = UUID.randomUUID().toString();
	private static final String CONNECT_ID3 = UUID.randomUUID().toString();

	@Inject
	private SubscriptionDaoImpl subscriptionDao;

	@Inject
	private TriggeredSubscriptionDaoImpl sut;

	@Produces
	EntityManager getEntityManager() {
		return em;
	}

	@BeforeEach
	public void prepare(){
		Operation operation = sequenceOf(
				DELETE_ALL, INSERT_SUBSCRIPTION
		);

		DbSetup dbSetup = new DbSetup(new DataSourceDestination(ds), operation);
		dbSetupTracker.launchIfNecessary(dbSetup);
	}

	@Test
	void testEmptyConstructor() {
		TriggeredSubscriptionDaoImpl sut = new TriggeredSubscriptionDaoImpl();
		assertNotNull(sut);
	}

	@Test
	void testCreateTriggeredSubscription() {
		em.getTransaction().begin();
		SubscriptionEntity subscription = subscriptionDao.findById(3L);
		TriggeredSubscriptionEntity entity = makeTriggeredSubscription(subscription);
		addTriggeredSubscriptionDataEntity(entity, "key1", "value1");
		addTriggeredSubscriptionDataEntity(entity, "key2", "value2");
		entity = sut.create(entity);
		em.getTransaction().commit();
		em.clear();
		assertNotNull(entity.getId());
		TriggeredSubscriptionEntity entityFromDb = sut.getById(entity.getId());
		assertNotNull(entityFromDb);
		assertEquals(new HashSet<>(Arrays.asList("key1=value1","key2=value2")), entityFromDb.getData().stream().map(d -> d.getKey() + '=' + d.getValue()).collect(Collectors.toSet()));
	}

	@Test
	void testGetByIdForNonExistingId() {
		assertThrows(EntityDoesNotExistException.class, () -> sut.getById(99999999L));
	}

	@Test
	void testActiveExists() {
		em.getTransaction().begin();
		SubscriptionEntity subscription2 = subscriptionDao.findById(2L);
		TriggeredSubscriptionEntity trig2 = makeTriggeredSubscription(subscription2);
		addTriggeredSubscriptionDataEntity(trig2, "key1", "value1");
		SubscriptionEntity subscription3 = subscriptionDao.findById(3L);
		TriggeredSubscriptionEntity trig3 = makeTriggeredSubscription(subscription3);
		addTriggeredSubscriptionDataEntity(trig3, "key2", "value2");
		trig2 = sut.create(trig2);
		trig3 = sut.create(trig3);
		em.persist(makeExecution(trig2, PENDING));
		em.persist(makeExecution(trig3, PENDING));
		em.getTransaction().commit();
		em.clear();

		assertTrue(sut.activeExists(subscription2, Collections.singleton(new TriggeredSubscriptionDataEntity(null, "key1", "value1"))));
		assertFalse(sut.activeExists(subscription3, Collections.singleton(new TriggeredSubscriptionDataEntity(null, "key1", "value1"))));
	}

	private TriggeredSubscriptionEntity makeTriggeredSubscription(SubscriptionEntity subscription) {
		TriggeredSubscriptionEntity entity = new TriggeredSubscriptionEntity();
		entity.setSubscription(subscription);
		entity.setSource("SOURCE");
		entity.setCreationDate(new Date());
		entity.setStatus(ACTIVE);
		return entity;
	}

	private void addTriggeredSubscriptionDataEntity(TriggeredSubscriptionEntity entity, String key, String value) {
		TriggeredSubscriptionDataEntity data = new TriggeredSubscriptionDataEntity();
		data.setTriggeredSubscription(entity);
		data.setKey(key);
		data.setValue(value);
		entity.getData().add(data);
	}

	private SubscriptionExecutionEntity makeExecution(TriggeredSubscriptionEntity entity, SubscriptionExecutionStatusType status) {
		SubscriptionExecutionEntity execution = new SubscriptionExecutionEntity();
		execution.setTriggeredSubscription(entity);
		execution.setStatus(status);
		execution.setRequestedTime(new Date());
		return execution;
	}

	@Test
	void testFind() {
		em.getTransaction().begin();
		SubscriptionEntity s1 = makeSubscription("s1", 111L, 222L);
		s1.setStopWhenQuitArea(true);
		SubscriptionEntity s2 = makeSubscription("s2", 333L);
		s2.setStopWhenQuitArea(true);
		SubscriptionEntity s3 = makeSubscription("s3");
		s3.setStopWhenQuitArea(true);
		SubscriptionEntity s4 = makeSubscription("s4", 555L);
		em.persist(s1);
		em.persist(s2);
		em.persist(s3);
		em.persist(s4);
		TriggeredSubscriptionEntity s1t1 = makeTriggeredSubscription(s1);
		addTriggeredSubscriptionDataEntity(s1t1, "connectId", CONNECT_ID1);
		TriggeredSubscriptionEntity s1t2 = makeTriggeredSubscription(s1);
		addTriggeredSubscriptionDataEntity(s1t2, "connectId", CONNECT_ID2);
		TriggeredSubscriptionEntity s2t1 = makeTriggeredSubscription(s2);
		addTriggeredSubscriptionDataEntity(s2t1, "connectId", CONNECT_ID1);
		TriggeredSubscriptionEntity s2t2 = makeTriggeredSubscription(s2);
		addTriggeredSubscriptionDataEntity(s2t2, "connectId", CONNECT_ID2);
		s2t2.setStatus(INACTIVE);
		TriggeredSubscriptionEntity s2t3 = makeTriggeredSubscription(s2);
		addTriggeredSubscriptionDataEntity(s2t3, "connectId", CONNECT_ID3);
		s2t3.setStatus(STOPPED);
		TriggeredSubscriptionEntity s3t1 = makeTriggeredSubscription(s3);
		addTriggeredSubscriptionDataEntity(s3t1, "connectId", CONNECT_ID1);
		TriggeredSubscriptionEntity s3t2 = makeTriggeredSubscription(s3);
		addTriggeredSubscriptionDataEntity(s3t2, "connectId", CONNECT_ID2);
		TriggeredSubscriptionEntity s4t1 = makeTriggeredSubscription(s4);
		addTriggeredSubscriptionDataEntity(s4t1, "connectId", CONNECT_ID1);
		TriggeredSubscriptionEntity s4t2 = makeTriggeredSubscription(s4);
		addTriggeredSubscriptionDataEntity(s4t2, "connectId", CONNECT_ID2);
		s1t1 = sut.create(s1t1);
		s1t2 = sut.create(s1t2);
		s2t1 = sut.create(s2t1);
		s2t2 = sut.create(s2t2);
		s2t3 = sut.create(s2t3);
		sut.create(s3t1);
		s3t2 = sut.create(s3t2);
		s4t1 = sut.create(s4t1);
		s4t2 = sut.create(s4t2);
		em.getTransaction().commit();

		TriggeredSubscriptionSearchCriteria criteria1 = new TriggeredSubscriptionSearchCriteria();
		criteria1.setSingleStatus(ACTIVE);
		criteria1.setSubscriptionQuitArea(true);
		criteria1.setTriggeredSubscriptionData(Collections.singletonMap("connectId", CONNECT_ID1));
		criteria1.setNotInAreas(new HashSet<>(Arrays.asList(new AreaCriterion(AreaType.USERAREA, 333L), new AreaCriterion(AreaType.USERAREA, 444L))));
		List<TriggeredSubscriptionEntity> result1 = sut.find(criteria1).collect(Collectors.toList());

		assertEquals(1, result1.size());
		assertEquals(s1t1.getId(), result1.get(0).getId());

		TriggeredSubscriptionSearchCriteria criteria2 = new TriggeredSubscriptionSearchCriteria();
		criteria2.setWithStatus(EnumSet.of(INACTIVE, STOPPED));
		List<TriggeredSubscriptionEntity> result2 = sut.find(criteria2).collect(Collectors.toList());

		assertEquals(new HashSet<>(Arrays.asList(s2t2.getId(), s2t3.getId())), result2.stream().map(TriggeredSubscriptionEntity::getId).collect(Collectors.toSet()));

		TriggeredSubscriptionSearchCriteria criteria3 = new TriggeredSubscriptionSearchCriteria();
		criteria3.setNotInAreas(new HashSet<>(Arrays.asList(new AreaCriterion(AreaType.USERAREA, 111L), new AreaCriterion(AreaType.USERAREA, 222L))));
		List<TriggeredSubscriptionEntity> result3 = sut.find(criteria3).collect(Collectors.toList());

		assertEquals(new HashSet<>(Arrays.asList(s2t1.getId(),s2t2.getId(),s2t3.getId(),s4t1.getId(),s4t2.getId())), result3.stream().map(TriggeredSubscriptionEntity::getId).collect(Collectors.toSet()));

		TriggeredSubscriptionSearchCriteria criteria4 = new TriggeredSubscriptionSearchCriteria();
		criteria4.setTriggeredSubscriptionData(Collections.singletonMap("connectId", CONNECT_ID2));
		List<TriggeredSubscriptionEntity> result4 = sut.find(criteria4).collect(Collectors.toList());

		assertEquals(new HashSet<>(Arrays.asList(s1t2.getId(),s2t2.getId(),s3t2.getId(),s4t2.getId())), result4.stream().map(TriggeredSubscriptionEntity::getId).collect(Collectors.toSet()));
	}

	private SubscriptionEntity makeSubscription(String name, long... areaGids) {
		SubscriptionEntity s = new SubscriptionEntity();
		s.setActive(true);
		s.setName(name);
		s.setAreas(LongStream.of(areaGids)
				.mapToObj(gid -> {
					AreaEntity a = new AreaEntity();
					a.setAreaType(AreaType.USERAREA);
					a.setGid(gid);
					a.setSubscription(s);
					return a;
				})
				.collect(Collectors.toSet())
		);
		s.setHasAreas(!s.getAreas().isEmpty());
		return s;
	}
}
