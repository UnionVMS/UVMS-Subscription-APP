/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.movement.trigger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.xml.datatype.DatatypeFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import eu.europa.ec.fisheries.uvms.subscription.service.bean.SubscriptionFinder;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionDataEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.SubscriptionSearchCriteria.AreaCriterion;
import eu.europa.fisheries.uvms.subscription.model.enums.TriggerType;
import eu.europa.fisheries.uvms.subscription.model.exceptions.MessageFormatException;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for the {@link MovementTriggeredSubscriptionCreator}.
 */
@EnableAutoWeld
@ExtendWith(MockitoExtension.class)
public class MovementTriggeredSubscriptionCreatorTest {

	@Produces @Mock
	private SubscriptionFinder subscriptionFinder;

	@Inject
	private MovementTriggeredSubscriptionCreator sut;

	@Produces
	DatatypeFactory getDatatypeFactory() throws Exception {
		return DatatypeFactory.newInstance();
	}

	@Test
	void testJAXBExceptionResultsInApplicationException() {
		assertThrows(MessageFormatException.class, () -> sut.createTriggeredSubscriptions("bad"));
	}

	@Test
	void testNOKReturnsEmptyStream() {
		String representation = readResource("CreateMovementBatchResponse-NOK.xml");
		long size = sut.createTriggeredSubscriptions(representation).count();
		assertEquals(0, size);
		verifyNoInteractions(subscriptionFinder);
	}

	@Test
	void testDoNotTriggerOnDuplicateMovements() {
		String representation = readResource("CreateMovementBatchResponse-OK-duplicate.xml");
		long size = sut.createTriggeredSubscriptions(representation).count();
		assertEquals(0, size);
		verifyNoInteractions(subscriptionFinder);
	}

	@Test
	void testOK() {
		SubscriptionEntity subscription = new SubscriptionEntity();
		when(subscriptionFinder.findSubscriptionsTriggeredByAreas(any(), any(), any())).thenReturn(Collections.singletonList(subscription));
		String representation = readResource("CreateMovementBatchResponse-OK.xml");
		List<TriggeredSubscriptionEntity> result = sut.createTriggeredSubscriptions(representation).collect(Collectors.toList());
		assertEquals(1, result.size());
		assertSame(subscription, result.get(0).getSubscription());
		assertNotNull(result.get(0).getCreationDate());
		assertTrue(result.get(0).getActive());
		@SuppressWarnings("unchecked")
		ArgumentCaptor<Collection<AreaCriterion>> captor = ArgumentCaptor.forClass(Collection.class);
		@SuppressWarnings("unchecked")
		ArgumentCaptor<Collection<TriggerType>> triggerTypeCaptor = ArgumentCaptor.forClass(Collection.class);
		verify(subscriptionFinder).findSubscriptionsTriggeredByAreas(captor.capture(), any(), triggerTypeCaptor.capture());
		String areaCriteria = captor.getValue().stream().map(ac -> ac.getType().toString() + '-' + ac.getGid()).sorted().collect(Collectors.joining(","));
		assertEquals("FMZ-270,STATRECT-73", areaCriteria);
		assertEquals(1, triggerTypeCaptor.getValue().size());
		assertEquals(TriggerType.INC_POSITION, triggerTypeCaptor.getValue().iterator().next());
	}

	private String readResource(String resourceName) {
		try(InputStream is = this.getClass().getResourceAsStream(resourceName)) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] buf = new byte[512];
			int r;
			while ((r = is.read(buf, 0, buf.length)) >= 0) {
				baos.write(buf, 0, r);
			}
			return new String(baos.toByteArray(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	void testExtractTriggeredSubscriptionDataForDuplicates() {
		TriggeredSubscriptionEntity e = new TriggeredSubscriptionEntity();
		e.getData().add(new TriggeredSubscriptionDataEntity(e, "connectId", "CONNECT_ID"));
		e.getData().add(new TriggeredSubscriptionDataEntity(e, "occurrence", "OCCURRENCE"));
		e.getData().add(new TriggeredSubscriptionDataEntity(e, "somethingElse", "XXX"));
		Set<TriggeredSubscriptionDataEntity> result = sut.extractTriggeredSubscriptionDataForDuplicates(e);
		assertEquals(new HashSet<>(Arrays.asList(
				new TriggeredSubscriptionDataEntity(e, "connectId", "CONNECT_ID")
		)), result);
	}
}
