/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */

package eu.europa.ec.fisheries.uvms.subscription.service.dao;

import javax.persistence.EntityManager;
import java.util.List;

import eu.europa.ec.fisheries.uvms.commons.service.dao.AbstractDAO;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.ec.fisheries.wsdl.subscription.module.SubscriptionDataQuery;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SubscriptionDao extends AbstractDAO<SubscriptionEntity> {

    private EntityManager em;

    public SubscriptionDao(EntityManager em) {
        this.em = em;
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    @SuppressWarnings("unchecked")
    @SneakyThrows
    public List<SubscriptionEntity> listSubscriptions(SubscriptionDataQuery query) {

        /*
        Map parameters = QueryParameter
                .with(ORGANISATION, query != null ? query.getOrganisation() : null)
                .and(CHANNEL, query != null ? query.getChannel() : null)
                .and(END_POINT, query != null ? query.getEndPoint() : null)
                .and(MESSAGE_TYPE, query != null ? query.getMessageType() : null)
                .and(DESCRIPTION, query != null ? query.getDescription() : null)
                .and(NAME, query != null ? query.getName() : null)
                .and(ACTIVE, query != null ? query.isEnabled() : null)
                .and(CFR_LIST_HAS_ITEMS, 0)
                .and(CFR_VALUES, new ArrayList<>())
                .and(SYSTEM_AREA_LIST_HAS_ITEMS, 0)
                .parameters();
*/

        return null;
    }

}
