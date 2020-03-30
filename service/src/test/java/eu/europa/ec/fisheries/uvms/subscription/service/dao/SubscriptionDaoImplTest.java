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
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ninja_squad.dbsetup.DbSetup;
import com.ninja_squad.dbsetup.destination.DataSourceDestination;
import com.ninja_squad.dbsetup.operation.Operation;
import eu.europa.ec.fisheries.uvms.subscription.helper.SubscriptionTestHelper;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.AreaEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.SubscriptionListQuery;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.list.SubscriptionListDto;
import eu.europa.ec.fisheries.uvms.subscription.service.mapper.CustomMapper;
import eu.europa.ec.fisheries.uvms.subscription.service.mapper.SubscriptionMapper;
import eu.europa.ec.fisheries.uvms.subscription.service.mapper.SubscriptionMapperImpl;
import eu.europa.ec.fisheries.wsdl.subscription.module.AreaType;
import eu.europa.ec.fisheries.wsdl.subscription.module.AreaValueType;
import eu.europa.ec.fisheries.wsdl.user.types.Channel;
import eu.europa.ec.fisheries.wsdl.user.types.EndPoint;
import eu.europa.ec.fisheries.wsdl.user.types.Organisation;
import eu.europa.fisheries.uvms.subscription.model.enums.ColumnType;
import eu.europa.fisheries.uvms.subscription.model.enums.DirectionType;
import eu.europa.fisheries.uvms.subscription.model.enums.OutgoingMessageType;
import lombok.SneakyThrows;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@EnableAutoWeld
public class SubscriptionDaoImplTest extends BaseSubscriptionInMemoryTest {

    private SubscriptionDaoImpl daoUnderTest = new SubscriptionDaoImpl(em);

    private ObjectMapper objectMapper = new ObjectMapper();

    @Produces
    private SubscriptionMapper mapper = new SubscriptionMapperImpl();

    @Inject
    private CustomMapper customMapper;

    @BeforeEach
    public void prepare(){
        Operation operation = sequenceOf(
                DELETE_ALL, INSERT_SUBSCRIPTION, INSERT_CONDITION, INSERT_AREA
        );

        DbSetup dbSetup = new DbSetup(new DataSourceDestination(ds), operation);
        dbSetupTracker.launchIfNecessary(dbSetup);
    }

    @ParameterizedTest
    @MethodSource("queryParametersWithCriteria")
    public void testListSubscriptionWithCriteria(SubscriptionListQuery query, int expected){
        List<SubscriptionEntity> subscriptionEntities = daoUnderTest.listSubscriptions(query);
        assertEquals(expected, subscriptionEntities.size());
    }

    protected static Stream<Arguments> queryParametersWithCriteria(){
        return Stream.of(
                Arguments.of(SubscriptionTestHelper.createQuery("subscription3", null, null, null, null, "", null, null, null, null, null),1),
                Arguments.of(SubscriptionTestHelper.createQuery("", null, null, null, 11L, "", null, null, null, null, null),4),
                Arguments.of(SubscriptionTestHelper.createQuery("", null, null, null, null, "", null, null, null, null, null),4),
                Arguments.of(SubscriptionTestHelper.createQuery("3", null, null, null, null, "", null, null, null, null, null),1),
                Arguments.of(SubscriptionTestHelper.createQuery("subscription2", null, null, null, null, "", null, null, OutgoingMessageType.FA_QUERY, null, null),1),
                Arguments.of(SubscriptionTestHelper.createQuery("", true, null, null, null, "", null, null, null, null, null),3),
                Arguments.of(SubscriptionTestHelper.createQuery("subscription4", false, 11L, null, 11L, "", null, null, null, null, null),1),
                Arguments.of(SubscriptionTestHelper.createQuery("", true, null, null, null, "tade", null, null, null, null, null),2)
        );
    }

    @ParameterizedTest
    @MethodSource("queryParametersWithOrderingAsc")
    public void testListSubscriptionWithOrderingAsc(SubscriptionListQuery query, long firstResultId, long lastResultId){
        List<SubscriptionEntity> subscriptionEntities = daoUnderTest.listSubscriptions(query);
        assertEquals(firstResultId, subscriptionEntities.get(0).getId());
        assertEquals(lastResultId, subscriptionEntities.get(subscriptionEntities.size()-1).getId());
    }

    protected static Stream<Arguments> queryParametersWithOrderingAsc(){
        return Stream.of(
                Arguments.of(SubscriptionTestHelper.createQuery("", null, null, null, null, "", null, null, null, DirectionType.ASC, ColumnType.NAME), 1L, 4L),
                Arguments.of(SubscriptionTestHelper.createQuery("", null, null, null, null, "", null, null, null, DirectionType.ASC, ColumnType.DESCRIPTION), 1L, 4L),
                Arguments.of(SubscriptionTestHelper.createQuery("", null, null, null, null, "", null, null, null, DirectionType.ASC, ColumnType.MESSAGETYPE), 2L, 4L)
        );
    }

    @Test
    @SneakyThrows
    public void testCreateSubscriptionWithArea(){

        int sizeBefore = findAllSubscriptions().size();
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        SubscriptionEntity subscription = SubscriptionTestHelper.random();
//        subscription.addArea(SubscriptionTestHelper.randomArea());

        Long id = daoUnderTest.createEntity(subscription).getId();

        em.flush();

        List<SubscriptionEntity> subscriptionEntities = findAllSubscriptions();
        assertEquals(sizeBefore + 1, subscriptionEntities.size());

        SubscriptionEntity entityById = daoUnderTest.findById(id);
        assertEquals(subscription, entityById);
    }

    @Test
    @SneakyThrows
    public void testAddAreaToSubscription(){
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        SubscriptionEntity subscription = daoUnderTest.findById(1L);
//        assertEquals(3, subscription.getAreas().size());

        AreaEntity area = new AreaEntity();
        area.setAreaType(AreaType.COUNTRY);
        area.setAreaValueType(AreaValueType.AREA_CODE);
        area.setValue("BEL");
//        subscription.addArea(area);

        em.flush();

        daoUnderTest.findById(1L);
//        assertEquals(4, subscription.getAreas().size());
    }

    @Test
    @SneakyThrows
    public void testRemoveAreaToSubscription(){
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        SubscriptionEntity subscription = daoUnderTest.findById(1L);
//        assertEquals(3, subscription.getAreas().size());

//        AreaEntity next = subscription.getAreas().iterator().next();
//        subscription.removeArea(next);

        em.flush();

        daoUnderTest.findById(1L);
//        assertEquals(2, subscription.getAreas().size());
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

    private List<SubscriptionEntity> findAllSubscriptions() {
        TypedQuery<SubscriptionEntity> query = em.createQuery("SELECT s FROM SubscriptionEntity s", SubscriptionEntity.class);
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
