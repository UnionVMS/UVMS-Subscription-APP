package eu.europa.ec.fisheries.uvms.subsription.rest.resource

import eu.europa.ec.fisheries.uvms.subscription.service.bean.SubscriptionService
import eu.europa.ec.fisheries.uvms.subscription.service.dto.list.SubscriptionListResponseDto
import eu.europa.ec.fisheries.uvms.subscription.service.dto.search.SubscriptionListQueryImpl
import spock.lang.Specification

import javax.ws.rs.core.Response

class SubscriptionResourceSpec extends Specification {

    private SubscriptionResource subscriptionResource
    private SubscriptionService subscriptionService

    void setup() {
        subscriptionService = Mock(SubscriptionService)
        subscriptionResource = new SubscriptionResource()
        subscriptionResource.service = subscriptionService
    }

    def "Test available rest service"() {
        given: "a subscription name and an id"
            String name = "test";
            Long id = 1234L


        when: "when available resource method is invoked"
            Response response = subscriptionResource.available(name, id)

        then: "the result is correct"
            1 * subscriptionService.checkNameAvailability(name, id) >> true

        and:
            assert response.status == 200
            assert response.entity.data == true
    }

    def "Test list subscriptions rest service"() {
        given: "subscription query params, a scope and a role"
            String scopeName = "testScope";
            String roleName = "testRole"
            SubscriptionListQueryImpl queryParams = new SubscriptionListQueryImpl()

        when: "when available resource method is invoked"
            Response response = subscriptionResource.listSubscriptions(queryParams, scopeName, roleName)

        then: "the result is correct"
            1 * subscriptionService.listSubscriptions(queryParams, scopeName, roleName) >> new SubscriptionListResponseDto()

        and:
            assert response.status == 200
            assert response.entity.data != null
    }

}