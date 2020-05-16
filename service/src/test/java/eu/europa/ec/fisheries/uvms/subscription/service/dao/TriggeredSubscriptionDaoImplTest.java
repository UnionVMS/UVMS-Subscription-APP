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
import java.util.HashSet;
import java.util.stream.Collectors;

import com.ninja_squad.dbsetup.DbSetup;
import com.ninja_squad.dbsetup.destination.DataSourceDestination;
import com.ninja_squad.dbsetup.operation.Operation;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionExecutionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionDataEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionEntity;
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
		entity.setActive(true);
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
}
