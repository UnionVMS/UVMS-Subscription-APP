/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.rules.communication;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import eu.europa.ec.fisheries.schema.rules.module.v1.CreateTicketRequest;
import eu.europa.ec.fisheries.schema.rules.ticket.v1.TicketStatusType;
import eu.europa.ec.fisheries.schema.rules.ticket.v1.TicketType;
import eu.europa.ec.fisheries.wsdl.asset.types.VesselIdentifiersHolder;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for the {@link RulesSenderImpl}.
 */
@EnableAutoWeld
@ExtendWith(MockitoExtension.class)
public class RulesSenderImplTest {

	private static final String CONNECT_ID = "connectid";
	private static final String ASSET_GUID = "asset guid";
	private static final String SUBSCRIPTION_NAME = "subscription name";
	private static final Date OPEN_DATE = new Date();
	private static final String MOVEMENT_GUID = "mov guid";

	@Produces @Mock
	private RulesClient rulesClient;

	@Inject
	private RulesSenderImpl sut;

	@Test
	void testEmptyConstructor() {
		assertDoesNotThrow(() -> new RulesSenderImpl());
	}

	@Test
	void testPositionTrigger() {
		VesselIdentifiersHolder vesselIdentifiers = new VesselIdentifiersHolder();
		vesselIdentifiers.setAssetGuid(ASSET_GUID);
		List<String> movementGuids = Collections.singletonList(MOVEMENT_GUID);
		sut.createAlertsAsync(SUBSCRIPTION_NAME, OPEN_DATE, vesselIdentifiers, movementGuids);
		ArgumentCaptor<CreateTicketRequest> captor = ArgumentCaptor.forClass(CreateTicketRequest.class);
		verify(rulesClient).sendAsyncRequest(captor.capture());
		CreateTicketRequest request = captor.getValue();
		assertEquals(1, request.getTickets().size());
		TicketType ticket = request.getTickets().get(0);
		assertEquals(MOVEMENT_GUID, ticket.getMovementGuid());
		assertEquals(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z").format(OPEN_DATE), ticket.getOpenDate());
		assertEquals(ASSET_GUID, ticket.getAssetGuid());
		assertEquals(SUBSCRIPTION_NAME,ticket.getRuleName());
		assertNotNull(ticket.getGuid());
		assertEquals(TicketStatusType.OPEN, ticket.getStatus());
		assertEquals("UVMS", ticket.getUpdatedBy());
	}
}
