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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.ninja_squad.dbsetup.DbSetup;
import com.ninja_squad.dbsetup.destination.DataSourceDestination;
import com.ninja_squad.dbsetup.operation.Operation;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionExecutionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionEntity;
import eu.europa.fisheries.uvms.subscription.model.enums.SubscriptionExecutionStatusType;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link SubscriptionExecutionDaoImpl}.
 */
@EnableAutoWeld
public class SubscriptionExecutionDaoImplTest extends BaseSubscriptionInMemoryTest {

	@Inject
	private SubscriptionExecutionDaoImpl sut;

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
		SubscriptionExecutionDaoImpl sut = new SubscriptionExecutionDaoImpl();
		assertNotNull(sut);
	}

	@Test
	void testCreate() {
		em.getTransaction().begin();
		SubscriptionEntity subscription = em.find(SubscriptionEntity.class, 1L);
		TriggeredSubscriptionEntity triggeredSubscription = new TriggeredSubscriptionEntity();
		triggeredSubscription.setActive(true);
		triggeredSubscription.setCreationDate(new Date());
		triggeredSubscription.setSource("test");
		triggeredSubscription.setSubscription(subscription);
		em.persist(triggeredSubscription);
		SubscriptionExecutionEntity execution = new SubscriptionExecutionEntity();
		execution.setCreationDate(new Date());
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.add(Calendar.DATE, 1);
		execution.setRequestedTime(calendar.getTime());
		execution.setStatus(SubscriptionExecutionStatusType.PENDING);
		execution.setTriggeredSubscription(triggeredSubscription);

		sut.create(execution);

		em.getTransaction().commit();
		em.clear();
		TypedQuery<SubscriptionExecutionEntity> q = em.createQuery("SELECT e FROM SubscriptionExecutionEntity e", SubscriptionExecutionEntity.class);
		List<SubscriptionExecutionEntity> results = q.getResultList();
		assertEquals(1, results.size());
		assertEquals(1L, results.get(0).getTriggeredSubscription().getSubscription().getId());
	}
}
