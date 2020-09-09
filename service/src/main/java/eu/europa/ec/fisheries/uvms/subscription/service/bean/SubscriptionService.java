/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.service.bean;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import eu.europa.ec.fisheries.uvms.subscription.service.domain.EmailBodyEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.SubscriptionListQuery;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.SubscriptionDto;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.list.SubscriptionListResponseDto;
import eu.europa.ec.fisheries.wsdl.subscription.module.SubscriptionDataQuery;
import eu.europa.ec.fisheries.wsdl.subscription.module.SubscriptionPermissionResponse;

public interface SubscriptionService {
	SubscriptionPermissionResponse hasActiveSubscriptions(SubscriptionDataQuery query);

	SubscriptionListResponseDto listSubscriptions(@Valid @NotNull SubscriptionListQuery queryParams, String scopeName, String roleName);

	SubscriptionDto findById(@NotNull Long id);

	SubscriptionDto create(@Valid @NotNull SubscriptionDto subscription);

	SubscriptionDto update(@Valid @NotNull SubscriptionDto subscription);

	void delete(@NotNull Long id);

	EmailBodyEntity createEmailBody(SubscriptionEntity subscription, String body);

	EmailBodyEntity updateEmailBody(SubscriptionEntity subscription, String body);

	Boolean checkNameAvailability(@NotNull String name, Long id);

    SubscriptionDto prepareManualRequest(@NotNull SubscriptionDto subscriptionDto);

    SubscriptionDto createManual(@Valid @NotNull SubscriptionDto subscription);
    
    void setSubscriptionActive(@NotNull Long id, Boolean active);
}
