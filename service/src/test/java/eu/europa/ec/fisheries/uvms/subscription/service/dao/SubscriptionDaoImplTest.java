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
import static eu.europa.ec.fisheries.uvms.subscription.helper.SubscriptionTestHelper.createDateRangeQuery;
import static eu.europa.ec.fisheries.uvms.subscription.helper.SubscriptionTestHelper.createQuery;
import static eu.europa.ec.fisheries.uvms.subscription.helper.SubscriptionTestHelper.zdt;
import static eu.europa.fisheries.uvms.subscription.model.enums.SubscriptionExecutionStatusType.PENDING;
import static eu.europa.fisheries.uvms.subscription.model.enums.SubscriptionFaReportDocumentType.DECLARATION;
import static eu.europa.fisheries.uvms.subscription.model.enums.SubscriptionFaReportDocumentType.NOTIFICATION;
import static eu.europa.fisheries.uvms.subscription.model.enums.SubscriptionVesselIdentifier.CFR;
import static eu.europa.fisheries.uvms.subscription.model.enums.SubscriptionVesselIdentifier.ICCAT;
import static eu.europa.fisheries.uvms.subscription.model.enums.SubscriptionVesselIdentifier.IRCS;
import static eu.europa.fisheries.uvms.subscription.model.enums.SubscriptionVesselIdentifier.UVI;
import static eu.europa.fisheries.uvms.subscription.model.enums.TriggeredSubscriptionStatus.ACTIVE;
import static java.util.stream.Collectors.toSet;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.params.ParameterizedTest.INDEX_PLACEHOLDER;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import com.ninja_squad.dbsetup.DbSetup;
import com.ninja_squad.dbsetup.destination.DataSourceDestination;
import com.ninja_squad.dbsetup.operation.Operation;
import eu.europa.ec.fisheries.uvms.subscription.helper.SubscriptionTestHelper;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.AreaEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.AssetEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.AssetGroupEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.EmailBodyEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionExecutionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionFishingActivity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionOutput;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionSubscriber;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionDataEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.AreaCriterion;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.SubscriptionListQuery;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.SubscriptionSearchCriteria;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.SubscriptionSearchCriteria.SenderCriterion;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.list.SubscriptionListDto;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.search.PaginationDataImpl;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.search.SubscriptionListQueryImpl;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.search.SubscriptionSearchCriteriaImpl;
import eu.europa.ec.fisheries.uvms.subscription.service.mapper.CustomMapper;
import eu.europa.ec.fisheries.uvms.subscription.service.mapper.SubscriptionMapper;
import eu.europa.ec.fisheries.uvms.subscription.service.mapper.SubscriptionMapperImpl;
import eu.europa.ec.fisheries.wsdl.subscription.module.AreaType;
import eu.europa.ec.fisheries.wsdl.user.types.Channel;
import eu.europa.ec.fisheries.wsdl.user.types.EndPoint;
import eu.europa.ec.fisheries.wsdl.user.types.Organisation;
import eu.europa.fisheries.uvms.subscription.model.enums.AssetType;
import eu.europa.fisheries.uvms.subscription.model.enums.ColumnType;
import eu.europa.fisheries.uvms.subscription.model.enums.DirectionType;
import eu.europa.fisheries.uvms.subscription.model.enums.OutgoingMessageType;
import eu.europa.fisheries.uvms.subscription.model.enums.SubscriptionTimeUnit;
import eu.europa.fisheries.uvms.subscription.model.enums.TriggerType;
import eu.europa.fisheries.uvms.subscription.model.exceptions.EntityDoesNotExistException;
import lombok.SneakyThrows;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mapstruct.ap.internal.util.Collections;

@EnableAutoWeld
public class SubscriptionDaoImplTest extends BaseSubscriptionInMemoryTest {

    @Produces
    private final SubscriptionMapper mapper = new SubscriptionMapperImpl();

    @Inject
    private CustomMapper customMapper;

    @Inject
    private SubscriptionDaoImpl sut;

    @Produces
    EntityManager getEntityManager() {
        return em;
    }

    @BeforeEach
    public void prepare(){
        Operation operation = sequenceOf(
                DELETE_ALL, INSERT_SUBSCRIPTION, INSERT_AREA, INSERT_ASSET, INSERT_ASSET_GROUP
        );

        DbSetup dbSetup = new DbSetup(new DataSourceDestination(ds), operation);
        dbSetupTracker.launchIfNecessary(dbSetup);
    }

    @Test
    void testFindById() {
        SubscriptionEntity subscription = sut.findById(3L);
        assertNotNull(subscription);
        assertEquals("subscription3", subscription.getName());
        assertEquals("C lorem ipsum", subscription.getDescription());
        assertEquals(3, subscription.getDeadline());
        assertEquals(SubscriptionTimeUnit.MONTHS, subscription.getDeadlineUnit());
        assertTrue(subscription.isStopWhenQuitArea());
    }

    @Test
    void testCount() {
        SubscriptionListQuery query = createQuery(null, true, null, null, null, "", null, null, null, null, null, null, null);
        Long count = sut.count(query.getCriteria());
        assertEquals(3L, count);
    }

    @ParameterizedTest
    @MethodSource("queryParametersWithCriteria")
    public void testListSubscriptionWithCriteria(SubscriptionListQuery query, int expected){
        List<SubscriptionEntity> subscriptionEntities = sut.listSubscriptions(query);
        assertEquals(expected, subscriptionEntities.size());
    }

    protected static Stream<Arguments> queryParametersWithCriteria(){
        return Stream.of(
                Arguments.of(createQuery("subscription3", null, null, null, null, "", null, null, null, null, null, null, null),1),
                Arguments.of(createQuery("", null, null, 2L, 11L, "", null, null, null, null, null, null, null),4),
                Arguments.of(createQuery("", null, null, null, null, "", null, null, null, null, null, null, null),4),
                Arguments.of(createQuery("3", null, null, null, null, "", null, null, null, null, null, null, null),1),
                Arguments.of(createQuery("subscription2", null, null, null, null, "", null, null, null, OutgoingMessageType.FA_QUERY, null, null, null),1),
                Arguments.of(createQuery("", true, null, null, null, "", null, null, null, null, null, null, null),3),
                Arguments.of(createQuery("subscription4", false, 11L, null, 11L, "", null, null, null, null, null, null, null),1),
                Arguments.of(createQuery("", true, null, null, null, "tade", null, null, null, null, null, null, null),2),
                Arguments.of(createQuery("", true, null, null, null, "", null, null, zdt("20190101"), null, null, null, null),1),
                Arguments.of(createQuery("", true, null, null, null, "", null, null, zdt("20190101"), null, null, null, java.util.Collections.emptyList()),1),
                Arguments.of(createQuery("", true, null, null, null, "", null, null, null, null, null, null, java.util.Collections.singleton(TriggerType.MANUAL)),1),
                Arguments.of(createQuery("", true, null, null, null, "", null, null, null, null, null, null, Arrays.asList(TriggerType.SCHEDULER, TriggerType.MANUAL)),2)
        );
    }

