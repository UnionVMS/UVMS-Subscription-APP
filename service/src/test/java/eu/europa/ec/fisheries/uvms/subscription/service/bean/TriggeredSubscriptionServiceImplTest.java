/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.service.bean;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import java.util.HashSet;
import java.util.Set;

import eu.europa.ec.fisheries.uvms.subscription.service.dao.TriggeredSubscriptionDao;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionDataEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionEntity;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for the {@link TriggeredSubscriptionServiceImpl}.
 */
@EnableAutoWeld
@ExtendWith(MockitoExtension.class)
public class TriggeredSubscriptionServiceImplTest {

	@Produces @Mock
	private TriggeredSubscriptionDao triggeredSubscriptionDao;

	@Inject
	private TriggeredSubscriptionServiceImpl sut;

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
}
