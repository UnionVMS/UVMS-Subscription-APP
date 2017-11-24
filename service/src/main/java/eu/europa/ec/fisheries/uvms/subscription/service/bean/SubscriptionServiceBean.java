/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */

package eu.europa.ec.fisheries.uvms.subscription.service.bean;

import javax.annotation.PostConstruct;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Map;

import eu.europa.ec.fisheries.uvms.commons.service.dao.QueryParameter;
import eu.europa.ec.fisheries.uvms.commons.service.exception.ServiceException;
import eu.europa.ec.fisheries.uvms.subscription.service.dao.SubscriptionDAO;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.ec.fisheries.wsdl.subscription.module.Subscription;
import eu.europa.ec.fisheries.wsdl.subscription.module.SubscriptionListRequest;
import lombok.extern.slf4j.Slf4j;

@Stateless
@LocalBean
@Slf4j
public class SubscriptionServiceBean {

    private SubscriptionDAO subscriptionDAO;

    @PersistenceContext(unitName = "subscriptionPU")
    private EntityManager em;

    @PostConstruct
    public void init() {
        subscriptionDAO = new SubscriptionDAO(em);
    }

    public List<Subscription> listSubscriptions(SubscriptionListRequest listModuleRequest) throws ServiceException {
        Map parameters = QueryParameter.with(null, null).parameters();
        List<SubscriptionEntity> entityList =
                subscriptionDAO.findEntityByNamedQuery(SubscriptionEntity.class, null, parameters);

        return null;
    }
}