    @ParameterizedTest(name = "[" + INDEX_PLACEHOLDER + "] {2}")
    @MethodSource("dateRangeArguments")
    void testDateRange(SubscriptionListQuery query, long[] expectedIds, @SuppressWarnings("unused") String descriptionOfTestCase) {
        List<SubscriptionEntity> results = sut.listSubscriptions(query);
        assertEquals(LongStream.of(expectedIds).boxed().collect(toSet()), results.stream().map(SubscriptionEntity::getId).collect(toSet()));
    }

    protected static Stream<Arguments> dateRangeArguments(){
        return Stream.of(
                Arguments.of(createDateRangeQuery(null,       null),       new long[] {1,2,3,4}, "No restriction"),
                Arguments.of(createDateRangeQuery("20160101", "20161231"), new long[] {},        "Older than the oldest subscription"),
                Arguments.of(createDateRangeQuery("20160101", "20171231"), new long[] {},        "Partially older than the oldest subscription"),
                Arguments.of(createDateRangeQuery("20170101", "20171231"), new long[] {1},       "Fully in the oldest subscription"),
                Arguments.of(createDateRangeQuery("20180101", "20181230"), new long[] {1,2},     "Fully in the 2 oldest subscriptions"),
                Arguments.of(createDateRangeQuery("20180601", null),       new long[] {1,2},     "Start date in the 2 oldest subscriptions"),
                Arguments.of(createDateRangeQuery(null, "20180601"),       new long[] {1,2},     "End date in the 2 oldest subscriptions"),
                Arguments.of(createDateRangeQuery("20200101", "20211231"), new long[] {},        "Partially older than the oldest subscription"),
                Arguments.of(createDateRangeQuery("20210101", "20211231"), new long[] {},        "Fully older than the oldest subscription")
        );
    }

    @ParameterizedTest
    @MethodSource("queryParametersWithOrderingAsc")
    public void testListSubscriptionWithOrderingAsc(SubscriptionListQuery query, Long[] expectedResultIds) {
        List<Long> resultIds = sut.listSubscriptions(query).stream().map(SubscriptionEntity::getId).collect(Collectors.toList());
        assertEquals(Arrays.asList(expectedResultIds), resultIds);
    }

    protected static Stream<Arguments> queryParametersWithOrderingAsc() {
        return Stream.of(
                Arguments.of(createQuery(null, null, null, null, null, null, null, null, null, null, DirectionType.ASC, ColumnType.NAME, null), new Long[] {1L, 2L, 3L, 4L}),
                Arguments.of(createQuery(null, null, null, null, null, null, null, null, null, null, DirectionType.ASC, ColumnType.DESCRIPTION, null), new Long[] {1L, 2L, 3L, 4L}),
                Arguments.of(createQuery(null, null, null, null, null, null, null, null, null, null, DirectionType.ASC, ColumnType.MESSAGETYPE, null), new Long[] {2L, 3L, 1L, 4L}),
                Arguments.of(createQuery(null, null, null, null, null, null, null, null, null, null, DirectionType.ASC, ColumnType.TRIGGERTYPE, null), new Long[] {2L, 3L, 1L, 4L})
        );
    }

    @Test
    void testListSubscriptionWithNullOrderByData() {
        SubscriptionListQueryImpl query = new SubscriptionListQueryImpl();
        query.setCriteria(new SubscriptionSearchCriteriaImpl());
        query.setPagination(new PaginationDataImpl(0,25));
        List<SubscriptionEntity> results = sut.listSubscriptions(query);
        Integer numberOfSavedSubscriptions = findAllSubscriptions().size();
        assertEquals(numberOfSavedSubscriptions, results.size());
    }

    @Test
    void testFindByAreas() {
        SubscriptionSearchCriteriaImpl criteria = new SubscriptionSearchCriteriaImpl();
        List<AreaCriterion> areas = Arrays.asList(new AreaCriterion(AreaType.EEZ, 101L), new AreaCriterion(AreaType.PORT, 222L));
        criteria.setActive(true);
        criteria.setInAnyArea(areas);
        List<SubscriptionEntity> results = sut.listSubscriptions(criteria);
        assertEquals(1, results.size());
        assertEquals(1L, results.get(0).getId());
    }

    @Test
    void testFindByAreasAllowNoAssets() {
        SubscriptionSearchCriteriaImpl criteria = new SubscriptionSearchCriteriaImpl();
        List<AreaCriterion> areas = java.util.Collections.singletonList(new AreaCriterion(AreaType.USERAREA, 103L));
        criteria.setActive(true);
        criteria.setInAnyArea(areas);
        List<SubscriptionSearchCriteria.AssetCriterion> assets = Arrays.asList(new SubscriptionSearchCriteria.AssetCriterion(AssetType.ASSET, "asset_guid_1"), new SubscriptionSearchCriteria.AssetCriterion(AssetType.ASSET, "asset_guid_2"));
        criteria.setWithAnyAsset(assets);
        criteria.setAllowWithNoAsset(true);
        List<SubscriptionEntity> results = sut.listSubscriptions(criteria);
        assertEquals(1, results.size());
        assertEquals(2L, results.get(0).getId());
    }

    @Test
    void testFindByAssets() {
        SubscriptionSearchCriteriaImpl criteria = new SubscriptionSearchCriteriaImpl();
        List<SubscriptionSearchCriteria.AssetCriterion> assets = Arrays.asList(new SubscriptionSearchCriteria.AssetCriterion(AssetType.ASSET, "asset_guid_1"), new SubscriptionSearchCriteria.AssetCriterion(AssetType.ASSET, "asset_guid_2"));
        criteria.setActive(true);
        criteria.setWithAnyAsset(assets);
        List<SubscriptionEntity> results = sut.listSubscriptions(criteria);
        assertEquals(1, results.size());
        assertEquals(1L, results.get(0).getId());
    }

    @Test
    void testFindByAssetGroups() {
        SubscriptionSearchCriteriaImpl criteria = new SubscriptionSearchCriteriaImpl();
        List<SubscriptionSearchCriteria.AssetCriterion> assets = java.util.Collections.singletonList(new SubscriptionSearchCriteria.AssetCriterion(AssetType.VGROUP, "asset_group_guid_1"));
        criteria.setActive(true);
        criteria.setWithAnyAsset(assets);
        List<SubscriptionEntity> results = sut.listSubscriptions(criteria);
        assertEquals(1, results.size());
        assertEquals(2L, results.get(0).getId());
    }

