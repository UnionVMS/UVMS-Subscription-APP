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
import static eu.europa.fisheries.uvms.subscription.model.enums.SubscriptionVesselIdentifier.CFR;
import static eu.europa.fisheries.uvms.subscription.model.enums.SubscriptionVesselIdentifier.ICCAT;
import static eu.europa.fisheries.uvms.subscription.model.enums.SubscriptionVesselIdentifier.IRCS;
import static eu.europa.fisheries.uvms.subscription.model.enums.SubscriptionVesselIdentifier.UVI;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import com.ninja_squad.dbsetup.DbSetup;
import com.ninja_squad.dbsetup.destination.DataSourceDestination;
import com.ninja_squad.dbsetup.operation.Operation;
import eu.europa.ec.fisheries.uvms.subscription.helper.SubscriptionTestHelper;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.AreaEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.EmailBodyEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionOutput;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.SubscriptionListQuery;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.SubscriptionSearchCriteria;
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
import eu.europa.fisheries.uvms.subscription.model.enums.ColumnType;
import eu.europa.fisheries.uvms.subscription.model.enums.DirectionType;
import eu.europa.fisheries.uvms.subscription.model.enums.OutgoingMessageType;
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
                DELETE_ALL, INSERT_SUBSCRIPTION, INSERT_AREA
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
                Arguments.of(createDateRangeQuery("20210101", "20211231"), new long[] {},        "Fulllly older than the oldest subscription")
        );
    }

    @ParameterizedTest
    @MethodSource("queryParametersWithOrderingAsc")
    public void testListSubscriptionWithOrderingAsc(SubscriptionListQuery query, long firstResultId, long lastResultId){
        List<SubscriptionEntity> subscriptionEntities = sut.listSubscriptions(query);
        assertTrue(subscriptionEntities.size() >= 2);
        assertEquals(firstResultId, subscriptionEntities.get(0).getId());
        assertEquals(lastResultId, subscriptionEntities.get(subscriptionEntities.size()-1).getId());
    }

    protected static Stream<Arguments> queryParametersWithOrderingAsc(){
        return Stream.of(
                Arguments.of(createQuery(null, null, null, null, null, null, null, null, null, null, DirectionType.ASC, ColumnType.NAME, null), 1L, 4L),
                Arguments.of(createQuery(null, null, null, null, null, null, null, null, null, null, DirectionType.ASC, ColumnType.DESCRIPTION, null), 1L, 4L),
                Arguments.of(createQuery(null, null, null, null, null, null, null, null, null, null, DirectionType.ASC, ColumnType.MESSAGETYPE, null), 2L, 4L)
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
        List<SubscriptionSearchCriteria.AreaCriterion> areas = Arrays.asList(new SubscriptionSearchCriteria.AreaCriterion(AreaType.EEZ, 101L), new SubscriptionSearchCriteria.AreaCriterion(AreaType.PORT, 222L));
        criteria.setActive(true);
        criteria.setInAnyArea(areas);
        List<SubscriptionEntity> results = sut.listSubscriptions(criteria);
        assertEquals(1, results.size());
        assertEquals(1L, results.get(0).getId());
    }

    @Test
    void testFindWithEmptyAreaCriteria() {
        SubscriptionSearchCriteriaImpl criteria = new SubscriptionSearchCriteriaImpl();
        List<SubscriptionSearchCriteria.AreaCriterion> areas = java.util.Collections.emptyList();
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
    public void createSubscriptionWithNullAreas() {
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        SubscriptionEntity subscription = SubscriptionTestHelper.random();
        subscription.setAreas(null);

        Long id = sut.createEntity(subscription).getId();
        em.flush();

        SubscriptionEntity createdSubscription = sut.findById(id);
        assertNull(createdSubscription.getAreas());
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
    public void updateSubscriptionWithNullAreas() {
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        SubscriptionEntity subscription = SubscriptionTestHelper.random();
        subscription.setAreas(null);
        Long id = sut.createEntity(subscription).getId();
        SubscriptionEntity createdSubscription = sut.findById(id);


        //update entity areas
        AreaEntity newArea1 = new AreaEntity();
        newArea1.setGid(10L);
        newArea1.setAreaType(AreaType.USERAREA);

        Long updatedId = sut.update(createdSubscription).getId();

        em.flush();

        SubscriptionEntity updatedSubscription = sut.findById(updatedId);
        assertNull(updatedSubscription.getAreas());
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
