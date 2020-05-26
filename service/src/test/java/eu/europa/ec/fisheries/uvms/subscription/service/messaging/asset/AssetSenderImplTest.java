/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2020.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.service.messaging.asset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import eu.europa.ec.fisheries.wsdl.asset.module.AssetModuleMethod;
import eu.europa.ec.fisheries.wsdl.asset.module.FindAssetHistGuidByAssetGuidAndOccurrenceDateRequest;
import eu.europa.ec.fisheries.wsdl.asset.module.FindAssetHistGuidByAssetGuidAndOccurrenceDateResponse;
import eu.europa.ec.fisheries.wsdl.asset.module.FindVesselIdsByAssetHistGuidRequest;
import eu.europa.ec.fisheries.wsdl.asset.module.FindVesselIdsByAssetHistGuidResponse;
import eu.europa.ec.fisheries.wsdl.asset.module.FindVesselIdsByMultipleAssetHistGuidsRequest;
import eu.europa.ec.fisheries.wsdl.asset.module.FindVesselIdsByMultipleAssetHistGuidsResponse;
import eu.europa.ec.fisheries.wsdl.asset.types.AssetHistGuidIdWithVesselIdentifiers;
import eu.europa.ec.fisheries.wsdl.asset.types.VesselIdentifiersHolder;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for the {@link AssetSenderImpl}.
 */
@EnableAutoWeld
@ExtendWith(MockitoExtension.class)
public class AssetSenderImplTest {

	private static final String ASSET_GUID1 = "G1";
	private static final String ASSET_GUID2 = "G2";
	private static final String ASSET_HIST_GUID = "AHG";

	@Produces @Mock
	private AssetClient assetClient;

	@Inject
	private AssetSenderImpl sut;

	@Test
	void testFindVesselIdentifiers() {
		FindVesselIdsByAssetHistGuidResponse response = new FindVesselIdsByAssetHistGuidResponse();
		VesselIdentifiersHolder vesselIdentifiersHolder = new VesselIdentifiersHolder();
		response.setIdentifiers(vesselIdentifiersHolder);
		when(assetClient.findVesselIdsByAssetHistGuid(any())).thenReturn(response);
		VesselIdentifiersHolder result = sut.findVesselIdentifiers(ASSET_GUID1);
		assertSame(vesselIdentifiersHolder, result);
		ArgumentCaptor<FindVesselIdsByAssetHistGuidRequest> captor = ArgumentCaptor.forClass(FindVesselIdsByAssetHistGuidRequest.class);
		verify(assetClient).findVesselIdsByAssetHistGuid(captor.capture());
		FindVesselIdsByAssetHistGuidRequest request = captor.getValue();
		assertEquals(ASSET_GUID1, request.getAssetHistoryGuid());
		assertEquals(AssetModuleMethod.FIND_VESSEL_IDS_BY_ASSET_HIST_GUID, request.getMethod());
	}

	@Test
	void testFindMultipleVesselIdentifiers() {
		FindVesselIdsByMultipleAssetHistGuidsResponse response = new FindVesselIdsByMultipleAssetHistGuidsResponse();
		when(assetClient.findVesselIdsByMultipleAssetHistGuid(any())).thenReturn(response);
		List<AssetHistGuidIdWithVesselIdentifiers> result = sut.findMultipleVesselIdentifiers(Arrays.asList(ASSET_GUID1, ASSET_GUID2));
		ArgumentCaptor<FindVesselIdsByMultipleAssetHistGuidsRequest> captor = ArgumentCaptor.forClass(FindVesselIdsByMultipleAssetHistGuidsRequest.class);
		verify(assetClient).findVesselIdsByMultipleAssetHistGuid(captor.capture());
		FindVesselIdsByMultipleAssetHistGuidsRequest request = captor.getValue();
		assertEquals(AssetModuleMethod.FIND_VESSEL_IDS_BY_MULTIPLE_ASSET_HIST_GUID, request.getMethod());
		assertEquals(Arrays.asList(ASSET_GUID1, ASSET_GUID2), request.getAssetHistoryGuids());
	}

	@Test
	void testFindAssetHistoryGuid() {
		FindAssetHistGuidByAssetGuidAndOccurrenceDateResponse response = new FindAssetHistGuidByAssetGuidAndOccurrenceDateResponse();
		response.setAssetHistGuid(ASSET_HIST_GUID);
		Date date = new Date();
		when(assetClient.findAssetHistGuidByAssetGuidAndOccurrenceDate(any())).thenReturn(response);
		String result = sut.findAssetHistoryGuid(ASSET_GUID1, date);
		assertEquals(ASSET_HIST_GUID, result);
		ArgumentCaptor<FindAssetHistGuidByAssetGuidAndOccurrenceDateRequest> captor = ArgumentCaptor.forClass(FindAssetHistGuidByAssetGuidAndOccurrenceDateRequest.class);
		verify(assetClient).findAssetHistGuidByAssetGuidAndOccurrenceDate(captor.capture());
		FindAssetHistGuidByAssetGuidAndOccurrenceDateRequest request = captor.getValue();
		assertEquals(AssetModuleMethod.FIND_ASSET_HIST_GUID_BY_ASSET_GUID_AND_OCCURRENCE_DATE, request.getMethod());
		assertEquals(ASSET_GUID1, request.getAssetGuid());
		assertEquals(date, request.getOccurrenceDate());
	}
}