    @Test
    void testFindByAssetsAndAssetGroups() {
        SubscriptionSearchCriteriaImpl criteria = new SubscriptionSearchCriteriaImpl();
        List<SubscriptionSearchCriteria.AssetCriterion> assets = Arrays.asList(new SubscriptionSearchCriteria.AssetCriterion(AssetType.ASSET, "asset_guid_1"), new SubscriptionSearchCriteria.AssetCriterion(AssetType.VGROUP, "asset_group_guid_1"));
        criteria.setActive(true);
        criteria.setWithAnyAsset(assets);
        List<SubscriptionEntity> results = sut.listSubscriptions(criteria);
        assertEquals(2, results.size());
        assertEquals(new HashSet<>(Arrays.asList(1L, 2L)), results.stream().map(SubscriptionEntity::getId).collect(toSet()));
    }

    @Test
    void testFindByAssetsAndAssetGroupsAllowNoAreas() {
        SubscriptionSearchCriteriaImpl criteria = new SubscriptionSearchCriteriaImpl();
        List<SubscriptionSearchCriteria.AssetCriterion> assets = Arrays.asList(new SubscriptionSearchCriteria.AssetCriterion(AssetType.ASSET, "asset_guid_3"), new SubscriptionSearchCriteria.AssetCriterion(AssetType.VGROUP, "asset_group_guid_1"));
        List<AreaCriterion> areas = java.util.Collections.singletonList(new AreaCriterion(AreaType.USERAREA, 999L));
        criteria.setActive(true);
        criteria.setWithAnyAsset(assets);
        criteria.setInAnyArea(areas);
        criteria.setAllowWithNoArea(true);
        List<SubscriptionEntity> results = sut.listSubscriptions(criteria);
        assertEquals(java.util.Collections.singleton(3L), results.stream().map(SubscriptionEntity::getId).collect(toSet()));
    }

    @Test
    void testFindWithEmptyAreaCriteria() {
        SubscriptionSearchCriteriaImpl criteria = new SubscriptionSearchCriteriaImpl();
        List<AreaCriterion> areas = java.util.Collections.emptyList();
        criteria.setInAnyArea(areas);
        List<SubscriptionEntity> results = sut.listSubscriptions(criteria);
        Integer numberOfSavedSubscriptions = findAllSubscriptions().size();
        assertEquals(numberOfSavedSubscriptions, results.size());
    }

    @Test
    public void createSubscriptionWithAreas() {
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        AreaEntity area1 = new AreaEntity();
        area1.setGid(1L);
        area1.setAreaType(AreaType.PORT);

        AreaEntity area2 = new AreaEntity();
        area2.setGid(2L);
        area2.setAreaType(AreaType.USERAREA);

        SubscriptionEntity subscription = SubscriptionTestHelper.random();
        subscription.setAreas(Collections.asSet(area1, area2));

        Long id = sut.createEntity(subscription).getId();
        em.flush();

        SubscriptionEntity createdSubscription = sut.findById(id);
        assertNotNull(createdSubscription.getAreas());
        assertEquals(2, createdSubscription.getAreas().size());
        assertTrue(createdSubscription.getAreas().contains(area1));
        assertTrue(createdSubscription.getAreas().contains(area2));
    }

    @Test
    public void createSubscriptionWithAssets() {
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        AssetEntity asset1 = new AssetEntity();
        asset1.setGuid("guid1");
        asset1.setName("name1");

        AssetEntity asset2 = new AssetEntity();
        asset2.setGuid("guid2");
        asset2.setName("name2");

        SubscriptionEntity subscription = SubscriptionTestHelper.random();
        subscription.setAssets(Collections.asSet(asset1, asset2));

        Long id = sut.createEntity(subscription).getId();
        em.flush();

        SubscriptionEntity createdSubscription = sut.findById(id);
        assertNotNull(createdSubscription.getAreas());
        assertEquals(2, createdSubscription.getAssets().size());
        assertTrue(createdSubscription.getAssets().contains(asset1));
        assertTrue(createdSubscription.getAssets().contains(asset2));
    }

    @Test
    public void createSubscriptionWithAssetGroups() {
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        AssetGroupEntity assetGroup1 = new AssetGroupEntity();
        assetGroup1.setGuid("guid1");
        assetGroup1.setName("name1");

        AssetGroupEntity assetGroup2 = new AssetGroupEntity();
        assetGroup2.setGuid("guid2");
        assetGroup2.setName("name2");

        SubscriptionEntity subscription = SubscriptionTestHelper.random();
        subscription.setAssetGroups(Collections.asSet(assetGroup1, assetGroup2));

        Long id = sut.createEntity(subscription).getId();
        em.flush();

        SubscriptionEntity createdSubscription = sut.findById(id);
        assertNotNull(createdSubscription.getAreas());
        assertEquals(2, createdSubscription.getAssetGroups().size());
        assertTrue(createdSubscription.getAssetGroups().contains(assetGroup1));
        assertTrue(createdSubscription.getAssetGroups().contains(assetGroup2));
    }

    @Test
    public void createSubscriptionWithEmptyAreas() {
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        SubscriptionEntity subscription = SubscriptionTestHelper.random();
        subscription.setAreas(java.util.Collections.emptySet());

        Long id = sut.createEntity(subscription).getId();
        em.flush();

        SubscriptionEntity createdSubscription = sut.findById(id);
        assertTrue(createdSubscription.getAreas().isEmpty());
    }

    @Test
    public void createSubscriptionWithEmptyAssetsAndAssetGroups() {
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        SubscriptionEntity subscription = SubscriptionTestHelper.random();
        subscription.setAssets(java.util.Collections.emptySet());
        subscription.setAssetGroups(java.util.Collections.emptySet());

        Long id = sut.createEntity(subscription).getId();
        em.flush();

        SubscriptionEntity createdSubscription = sut.findById(id);
        assertTrue(createdSubscription.getAssets().isEmpty());
        assertTrue(createdSubscription.getAssetGroups().isEmpty());
    }

