/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.service.trigger;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.util.Arrays;

import eu.europa.ec.fisheries.uvms.subscription.service.bean.Command;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for the {@link IncomingDataMessageServiceImpl}.
 */
@EnableAutoWeld
@ExtendWith(MockitoExtension.class)
public class IncomingDataMessageServiceImplTest {

	private static final String SUBSCRIPTION_SOURCE = "SUBSCRIPTION_SOURCE";
	private static final String REPRESENTATION = "REPRESENTATION";

	@Inject
	private IncomingDataMessageServiceImpl sut;

	@Produces
	private final SubscriptionCommandFromMessageExtractor subscriptionCommandFromMessageExtractor;
	{
		subscriptionCommandFromMessageExtractor = mock(SubscriptionCommandFromMessageExtractor.class);
		when(subscriptionCommandFromMessageExtractor.getEligibleSubscriptionSource()).thenReturn(SUBSCRIPTION_SOURCE);
	}

	@Test
	void testEmptyConstructor() {
		IncomingDataMessageServiceImpl sut = new IncomingDataMessageServiceImpl();
		assertNotNull(sut);
	}

	@Test
	void testHandle() {
		Command cmd1 = mock(Command.class);
		Command cmd2 = mock(Command.class);
		when(subscriptionCommandFromMessageExtractor.extractCommands(REPRESENTATION)).thenReturn(Arrays.stream(new Command[]{cmd1, cmd2}));

		sut.handle(SUBSCRIPTION_SOURCE, REPRESENTATION);

		verify(cmd1).execute();
		verify(cmd2).execute();
	}

	@Test
	void testHandleThrowsForUnknownSource() {
		assertThrows(IllegalStateException.class, () -> sut.handle("unknown source", REPRESENTATION));
	}
}
