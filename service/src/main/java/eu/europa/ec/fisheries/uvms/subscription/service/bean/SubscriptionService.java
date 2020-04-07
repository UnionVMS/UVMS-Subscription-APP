package eu.europa.ec.fisheries.uvms.subscription.service.bean;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.SubscriptionListQuery;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.SubscriptionDto;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.list.SubscriptionListResponseDto;
import eu.europa.ec.fisheries.wsdl.subscription.module.SubscriptionDataQuery;
import eu.europa.ec.fisheries.wsdl.subscription.module.SubscriptionPermissionResponse;
import lombok.SneakyThrows;

public interface SubscriptionService {
	SubscriptionPermissionResponse hasActiveSubscriptions(SubscriptionDataQuery query);

	@SneakyThrows
	SubscriptionListResponseDto listSubscriptions(@Valid @NotNull SubscriptionListQuery queryParams, String scopeName, String roleName, String requester);

	@SneakyThrows
	SubscriptionDto get(@NotNull Long id);

	@SneakyThrows
	SubscriptionDto create(@Valid @NotNull SubscriptionDto subscription, @NotNull String currentUser);

	@SneakyThrows
	SubscriptionDto update(@Valid @NotNull SubscriptionDto subscription, @NotNull String currentUser);

	@SneakyThrows
	void delete(@NotNull Long id, @NotNull String currentUser);

	Boolean valueExists(@NotNull String name);
}