    @Test
    public void updateSubscriptionWithNewAreas() {
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        //create entity
        AreaEntity area1 = new AreaEntity();
        area1.setGid(1L);
        area1.setAreaType(AreaType.PORT);
        AreaEntity area2 = new AreaEntity();
        area2.setGid(2L);
        area2.setAreaType(AreaType.USERAREA);
        SubscriptionEntity subscription = SubscriptionTestHelper.random();
        subscription.setAreas(Collections.asSet(area1, area2));
        Long id = sut.createEntity(subscription).getId();
        SubscriptionEntity createdSubscription = sut.findById(id);


        //update entity areas
        AreaEntity newArea1 = new AreaEntity();
        newArea1.setGid(10L);
        newArea1.setAreaType(AreaType.USERAREA);
        AreaEntity newArea2 = new AreaEntity();
        newArea2.setGid(11L);
        newArea2.setAreaType(AreaType.FAO);
        AreaEntity newArea3 = new AreaEntity();
        newArea3.setGid(12L);
        newArea3.setAreaType(AreaType.STATRECT);

        createdSubscription.setAreas(Collections.asSet(newArea1, newArea2, newArea3));
        Long updatedId = sut.update(createdSubscription).getId();

        em.flush();

        SubscriptionEntity updatedSubscription = sut.findById(updatedId);
        assertNotNull(updatedSubscription.getAreas());
        assertEquals(3, updatedSubscription.getAreas().size());
        assertTrue(updatedSubscription.getAreas().contains(newArea1));
        assertTrue(updatedSubscription.getAreas().contains(newArea2));
        assertTrue(updatedSubscription.getAreas().contains(newArea3));
        assertFalse(updatedSubscription.getAreas().contains(area1));
        assertFalse(updatedSubscription.getAreas().contains(area2));
    }

    @Test
    public void updateSubscriptionWithNewAssets() {
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        //create entity
        AssetEntity asset1 = new AssetEntity();
        asset1.setGuid("uu1");
        asset1.setName("name1");
        AssetEntity asset2 = new AssetEntity();
        asset2.setGuid("uu2");
        asset2.setName("name2");

        SubscriptionEntity subscription = SubscriptionTestHelper.random();
        subscription.setAssets(Collections.asSet(asset1, asset2));
        Long id = sut.createEntity(subscription).getId();
        SubscriptionEntity createdSubscription = sut.findById(id);


        //update entity assets

        AssetEntity newAsset1 = new AssetEntity();
        newAsset1.setGuid("new_uu1");
        newAsset1.setName("new_name1");
        AssetEntity newAsset2 = new AssetEntity();
        newAsset2.setGuid("new_uu2");
        newAsset2.setName("new_name2");
        AssetEntity newAsset3 = new AssetEntity();
        newAsset3.setGuid("new_uu3");
        newAsset3.setName("new_name3");

        createdSubscription.setAssets(Collections.asSet(newAsset1, newAsset2, newAsset3));
        Long updatedId = sut.update(createdSubscription).getId();

        em.flush();

        SubscriptionEntity updatedSubscription = sut.findById(updatedId);
        assertNotNull(updatedSubscription.getAssets());
        assertEquals(3, updatedSubscription.getAssets().size());
        assertTrue(updatedSubscription.getAssets().contains(newAsset1));
        assertTrue(updatedSubscription.getAssets().contains(newAsset2));
        assertTrue(updatedSubscription.getAssets().contains(newAsset3));
        assertFalse(updatedSubscription.getAssets().contains(asset1));
        assertFalse(updatedSubscription.getAssets().contains(asset2));
    }

    @Test
    public void updateSubscriptionWithNewAssetGroups() {
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        //create entity
        AssetGroupEntity assetGroup1 = new AssetGroupEntity();
        assetGroup1.setGuid("uu1");
        assetGroup1.setName("name1");
        AssetGroupEntity assetGroup2 = new AssetGroupEntity();
        assetGroup2.setGuid("uu2");
        assetGroup2.setName("name2");

        SubscriptionEntity subscription = SubscriptionTestHelper.random();
        subscription.setAssetGroups(Collections.asSet(assetGroup1, assetGroup2));
        Long id = sut.createEntity(subscription).getId();
        SubscriptionEntity createdSubscription = sut.findById(id);


        //update entity assets

        AssetGroupEntity newAssetGroup1 = new AssetGroupEntity();
        newAssetGroup1.setGuid("new_uu1");
        newAssetGroup1.setName("new_name1");
        AssetGroupEntity newAssetGroup2 = new AssetGroupEntity();
        newAssetGroup2.setGuid("new_uu2");
        newAssetGroup2.setName("new_name2");
        AssetGroupEntity newAssetGroup3 = new AssetGroupEntity();
        newAssetGroup3.setGuid("new_uu3");
        newAssetGroup3.setName("new_name3");

        createdSubscription.setAssetGroups(Collections.asSet(newAssetGroup1, newAssetGroup2, newAssetGroup3));
        Long updatedId = sut.update(createdSubscription).getId();

        em.flush();

        SubscriptionEntity updatedSubscription = sut.findById(updatedId);
        assertNotNull(updatedSubscription.getAssetGroups());
        assertEquals(3, updatedSubscription.getAssetGroups().size());
        assertTrue(updatedSubscription.getAssetGroups().contains(newAssetGroup1));
        assertTrue(updatedSubscription.getAssetGroups().contains(newAssetGroup2));
        assertTrue(updatedSubscription.getAssetGroups().contains(newAssetGroup3));
        assertFalse(updatedSubscription.getAssetGroups().contains(assetGroup1));
        assertFalse(updatedSubscription.getAssetGroups().contains(assetGroup2));
    }

    @Test
    public void updateSubscriptionAreas() {
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        //create entity
        AreaEntity area1 = new AreaEntity();
        area1.setGid(1L);
        area1.setAreaType(AreaType.PORT);
        AreaEntity area2 = new AreaEntity();
        area2.setGid(2L);
        area2.setAreaType(AreaType.USERAREA);
        SubscriptionEntity subscription = SubscriptionTestHelper.random();
        subscription.setAreas(Collections.asSet(area1, area2));
        Long id = sut.createEntity(subscription).getId();
        SubscriptionEntity createdSubscription = sut.findById(id);


        //update entity areas
        AreaEntity newArea1 = new AreaEntity();
        newArea1.setGid(10L);
        newArea1.setAreaType(AreaType.USERAREA);

        createdSubscription.setAreas(Collections.asSet(area1, newArea1));
        Long updatedId = sut.update(createdSubscription).getId();

        em.flush();

        SubscriptionEntity updatedSubscription = sut.findById(updatedId);
        assertNotNull(updatedSubscription.getAreas());
        assertEquals(2, updatedSubscription.getAreas().size());
        assertTrue(updatedSubscription.getAreas().contains(newArea1));
        assertTrue(updatedSubscription.getAreas().contains(area1));
        assertFalse(updatedSubscription.getAreas().contains(area2));
    }

