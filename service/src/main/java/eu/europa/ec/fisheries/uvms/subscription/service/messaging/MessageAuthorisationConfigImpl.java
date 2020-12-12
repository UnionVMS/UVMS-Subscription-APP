package eu.europa.ec.fisheries.uvms.subscription.service.messaging;

import static eu.europa.ec.fisheries.uvms.subscription.service.config.ParameterKey.AUTHZ_FA_QUERY;
import static eu.europa.ec.fisheries.uvms.subscription.service.config.ParameterKey.AUTHZ_FA_REPORT;
import static eu.europa.ec.fisheries.uvms.subscription.service.config.ParameterKey.AUTHZ_VMS_REPORT;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import eu.europa.ec.fisheries.uvms.config.event.ConfigSettingEvent;
import eu.europa.ec.fisheries.uvms.config.event.ConfigSettingEventType;
import eu.europa.ec.fisheries.uvms.config.event.ConfigSettingUpdatedEvent;
import eu.europa.ec.fisheries.uvms.config.exception.ConfigServiceException;
import eu.europa.ec.fisheries.uvms.config.service.ParameterService;
import eu.europa.ec.fisheries.uvms.subscription.service.config.ParameterKey;
import eu.europa.fisheries.uvms.subscription.model.exceptions.ApplicationException;

/**
 * Implementation of {@link MessageAuthorisationConfig} that uses Flux-FMC standard configuration mechanism.
 */
@ApplicationScoped
public class MessageAuthorisationConfigImpl implements MessageAuthorisationConfig {

	private ParameterService parameterService;

	private Boolean mustAuthoriseIncomingFaQuery;
	private Boolean mustAuthoriseIncomingFaReport;
	private Boolean mustAuthoriseIncomingVmsReport;

	/**
	 * Injection constructor.
	 *
	 * @param parameterService The parameter (configuration) service
	 */
	@Inject
	public MessageAuthorisationConfigImpl(ParameterService parameterService) {
		this.parameterService = parameterService;
	}

	/**
	 * Constructor for frameworks.
	 */
	@SuppressWarnings("unused")
	MessageAuthorisationConfigImpl() {
		// NOOP
	}

	void setConfig(@Observes @ConfigSettingUpdatedEvent ConfigSettingEvent event) {
		if (event.getType() == ConfigSettingEventType.UPDATE) {
			if (AUTHZ_FA_QUERY.getKey().equals(event.getKey())) {
				mustAuthoriseIncomingFaQuery = readParam(AUTHZ_FA_QUERY);
			} else if (AUTHZ_FA_REPORT.getKey().equals(event.getKey())) {
				mustAuthoriseIncomingFaReport = readParam(AUTHZ_FA_REPORT);
			} else if (AUTHZ_VMS_REPORT.getKey().equals(event.getKey())) {
				mustAuthoriseIncomingVmsReport = readParam(AUTHZ_VMS_REPORT);
			}
		}
	}

	@Override
	public boolean mustAuthoriseIncomingFaQuery() {
		if (mustAuthoriseIncomingFaQuery == null) {
			mustAuthoriseIncomingFaQuery = readParam(AUTHZ_FA_QUERY);
		}
		return mustAuthoriseIncomingFaQuery;
	}

	@Override
	public boolean mustAuthoriseIncomingFaReport() {
		if (mustAuthoriseIncomingFaReport == null) {
			mustAuthoriseIncomingFaReport = readParam(AUTHZ_FA_REPORT);
		}
		return mustAuthoriseIncomingFaReport;
	}

	@Override
	public boolean mustAuthoriseIncomingVmsReport() {
		if (mustAuthoriseIncomingVmsReport == null) {
			mustAuthoriseIncomingVmsReport = readParam(AUTHZ_VMS_REPORT);
		}
		return mustAuthoriseIncomingVmsReport;
	}

	private Boolean readParam(ParameterKey key) {
		try {
			return Boolean.valueOf(parameterService.getParamValueById(key.getKey()));
		} catch (ConfigServiceException e) {
			throw new ApplicationException("error retrieving parameter " + key + " from configuration", e);
		}
	}
}
