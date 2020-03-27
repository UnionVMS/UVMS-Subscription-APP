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
import static eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity.random;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ninja_squad.dbsetup.DbSetup;
import com.ninja_squad.dbsetup.destination.DataSourceDestination;
import com.ninja_squad.dbsetup.operation.Operation;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.AreaEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.ColumnType;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.DirectionType;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.QueryParameterDto;
import eu.europa.ec.fisheries.uvms.subscription.service.mapper.CustomMapper;
import eu.europa.ec.fisheries.wsdl.subscription.module.AreaType;
import eu.europa.ec.fisheries.wsdl.subscription.module.AreaValueType;
import eu.europa.ec.fisheries.wsdl.subscription.module.MessageType;
import eu.europa.ec.fisheries.wsdl.subscription.module.SubscriptionDataQuery;
import eu.europa.ec.fisheries.wsdl.user.types.Channel;
import eu.europa.ec.fisheries.wsdl.user.types.EndPoint;
import eu.europa.ec.fisheries.wsdl.user.types.Organisation;
import eu.europa.ec.fisheries.uvms.subscription.helper.SubscriptionTestHelper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class SubscriptionDaoImplTest extends BaseSubscriptionInMemoryTest {

    private SubscriptionDaoImpl daoUnderTest = new SubscriptionDaoImpl(em);

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void prepare(){
        Operation operation = sequenceOf(
                DELETE_ALL, INSERT_SUBSCRIPTION, INSERT_CONDITION, INSERT_AREA);

        DbSetup dbSetup = new DbSetup(new DataSourceDestination(ds), operation);
        dbSetup.launch();
    }

    @ParameterizedTest
    @MethodSource("dataQuery")
    public void testListSubscriptionForModuleAuthorization(SubscriptionDataQuery dataQuery, int expected){
        Map<String, Object> map = CustomMapper.mapCriteriaToQueryParameters(dataQuery); // FIXME add startEndDate logic from rest
        map.put("strict", true);
        List<SubscriptionEntity> subscriptionEntities = daoUnderTest.listSubscriptions(map, new HashMap<ColumnType, DirectionType>(),-1, -1);
        assertEquals(expected, subscriptionEntities.size());
    }

    private static Stream<Arguments> dataQuery() {
        return Stream.of(
                Arguments.of(SubscriptionTestHelper.getSubscriptionDataQueryFaQuery_1(), 1),
                Arguments.of(SubscriptionTestHelper.getSubscriptionDataQueryFaQuery_2(), 0),
                Arguments.of(SubscriptionTestHelper.getSubscriptionDataQueryFaQuery_3(), 0)
        );
    }

    @ParameterizedTest
    @MethodSource("queryParameters")
    public void testListSubscription(QueryParameterDto queryParameters, int expected){
        @SuppressWarnings("unchecked")
        Map<String, Object> map = objectMapper.convertValue(queryParameters, Map.class);
        map.put("strict", false);
        List<SubscriptionEntity> subscriptionEntities = daoUnderTest.listSubscriptions(map, new HashMap<>(),-1, -1);
        assertEquals(expected, subscriptionEntities.size());
    }

    protected static Stream<Arguments> queryParameters(){
        return Stream.of(
                Arguments.of(QueryParameterDto.builder().channel(new Long(1)).build(), 4),
                Arguments.of(QueryParameterDto.builder().channel(new Long(1)).build(), 4),
                Arguments.of(QueryParameterDto.builder().channel(new Long(1)).build(), 4),
                Arguments.of(QueryParameterDto.builder().build(), 4),
                Arguments.of(QueryParameterDto.builder().messageType(MessageType.FLUX_FA_QUERY_MESSAGE).organisation(new Long(1)).build(), 1),
                Arguments.of(QueryParameterDto.builder().enabled(true).build(), 3),
                Arguments.of(QueryParameterDto.builder().channel(new Long(1)).organisation(new Long(1)).name("subscription4").build(), 0),
                Arguments.of(QueryParameterDto.builder().name("sub").enabled(true).build(), 2)
        );
    }

    @Test
    @SneakyThrows
    public void testCreateSubscriptionWithArea(){

        int sizeBefore = findAllSubscriptions().size();
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        SubscriptionEntity subscription = random();
        subscription.addArea(AreaEntity.random());

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
        assertEquals(3, subscription.getAreas().size());

        AreaEntity area = new AreaEntity();
        area.setAreaType(AreaType.COUNTRY);
        area.setAreaValueType(AreaValueType.AREA_CODE);
        area.setValue("BEL");
        subscription.addArea(area);

        em.flush();

        daoUnderTest.findById(1L);
        assertEquals(4, subscription.getAreas().size());
    }

    @Test
    @SneakyThrows
    public void testRemoveAreaToSubscription(){
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        SubscriptionEntity subscription = daoUnderTest.findById(1L);
        assertEquals(3, subscription.getAreas().size());

        AreaEntity next = subscription.getAreas().iterator().next();
        subscription.removeArea(next);

        em.flush();

        daoUnderTest.findById(1L);
        assertEquals(2, subscription.getAreas().size());
    }

    @Test
    @SneakyThrows
    public void testListSubscriptionForEnrichment(){
        List<SubscriptionEntity> subscriptionList = findAllSubscriptions();
        List<SubscriptionEntity> subscriptionEntityList = CustomMapper.enrichSubscriptionList(subscriptionList, fetchAllOrganisations() );

        for(SubscriptionEntity subscription: subscriptionEntityList){
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
