/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.service.mapper;

import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.ec.fisheries.wsdl.user.types.Channel;
import eu.europa.ec.fisheries.wsdl.user.types.EndPoint;
import eu.europa.ec.fisheries.wsdl.user.types.Organisation;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * Tests for he {@link CustomMapper}.
 */
public class CustomMapperTest {

	private static final Long ORGANISATION_ID = 55L;
	private static final String PARENT_ORGANISATION_NAME = "PORG";
	private static final String ORGANISATION_NAME = "ORG NAME";
	private static final Long ENDPOINT_ID = 77L;
	private static final String ENDPOINT_NAME = "ENDPOINT NAME";
	private static final Long CHANNEL_ID = 99L;
	private static final String CHANNEL_DATAFLOW = "DATAFLOW";
	private static final String UNKNOWN = "UNKNOWN";

	@Test
	public void testEnrichSubscriptionListForNullOrEmptyOrganisationList() {
		List<SubscriptionEntity> resultList = Collections.emptyList();
		List<SubscriptionEntity> result = CustomMapper.enrichSubscriptionList(resultList, null);
		assertSame(result, resultList);
		result = CustomMapper.enrichSubscriptionList(resultList, Collections.emptyList());
		assertSame(result, resultList);
	}

	@Test
	public void testEnrichSubscriptionListForEmptySubscriptionList() {
		List<SubscriptionEntity> result = CustomMapper.enrichSubscriptionList(Collections.emptyList(), Collections.singletonList(new Organisation()));
		assertTrue(result.isEmpty());
	}

	@Test
	public void testEnrichSubscriptionListFillsInOrganizationDetails() {
		Organisation org = makeOrganisation(ORGANISATION_ID, PARENT_ORGANISATION_NAME, ENDPOINT_ID, CHANNEL_ID);
		SubscriptionEntity subscription = makeSubscription();

		List<SubscriptionEntity> result = CustomMapper.enrichSubscriptionList(Collections.singletonList(subscription), Collections.singletonList(org));

		assertEquals(1, result.size());
		subscription = result.get(0);
		assertEquals(PARENT_ORGANISATION_NAME + " / " + ORGANISATION_NAME, subscription.getOrganisationName());
		assertEquals(ENDPOINT_NAME, subscription.getEndpointName());
		assertEquals(CHANNEL_DATAFLOW, subscription.getChannelName());
	}

	@Test
	public void testEnrichSubscriptionListFillsInOrganizationDetailsForNoParent() {
		Organisation org = makeOrganisation(ORGANISATION_ID, null, ENDPOINT_ID, CHANNEL_ID);
		SubscriptionEntity subscription = makeSubscription();

		List<SubscriptionEntity> result = CustomMapper.enrichSubscriptionList(Collections.singletonList(subscription), Collections.singletonList(org));

		assertEquals(1, result.size());
		subscription = result.get(0);
		assertEquals(ORGANISATION_NAME, subscription.getOrganisationName());
		assertEquals(ENDPOINT_NAME, subscription.getEndpointName());
		assertEquals(CHANNEL_DATAFLOW, subscription.getChannelName());
	}

	@Test
	public void testEnrichSubscriptionListUnknownChannel() {
		Organisation org = makeOrganisation(ORGANISATION_ID, PARENT_ORGANISATION_NAME, ENDPOINT_ID, null);
		SubscriptionEntity subscription = makeSubscription();

		List<SubscriptionEntity> result = CustomMapper.enrichSubscriptionList(Collections.singletonList(subscription), Collections.singletonList(org));

		assertEquals(1, result.size());
		subscription = result.get(0);
		assertEquals(PARENT_ORGANISATION_NAME + " / " + ORGANISATION_NAME, subscription.getOrganisationName());
		assertEquals(ENDPOINT_NAME, subscription.getEndpointName());
		assertEquals(UNKNOWN, subscription.getChannelName());
	}

	@Test
	public void testEnrichSubscriptionListUnknownEndpoint() {
		Organisation org = makeOrganisation(ORGANISATION_ID, PARENT_ORGANISATION_NAME, null, null);
		SubscriptionEntity subscription = makeSubscription();

		List<SubscriptionEntity> result = CustomMapper.enrichSubscriptionList(Collections.singletonList(subscription), Collections.singletonList(org));

		assertEquals(1, result.size());
		subscription = result.get(0);
		assertEquals(PARENT_ORGANISATION_NAME + " / " + ORGANISATION_NAME, subscription.getOrganisationName());
		assertEquals(UNKNOWN, subscription.getEndpointName());
		assertEquals(UNKNOWN, subscription.getChannelName());
	}

	@Test
	public void testEnrichSubscriptionListUnknownOrganisation() {
		Organisation org = makeOrganisation(1L, PARENT_ORGANISATION_NAME, null, null);
		SubscriptionEntity subscription = makeSubscription();

		List<SubscriptionEntity> result = CustomMapper.enrichSubscriptionList(Collections.singletonList(subscription), Collections.singletonList(org));

		assertEquals(1, result.size());
		subscription = result.get(0);
		assertEquals(UNKNOWN, subscription.getOrganisationName());
		assertEquals(UNKNOWN, subscription.getEndpointName());
		assertEquals(UNKNOWN, subscription.getChannelName());
	}

	private Organisation makeOrganisation(long id, String parentName, Long endpointId, Long channelId) {
		Channel channel = null;
		EndPoint endpoint = null;

		if (channelId != null) {
			channel = new Channel();
			channel.setId(channelId);
			channel.setDataFlow(CHANNEL_DATAFLOW);
		}

		if (endpointId != null) {
			endpoint = new EndPoint();
			endpoint.setId(endpointId);
			endpoint.setName(ENDPOINT_NAME);
			if (channel != null) {
				endpoint.getChannels().add(channel);
			}
		}

		Organisation org = new Organisation();
		org.setId(id);
		org.setParentOrganisation(parentName);
		org.setName(ORGANISATION_NAME);
		if (endpoint != null) {
			org.getEndPoints().add(endpoint);
		}

		return org;
	}

	private SubscriptionEntity makeSubscription() {
		SubscriptionEntity subscription = new SubscriptionEntity();
		subscription.setOrganisation(ORGANISATION_ID);
		subscription.setEndPoint(ENDPOINT_ID);
		subscription.setChannel(CHANNEL_ID);
		return subscription;
	}
}