    @Test
    public void updateSubscriptionAssets() {
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        //create entity
        AssetEntity asset1 = new AssetEntity();
        asset1.setGuid("uu1");
        asset1.setName("name1");
        AssetEntity asset2 = new AssetEntity();
        asset2.setGuid("uu2");
        asset2.setName("name2");
        SubscriptionEntity subscription = SubscriptionTestHelper.random();
        subscription.setAssets(Collections.asSet(asset1, asset2));
        Long id = sut.createEntity(subscription).getId();
        SubscriptionEntity createdSubscription = sut.findById(id);


        //update entity assets
        AssetEntity newAsset1 = new AssetEntity();
        newAsset1.setGuid("new_uu1");
        newAsset1.setName("new_name1");

        createdSubscription.setAssets(Collections.asSet(asset1, newAsset1));
        Long updatedId = sut.update(createdSubscription).getId();

        em.flush();

        SubscriptionEntity updatedSubscription = sut.findById(updatedId);
        assertNotNull(updatedSubscription.getAssets());
        assertEquals(2, updatedSubscription.getAssets().size());
        assertTrue(updatedSubscription.getAssets().contains(newAsset1));
        assertTrue(updatedSubscription.getAssets().contains(asset1));
        assertFalse(updatedSubscription.getAssets().contains(asset2));
    }


    @Test
    public void updateSubscriptionAssetGroups() {
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        //create entity
        AssetGroupEntity assetGroup1 = new AssetGroupEntity();
        assetGroup1.setGuid("uu1");
        assetGroup1.setName("name1");
        AssetGroupEntity assetGroup2 = new AssetGroupEntity();
        assetGroup2.setGuid("uu2");
        assetGroup2.setName("name2");
        SubscriptionEntity subscription = SubscriptionTestHelper.random();
        subscription.setAssetGroups(Collections.asSet(assetGroup1, assetGroup2));
        Long id = sut.createEntity(subscription).getId();
        SubscriptionEntity createdSubscription = sut.findById(id);


        //update entity assets
        AssetGroupEntity newAssetGroup1 = new AssetGroupEntity();
        newAssetGroup1.setGuid("new_uu1");
        newAssetGroup1.setName("new_name1");

        createdSubscription.setAssetGroups(Collections.asSet(assetGroup1, newAssetGroup1));
        Long updatedId = sut.update(createdSubscription).getId();

        em.flush();

        SubscriptionEntity updatedSubscription = sut.findById(updatedId);
        assertNotNull(updatedSubscription.getAssets());
        assertEquals(2, updatedSubscription.getAssetGroups().size());
        assertTrue(updatedSubscription.getAssetGroups().contains(newAssetGroup1));
        assertTrue(updatedSubscription.getAssetGroups().contains(assetGroup1));
        assertFalse(updatedSubscription.getAssetGroups().contains(assetGroup2));
    }

    @Test
    public void updateSubscriptionWithEmptyAreas() {
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        SubscriptionEntity subscription = SubscriptionTestHelper.random();
        subscription.setAreas(java.util.Collections.emptySet());
        Long id = sut.createEntity(subscription).getId();

        SubscriptionEntity createdSubscription = sut.findById(id);
        createdSubscription.setName("new_name");
        Long updatedId = sut.update(createdSubscription).getId();

        em.flush();

        SubscriptionEntity updatedSubscription = sut.findById(updatedId);
        assertTrue(updatedSubscription.getAreas().isEmpty());
    }

    @Test
    public void updateSubscriptionWithEmptyAssets() {
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        SubscriptionEntity subscription = SubscriptionTestHelper.random();
        Long id = sut.createEntity(subscription).getId();

        SubscriptionEntity createdSubscription = sut.findById(id);
        createdSubscription.setName("new_name");
        Long updatedId = sut.update(createdSubscription).getId();

        em.flush();

        SubscriptionEntity updatedSubscription = sut.findById(updatedId);
        assertTrue(updatedSubscription.getAssets().isEmpty());
    }

    @Test
    public void updateSubscriptionWithEmptyAssetGroups() {
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        SubscriptionEntity subscription = SubscriptionTestHelper.random();
        Long id = sut.createEntity(subscription).getId();

        SubscriptionEntity createdSubscription = sut.findById(id);
        createdSubscription.setName("new_name");
        Long updatedId = sut.update(createdSubscription).getId();

        em.flush();

        SubscriptionEntity updatedSubscription = sut.findById(updatedId);
        assertTrue(updatedSubscription.getAssetGroups().isEmpty());
    }

    @Test
    public void deleteSubscriptionWithAreas() {
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        Integer originalNumberOfSavedAreas = findAllAreas().size();

        //create subscription
        AreaEntity area1 = new AreaEntity();
        area1.setGid(1L);
        area1.setAreaType(AreaType.PORT);
        AreaEntity area2 = new AreaEntity();
        area2.setGid(2L);
        area2.setAreaType(AreaType.USERAREA);
        SubscriptionEntity subscription = SubscriptionTestHelper.random();
        subscription.setAreas(Collections.asSet(area1, area2));
        Long id = sut.createEntity(subscription).getId();

        sut.delete(id);
        em.flush();
        Integer numberOfSavedAreas = findAllAreas().size();
        assertEquals(originalNumberOfSavedAreas, numberOfSavedAreas);
    }

    @Test
    public void deleteSubscriptionWithAssets() {
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        Integer originalNumberOfSavedAssets = findAllAssets().size();

        //create subscription
        AssetEntity asset1 = new AssetEntity();
        asset1.setGuid("uu1");
        asset1.setName("name1");
        AssetEntity asset2 = new AssetEntity();
        asset2.setGuid("uu2");
        asset2.setName("name2");
        SubscriptionEntity subscription = SubscriptionTestHelper.random();
        subscription.setAssets(Collections.asSet(asset1, asset2));
        Long id = sut.createEntity(subscription).getId();

        sut.delete(id);
        em.flush();
        Integer numberOfSavedAssets = findAllAssets().size();
        assertEquals(originalNumberOfSavedAssets, numberOfSavedAssets);
    }

    @Test
    public void deleteSubscriptionWithAssetGroups() {
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        Integer originalNumberOfSavedAssetGroups = findAllAssetGroups().size();

        //create subscription
        AssetGroupEntity assetGroup1 = new AssetGroupEntity();
        assetGroup1.setGuid("uu1");
        assetGroup1.setName("name1");
        AssetGroupEntity assetGroup2 = new AssetGroupEntity();
        assetGroup2.setGuid("uu2");
        assetGroup2.setName("name2");
        SubscriptionEntity subscription = SubscriptionTestHelper.random();
        subscription.setAssetGroups(Collections.asSet(assetGroup1, assetGroup2));
        Long id = sut.createEntity(subscription).getId();

        sut.delete(id);
        em.flush();
        Integer numberOfSavedAssetGroups = findAllAssetGroups().size();
        assertEquals(originalNumberOfSavedAssetGroups, numberOfSavedAssetGroups);
    }

