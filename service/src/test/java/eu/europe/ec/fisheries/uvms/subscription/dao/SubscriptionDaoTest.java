/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */

package eu.europe.ec.fisheries.uvms.subscription.dao;

import static com.ninja_squad.dbsetup.Operations.sequenceOf;
import static junitparams.JUnitParamsRunner.$;
import static org.jsoup.helper.Validate.fail;
import static org.junit.Assert.assertEquals;

import javax.persistence.EntityTransaction;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ninja_squad.dbsetup.DbSetup;
import com.ninja_squad.dbsetup.destination.DataSourceDestination;
import com.ninja_squad.dbsetup.operation.Operation;
import eu.europa.ec.fisheries.uvms.subscription.service.dao.SubscriptionDao;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.AreaEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.MessageType;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.QueryParameterDto;
import eu.europa.ec.fisheries.wsdl.subscription.module.AreaType;
import eu.europa.ec.fisheries.wsdl.subscription.module.AreaValueType;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
public class SubscriptionDaoTest extends BaseSubscriptionDaoTest {

    private SubscriptionDao daoUnderTest = new SubscriptionDao(em);

    private ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void prepare(){
        Operation operation = sequenceOf(
                DELETE_ALL, INSERT_SUBSCRIPTION, INSERT_CONDITION, INSERT_AREA);

        DbSetup dbSetup = new DbSetup(new DataSourceDestination(ds), operation);
        dbSetup.launch();
    }

    @Test
    @Parameters(method = "queryParameters")
    public void testListSubscription(QueryParameterDto queryParameters, int expected){

        Map<String, Object> map = objectMapper.convertValue(queryParameters, Map.class);
        //map.put("startDate", queryParameters.getStartDate());
        //map.put("endDate", queryParameters.getEndDate());

        List<SubscriptionEntity> subscriptionEntities = daoUnderTest.listSubscriptions(map,-1, -1);
        assertEquals(expected, subscriptionEntities.size());
    }

    @Test
    @SneakyThrows
    public void testCreateSubscriptionWithArea(){

        int sizeBefore = daoUnderTest.findAllEntity(SubscriptionEntity.class).size();
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        SubscriptionEntity subscription = SubscriptionEntity.random();
        subscription.addArea(AreaEntity.random());

        Long id = daoUnderTest.createEntity(subscription).getId();

        em.flush();

        List<SubscriptionEntity> subscriptionEntities = daoUnderTest.findAllEntity(SubscriptionEntity.class);
        assertEquals(sizeBefore + 1, subscriptionEntities.size());

        SubscriptionEntity entityById = daoUnderTest.findEntityById(SubscriptionEntity.class, id);

        assertEquals(subscription, entityById);
    }

    @Test
    @SneakyThrows
    public void testAddAreaToSubscription(){
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        SubscriptionEntity subscription = daoUnderTest.findEntityById(SubscriptionEntity.class, 1L);
        assertEquals(3, subscription.getAreas().size());

        AreaEntity area = new AreaEntity();
        area.setAreaType(AreaType.COUNTRY);
        area.setAreaValueType(AreaValueType.AREA_CODE);
        area.setValue("BEL");
        subscription.addArea(area);

        em.flush();

        daoUnderTest.findEntityById(SubscriptionEntity.class, 1L);
        assertEquals(4, subscription.getAreas().size());

    }

    @Test
    @SneakyThrows
    public void testRemoveAreaToSubscription(){
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        SubscriptionEntity subscription = daoUnderTest.findEntityById(SubscriptionEntity.class, 1L);
        assertEquals(3, subscription.getAreas().size());

        AreaEntity next = subscription.getAreas().iterator().next();
        subscription.removeArea(next);

        em.flush();

        daoUnderTest.findEntityById(SubscriptionEntity.class, 1L);
        assertEquals(2, subscription.getAreas().size());

    }

    @Test
    @SneakyThrows
    @Parameters(method = "subscriptionCreateWithConstraintViolationExceptionParameters")
    public void testCreateWithMissingMandatoryValuesShouldThrowException(SubscriptionEntity subscription){

        try {
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            daoUnderTest.createEntity(subscription);
            em.flush();
            fail("should throw javax.validation.ConstraintViolationException");
        } catch (javax.validation.ConstraintViolationException e){
            e.printStackTrace();
        }
    }

    protected Object[] subscriptionCreateWithConstraintViolationExceptionParameters(){
        return $(
                $(SubscriptionEntity.builder().channel("channel100").build(),
                        SubscriptionEntity.builder().name("name1").channel("channel4").build(),
                        SubscriptionEntity.builder().name("name1").channel("channel4").endPoint("endpoint2").build(),
                        SubscriptionEntity.builder().name("name1").channel("channel4").endPoint("endpoint2").organisation("org4").build(),
                        SubscriptionEntity.builder().name("name1").channel("channel4").endPoint("endpoint2").organisation("org4").enabled(true).build(),
                        SubscriptionEntity.builder().name("name1").channel("channel4").endPoint("endpoint2").organisation("org4").enabled(true).messageType(MessageType.FLUXFAReportMessage).build())
        );
    }
    protected Object[] queryParameters(){
        return $(
                $(QueryParameterDto.builder().channel("channel1").build(), 0)//,
               // $(QueryParameterDto.builder().channel("channel2").build(), 2),
               // $(QueryParameterDto.builder().channel("channel3").build(), 1),
               // $(QueryParameterDto.builder().build(), 4),
               // $(QueryParameterDto.builder().enabled(true).build(), 3),
               // $(QueryParameterDto.builder().channel("channel4").organisation("org1").name("subscription4").build(), 1),
               // $(QueryParameterDto.builder().name("sub").enabled(true).build(), 2)
        );
    }
}
