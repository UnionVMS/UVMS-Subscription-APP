/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.service.dao;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

import eu.europa.ec.fisheries.uvms.subscription.service.domain.EmailBodyEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.SubscriptionListQuery;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.SubscriptionSearchCriteria;

/**
 * DAO for subscriptions.
 */
public interface SubscriptionDao {
	List<SubscriptionEntity> listSubscriptions(@Valid @NotNull SubscriptionListQuery subscriptionListParams);

	/**
	 * Find subscriptions without paging, not for use by the UI.
	 *
	 * @param criteria The criteria
	 * @return The subscriptions found
	 */
	List<SubscriptionEntity> listSubscriptions(@Valid @NotNull SubscriptionSearchCriteria criteria);

	Long count(@Valid @NotNull SubscriptionSearchCriteria criteria);

	SubscriptionEntity findSubscriptionByName(@NotNull String name);

	SubscriptionEntity createEntity(SubscriptionEntity entity);

	SubscriptionEntity findById(Long id);

	SubscriptionEntity update(SubscriptionEntity entity);

	EmailBodyEntity findEmailBodyEntity(Long id);

	EmailBodyEntity createEmailBodyEntity(EmailBodyEntity entity);

	EmailBodyEntity updateEmailBodyEntity(EmailBodyEntity entity);

	void updateEmailConfigurationPassword(Long id, String password);

	String getEmailConfigurationPassword(Long id);

	void delete(Long id);
}