    @Test
    @SneakyThrows
    public void testListSubscriptionForEnrichment(){
        List<SubscriptionEntity> subscriptionList = findAllSubscriptions();
        List<SubscriptionListDto> subscriptionEntityList = customMapper.enrichSubscriptionList(subscriptionList, fetchAllOrganisations() );

        for(SubscriptionListDto subscription: subscriptionEntityList){
            System.out.println(subscription.getOrganisationName() +
                    " - " + subscription.getChannelName() +
                    " - " + subscription.getEndpointName() );
            assertNotNull( subscription.getOrganisationName() );
            assertNotNull( subscription.getChannelName() );
            assertNotNull( subscription.getEndpointName() );
        }
    }

    @Test
    void testUpdate() {
        em.getTransaction().begin();
        SubscriptionEntity subscription = findAllSubscriptions().get(0);
        Set<AreaEntity> detachedAreas = subscription.getAreas().stream().peek(em::detach).collect(toSet());
        subscription.getAssets().forEach(em::detach);
        subscription.getAssetGroups().forEach(em::detach);
        em.detach(subscription);
        subscription.setAreas(detachedAreas);
        subscription.setDescription("updated description");
        sut.update(subscription);
        em.getTransaction().commit();
        em.clear();
        SubscriptionEntity updatedSubscription = findAllSubscriptions().get(0);
        assertEquals("updated description", updatedSubscription.getDescription());
    }

    @Test
    public void testCreateEmailBody() {
        em.getTransaction().begin();
        SubscriptionEntity subscription = findAllSubscriptions().get(0);
        EmailBodyEntity emailBody = new EmailBodyEntity(subscription, "lorem ipsum");
        sut.createEmailBodyEntity(emailBody);
        em.getTransaction().commit();
        em.clear();
        EmailBodyEntity createdEmailBody = findAllEmailBodies().get(0);
        assertEquals(subscription.getId(), emailBody.getSubscription().getId());
        assertEquals("lorem ipsum", createdEmailBody.getBody());
    }

    @Test
    public void testFindEmailBody() {
        em.getTransaction().begin();
        SubscriptionEntity subscription = findAllSubscriptions().get(0);
        EmailBodyEntity emailBody = new EmailBodyEntity(subscription, "lorem ipsum");
        sut.createEmailBodyEntity(emailBody);
        em.getTransaction().commit();
        em.clear();
        EmailBodyEntity createdEmailBody = sut.findEmailBodyEntity(emailBody.getSubscription().getId());
        assertEquals(subscription.getId(), emailBody.getSubscription().getId());
        assertEquals("lorem ipsum", createdEmailBody.getBody());
    }

    @Test
    public void testUpdateNonExistentEmailBody() {
        em.getTransaction().begin();
        SubscriptionEntity subscription = findAllSubscriptions().get(0);
        EmailBodyEntity emailBody = new EmailBodyEntity(subscription, "lorem ipsum");
        sut.updateEmailBodyEntity(emailBody);
        em.getTransaction().commit();
        em.clear();
        EmailBodyEntity createdEmailBody = findAllEmailBodies().get(0);
        assertEquals(subscription.getId(), emailBody.getSubscription().getId());
        assertEquals("lorem ipsum", createdEmailBody.getBody());
    }

    @Test
    public void testUpdateExistentEmailBody() {
        //create email body entity
        em.getTransaction().begin();
        SubscriptionEntity subscription = findAllSubscriptions().get(0);
        EmailBodyEntity createdEmailBody = new EmailBodyEntity(subscription, "lorem ipsum");
        sut.createEmailBodyEntity(createdEmailBody);

        //fetch email body entity
        EmailBodyEntity emailBody = findAllEmailBodies().get(0);
        emailBody.setBody("new body");
        sut.updateEmailBodyEntity(emailBody);
        em.getTransaction().commit();
        em.clear();

        EmailBodyEntity updatedEmailBody = findAllEmailBodies().get(0);
        assertEquals(subscription.getId(), updatedEmailBody.getSubscription().getId());
        assertEquals("new body", updatedEmailBody.getBody());
    }

    @Test
    public void testGetEmailConfigurationPassword() {
        em.getTransaction().begin();
        String password = sut.getEmailConfigurationPassword(4L);
        em.getTransaction().commit();
        em.clear();
        assertEquals("abcd1234", password);
    }

    @Test
    public void testUpdateEmailConfigurationPassword() {
        em.getTransaction().begin();
        sut.updateEmailConfigurationPassword(4L, "new_pass");
        em.getTransaction().commit();
        em.clear();
        String password = sut.getEmailConfigurationPassword(4L);
        assertEquals("new_pass", password);
    }

    @Test
    public void testGetNullEmailConfigurationPassword() {
        em.getTransaction().begin();
        String password = sut.getEmailConfigurationPassword(1L);
        em.getTransaction().commit();
        em.clear();
        assertNull(password);
    }

    @Test
    public void testUpdateEmptyEmailConfigurationPassword() {
        em.getTransaction().begin();
        sut.updateEmailConfigurationPassword(4L, "");
        em.getTransaction().commit();
        em.clear();
        String password = sut.getEmailConfigurationPassword(4L);
        assertEquals("", password);
    }

    @Test
    void testDeleteNonExisting() {
        em.getTransaction().begin();
        try {
            sut.delete(9999999L);
            fail("Should throw when asked to delete non-existing Subscription");
        } catch (EntityDoesNotExistException expected) {
            // expected
        }
    }

    @Test
    void testDelete() {
        em.getTransaction().begin();
        SubscriptionEntity subscription = SubscriptionTestHelper.random();
        Long id = sut.createEntity(subscription).getId();
        EmailBodyEntity email = new EmailBodyEntity(subscription, "email content");
        sut.createEmailBodyEntity(email);
        TriggeredSubscriptionEntity ts = new TriggeredSubscriptionEntity();
        ts.setStatus(ACTIVE);
        ts.setEffectiveFrom(new Date());
        ts.setCreationDate(new Date());
        ts.setSubscription(subscription);
        em.persist(ts);
        TriggeredSubscriptionDataEntity dataEntity = new TriggeredSubscriptionDataEntity();
        dataEntity.setKey("A");
        dataEntity.setValue("B");
        dataEntity.setTriggeredSubscription(ts);
        ts.getData().add(dataEntity);
        SubscriptionExecutionEntity ex1 = new SubscriptionExecutionEntity();
        ex1.setStatus(PENDING);
        ex1.setTriggeredSubscription(ts);
        ex1.setRequestedTime(new Date());
        ex1.setCreationDate(new Date());
        em.persist(ex1);
        SubscriptionExecutionEntity ex2 = new SubscriptionExecutionEntity();
        ex2.setStatus(PENDING);
        ex2.setTriggeredSubscription(ts);
        ex2.setRequestedTime(new Date());
        ex2.setCreationDate(new Date());
        em.persist(ex2);
        em.getTransaction().commit();
        em.clear();
        assertNotNull(em.find(SubscriptionEntity.class, id));
        em.clear();
        em.getTransaction().begin();

        sut.delete(id);

        em.getTransaction().commit();
        em.clear();
        assertNull(em.find(SubscriptionEntity.class, id));
    }

