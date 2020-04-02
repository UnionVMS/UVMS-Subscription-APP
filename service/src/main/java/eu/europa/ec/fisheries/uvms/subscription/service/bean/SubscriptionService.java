package eu.europa.ec.fisheries.uvms.subscription.service.bean;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import eu.europa.ec.fisheries.uvms.commons.rest.dto.PaginationDto;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.SubscriptionDto;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.list.SubscriptionListResponseDto;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.search.OrderByDto;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.search.QueryParameterDto;
import eu.europa.ec.fisheries.wsdl.subscription.module.SubscriptionDataQuery;
import eu.europa.ec.fisheries.wsdl.subscription.module.SubscriptionPermissionResponse;
import lombok.SneakyThrows;

public interface SubscriptionService {
	SubscriptionPermissionResponse hasActiveSubscriptions(SubscriptionDataQuery query);

	@SneakyThrows
	SubscriptionListResponseDto listSubscriptions(@Valid @NotNull QueryParameterDto parameters, @Valid @NotNull PaginationDto pagination,
												  @Valid @NotNull OrderByDto orderByDto, String scopeName, String roleName, String requester);

	@SneakyThrows
	SubscriptionDto create(@Valid @NotNull SubscriptionDto subscription, @NotNull String currentUser);

	@SneakyThrows
	SubscriptionDto update(@Valid @NotNull SubscriptionDto subscription, @NotNull String currentUser);

	@SneakyThrows
	void delete(@NotNull Long id, @NotNull String currentUser);

	SubscriptionEntity findSubscriptionByName(@NotNull String name);
}
