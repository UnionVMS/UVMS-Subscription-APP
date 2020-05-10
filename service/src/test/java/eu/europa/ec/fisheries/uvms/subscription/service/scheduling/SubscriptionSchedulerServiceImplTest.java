/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.service.scheduling;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import java.util.Date;
import java.util.stream.Stream;

import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionExecutionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.execution.SubscriptionExecutionService;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for the {@link SubscriptionSchedulerServiceImpl}.
 */
@EnableAutoWeld
@ExtendWith(MockitoExtension.class)
public class SubscriptionSchedulerServiceImplTest {

	@Produces @Mock
	private SubscriptionExecutionService subscriptionExecutionService;

	@Inject
	private SubscriptionSchedulerServiceImpl sut;

	@Test
	void testActivatePendingSubscriptionExecutions() {
		Date date = new Date();
		SubscriptionExecutionEntity execution = new SubscriptionExecutionEntity();
		when(subscriptionExecutionService.findPendingSubscriptionExecutions(date)).thenReturn(Stream.of(execution));
		sut.activatePendingSubscriptionExecutions(date);
		verify(subscriptionExecutionService).enqueueForExecution(execution);
		verifyNoMoreInteractions(subscriptionExecutionService);
	}
}