    @ParameterizedTest
    @MethodSource("inputForDate")
    void testFindScheduledSubscriptionsReadyForTriggering(Date now, int expectedResultSetSize) {
        List<Long> results = sut.findScheduledSubscriptionIdsForTriggering(now, 0, 10);
        assertEquals(expectedResultSetSize, results.size());
        if (results.size() > 0) {
            assertEquals(1L, results.get(0));
        }
    }

    protected static Stream<Arguments> inputForDate(){
        return Stream.of(
                Arguments.of(Date.from(Instant.parse("2016-12-30T12:00:00Z")), 0),
                Arguments.of(Date.from(Instant.parse("2020-06-25T12:00:00Z")), 1)
        );
    }

    @Test
    void testFindSubscriptionByName() {
        assertNotNull(sut.findSubscriptionByName("subscription2"));
        assertNull(sut.findSubscriptionByName("non-existing name"));
    }

    @Test
    void testReadVesselIds() {
        SubscriptionEntity subscription;
        subscription = em.find(SubscriptionEntity.class, 1L);
        assertTrue(subscription.getOutput().getVesselIds().isEmpty());
        subscription = em.find(SubscriptionEntity.class, 2L);
        assertEquals(EnumSet.of(IRCS), subscription.getOutput().getVesselIds());
        subscription = em.find(SubscriptionEntity.class, 3L);
        assertEquals(EnumSet.of(CFR, IRCS), subscription.getOutput().getVesselIds());
    }

    @Test
    void testWriteVesselIds() {
        em.getTransaction().begin();
        SubscriptionEntity subscription = SubscriptionTestHelper.random();
        SubscriptionOutput subscriptionOutput = new SubscriptionOutput();
        subscriptionOutput.setVesselIds(EnumSet.of(UVI, ICCAT));
        subscriptionOutput.setMessageType(OutgoingMessageType.NONE);
        subscription.setOutput(subscriptionOutput);
        Long id = sut.createEntity(subscription).getId();
        em.getTransaction().commit();
        em.clear();
        SubscriptionEntity subscriptionFromDb = em.find(SubscriptionEntity.class, id);
        assertNotNull(subscriptionFromDb);
        assertEquals(EnumSet.of(UVI, ICCAT), subscriptionFromDb.getOutput().getVesselIds());
    }

    @Test
    public void createAndUpdateSubscriptionWithActivities(){
        em.getTransaction().begin();
        SubscriptionEntity subscription = SubscriptionTestHelper.random();
        Set<SubscriptionFishingActivity> startActivities = new HashSet<>();
        SubscriptionFishingActivity startActivity = new SubscriptionFishingActivity(DECLARATION, "1");
        startActivities.add(startActivity);
        subscription.setStartActivities(startActivities);
        Set<SubscriptionFishingActivity> stopActivities = new HashSet<>();
        SubscriptionFishingActivity stopActivity = new SubscriptionFishingActivity(NOTIFICATION,"2");
        stopActivities.add(stopActivity);
        subscription.setStopActivities(stopActivities);
        Long id = sut.createEntity(subscription).getId();
        em.getTransaction().commit();
        em.clear();

        em.getTransaction().begin();
        SubscriptionEntity retrievedSubscription = sut.findById(id);
        SubscriptionFishingActivity start = retrievedSubscription.getStartActivities().iterator().next();
        SubscriptionFishingActivity stop = retrievedSubscription.getStopActivities().iterator().next();

        assertEquals(startActivity, start);
        assertEquals(stopActivity, stop);

        Set<SubscriptionFishingActivity> updatedStopActivities = new HashSet<>();
        SubscriptionFishingActivity stopActivity1 = new SubscriptionFishingActivity(NOTIFICATION,"22");
        SubscriptionFishingActivity stopActivity2 = new SubscriptionFishingActivity(DECLARATION,"23");
        updatedStopActivities.add(stopActivity1);
        updatedStopActivities.add(stopActivity2);
        retrievedSubscription.setStopActivities(updatedStopActivities);

        retrievedSubscription.getStartActivities().remove(start);
        retrievedSubscription.getStartActivities().add(new SubscriptionFishingActivity(DECLARATION, "3"));
        em.getTransaction().commit();
        em.clear();

        retrievedSubscription = sut.findById(id);
        assertEquals(1, retrievedSubscription.getStartActivities().size());
        assertEquals(DECLARATION, retrievedSubscription.getStartActivities().iterator().next().getType());
        assertEquals("3", retrievedSubscription.getStartActivities().iterator().next().getValue());
        assertEquals(2, retrievedSubscription.getStopActivities().size());
        assertEquals(Arrays.asList(NOTIFICATION, DECLARATION), retrievedSubscription.getStopActivities().stream().sorted(Comparator.comparing(SubscriptionFishingActivity::getValue)).map(SubscriptionFishingActivity::getType).collect(Collectors.toList()));
        assertEquals(Arrays.asList("22", "23"), retrievedSubscription.getStopActivities().stream().sorted(Comparator.comparing(SubscriptionFishingActivity::getValue)).map(SubscriptionFishingActivity::getValue).collect(Collectors.toList()));
    }

    @Test
    public void createAndUpdateSubscriptionWithSubscribers(){
        em.getTransaction().begin();
        SubscriptionEntity subscription = SubscriptionTestHelper.random();
        Set<SubscriptionSubscriber> senders = new HashSet<>();
        SubscriptionSubscriber subscriptionSubscriber = new SubscriptionSubscriber();
        subscriptionSubscriber.setEndpointId(1L);
        subscriptionSubscriber.setChannelId(2L);
        subscriptionSubscriber.setOrganisationId(3L);
        senders.add(subscriptionSubscriber);
        subscription.setSenders(senders);
        Long id = sut.createEntity(subscription).getId();
        em.getTransaction().commit();
        em.clear();

        em.getTransaction().begin();
        SubscriptionEntity retrievedSubscription = sut.findById(id);
        SubscriptionSubscriber retrievedSubscriptionSubscriber = retrievedSubscription.getSenders().iterator().next();

        assertEquals(retrievedSubscriptionSubscriber, subscriptionSubscriber);

        retrievedSubscription.setSenders(java.util.Collections.singleton(new SubscriptionSubscriber(4L,5L,6L)));
        em.getTransaction().commit();
        em.clear();

        retrievedSubscription = sut.findById(id);
        assertEquals(java.util.Collections.singleton(new SubscriptionSubscriber(4L,5L,6L)), retrievedSubscription.getSenders());
    }

