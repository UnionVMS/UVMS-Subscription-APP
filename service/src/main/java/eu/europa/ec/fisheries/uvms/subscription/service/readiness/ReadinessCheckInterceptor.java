package eu.europa.ec.fisheries.uvms.subscription.service.readiness;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import eu.europa.ec.fisheries.uvms.commons.service.exception.ServiceException;
import eu.europa.ec.fisheries.uvms.init.ReadinessService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Interceptor
@ReadinessCheck
@Priority(Interceptor.Priority.APPLICATION - 1)
public class ReadinessCheckInterceptor {

    @Inject
    private ReadinessService readinessService;

    @AroundInvoke
    Object interceptRequest(final InvocationContext ic) throws Exception {
        if (readinessService.isReady()) {
            return ic.proceed();
        }
        throw new ServiceException("Service is not fully initialized to serve requests");
    }
}