    @Test
    public void testSubscriberCriterion() {
        em.getTransaction().begin();
        SubscriptionEntity s1 = sut.findById(1L);
        SubscriptionEntity s2 = sut.findById(2L);
        SubscriptionSubscriber ss1 = new SubscriptionSubscriber(1L, 2L, 3L);
        SubscriptionSubscriber ss2 = new SubscriptionSubscriber(4L, 2L, 6L);
        SubscriptionSubscriber ss3 = new SubscriptionSubscriber(4L, 8L, 3L);
        s1.getSenders().add(ss1);
        s1.getSenders().add(ss2);
        s2.getSenders().add(ss2);
        s2.getSenders().add(ss3);
        s1.setHasSenders(true);
        s2.setHasSenders(true);
        em.getTransaction().commit();
        em.clear();

        SubscriptionSearchCriteriaImpl criteria1 = new SubscriptionSearchCriteriaImpl();
        criteria1.setSender(new SenderCriterion(1L, 2L, 3L));
        List<SubscriptionEntity> result1 = sut.listSubscriptions(criteria1);
        assertEquals(java.util.Collections.singletonList(1L), result1.stream().map(SubscriptionEntity::getId).sorted().collect(Collectors.toList()));

        SubscriptionSearchCriteriaImpl criteria2 = new SubscriptionSearchCriteriaImpl();
        criteria2.setSender(new SenderCriterion(4L, 2L, 6L));
        List<SubscriptionEntity> result2 = sut.listSubscriptions(criteria2);
        assertEquals(Arrays.asList(1L,2L), result2.stream().map(SubscriptionEntity::getId).sorted().collect(Collectors.toList()));

        SubscriptionSearchCriteriaImpl criteria3 = new SubscriptionSearchCriteriaImpl();
        criteria3.setSender(new SenderCriterion(4L, 8L, 3L));
        criteria3.setAllowWithNoSenders(true);
        List<SubscriptionEntity> result3 = sut.listSubscriptions(criteria3);
        assertEquals(Arrays.asList(2L, 3L, 4L), result3.stream().map(SubscriptionEntity::getId).sorted().collect(Collectors.toList()));

        SubscriptionSearchCriteriaImpl criteria4 = new SubscriptionSearchCriteriaImpl();
        criteria4.setSender(new SenderCriterion(4L, 2L, 6L));
        criteria4.setInAnyArea(java.util.Collections.singletonList(new AreaCriterion(AreaType.EEZ, 101L)));
        List<SubscriptionEntity> result4 = sut.listSubscriptions(criteria4);
        assertEquals(java.util.Collections.singletonList(1L), result4.stream().map(SubscriptionEntity::getId).sorted().collect(Collectors.toList()));
    }

    private List<SubscriptionEntity> findAllSubscriptions() {
        TypedQuery<SubscriptionEntity> query = em.createQuery("SELECT s FROM SubscriptionEntity s ORDER BY s.id", SubscriptionEntity.class);
        return query.getResultList();
    }

    private List<EmailBodyEntity> findAllEmailBodies() {
        TypedQuery<EmailBodyEntity> query = em.createQuery("SELECT e FROM EmailBodyEntity e ORDER BY e.subscription.id", EmailBodyEntity.class);
        return query.getResultList();
    }

    private List<AreaEntity> findAllAreas() {
        TypedQuery<AreaEntity> query = em.createQuery("SELECT area FROM AreaEntity area ORDER BY area.subscription.id", AreaEntity.class);
        return query.getResultList();
    }

    private List<AssetEntity> findAllAssets() {
        TypedQuery<AssetEntity> query = em.createQuery("SELECT area FROM AssetEntity area ORDER BY area.subscription.id", AssetEntity.class);
        return query.getResultList();
    }

    private List<AssetGroupEntity> findAllAssetGroups() {
        TypedQuery<AssetGroupEntity> query = em.createQuery("SELECT area FROM AssetGroupEntity area ORDER BY area.subscription.id", AssetGroupEntity.class);
        return query.getResultList();
    }

    private static List<Organisation> fetchAllOrganisations() {

        List<Organisation> organisationList = new ArrayList<>(1);
        Organisation org1 = new Organisation();
        org1.setId( 1 );
        org1.setEmail( "org1@email.com" );
        org1.setParentOrganisation( "PARENT ORG 1 NAME" );
        org1.setName( "ORG1 NAME" );
        List<EndPoint> endpointList = org1.getEndPoints();
        EndPoint endpoint1 = new EndPoint();
        endpoint1.setId( 24 );
        endpoint1.setName( "FLUX.EEC" );
        endpoint1.setEnabled( true );
        List<Channel> channelList = endpoint1.getChannels();
        Channel channel1 = new Channel();
        channel1.setId( 1 );
        channel1.setDataFlow( "DataFlow" );
        channel1.setService( "Channel Service " );

        channelList.add( channel1 );

        EndPoint endpoint2 = new EndPoint();
        endpoint2.setId( 4 );
        endpoint2.setName( "FLUX.FRA" );
        endpoint2.setEnabled( true );
        List<Channel> channelList2 = endpoint2.getChannels();
        channelList2.add( channel1 );

        EndPoint endpoint3 = new EndPoint();
        endpoint3.setId( 1 );
        endpoint3.setName( "FLUX.GRC" );
        endpoint3.setEnabled( true );
        List<Channel> channelList3 = endpoint3.getChannels();
        channelList3.add( channel1 );

        endpointList.add( endpoint1 );
        endpointList.add( endpoint2 );
        endpointList.add( endpoint3 );

        Organisation org2 = new Organisation();
        org2.setId( 2 );
        org2.setEmail( "org2@email.com" );
        org2.setParentOrganisation( "ORG1 NAME" );
        org2.setName( "ORG2 NAME" );
        List<EndPoint> endpointList2 = org2.getEndPoints();
        endpointList2.add( endpoint1 );
        endpointList2.add( endpoint2 );
        endpointList2.add( endpoint3 );

        Organisation org3 = new Organisation();
        org3.setId( 4 );
        org3.setEmail( "org3@email.com" );
        org3.setParentOrganisation( "ORG1 NAME" );
        org3.setName( "ORG3 NAME" );
        List<EndPoint> endpointList3 = org3.getEndPoints();
        endpointList3.add( endpoint1 );
        endpointList3.add( endpoint2 );
        endpointList3.add( endpoint3 );


        organisationList.add( org1 );
        organisationList.add( org2 );
        organisationList.add( org3 );
        return organisationList;
    }
}
