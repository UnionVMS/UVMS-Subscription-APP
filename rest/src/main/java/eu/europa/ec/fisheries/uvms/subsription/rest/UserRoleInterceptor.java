package eu.europa.ec.fisheries.uvms.subsription.rest;

import eu.europa.ec.fisheries.uvms.commons.rest.constants.ErrorCodes;
import eu.europa.ec.fisheries.uvms.commons.service.exception.ServiceException;
import eu.europa.ec.fisheries.wsdl.subscription.module.SubscriptionFeaturesEnum;

import javax.annotation.Priority;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import java.io.Serializable;

@IUserRoleInterceptor
@Interceptor
@Priority(Interceptor.Priority.APPLICATION)
public class UserRoleInterceptor implements Serializable {

    @Context
    private transient HttpServletRequest request;

    @AroundInvoke
    public Object interceptRequest(final InvocationContext ic) throws Exception {
        IUserRoleInterceptor iUserRoleInterceptor = ic.getMethod().getAnnotation(IUserRoleInterceptor.class);
        SubscriptionFeaturesEnum[] features = iUserRoleInterceptor.requiredUserRole(); // Get User role defined in the Rest service
        Object[] parameters = ic.getParameters(); // Request parameters
        HttpServletRequest req = getHttpServletRequest(parameters);
        boolean isUserAuthorized = false;
        for (SubscriptionFeaturesEnum subscriptionFeatureEnum : features) {
            isUserAuthorized = req.isUserInRole(subscriptionFeatureEnum.value());
        }
        if (!isUserAuthorized) {
            throw new ServiceException(ErrorCodes.NOT_AUTHORIZED);
        }
        return ic.proceed();
    }

    private HttpServletRequest getHttpServletRequest(Object[] parameters) throws ServiceException {
        for (Object object : parameters) {
            if (object instanceof HttpServletRequest) {
                return (HttpServletRequest) object;
            }
        }
        throw new ServiceException("REST_SERVICE_REQUEST_PARAM_NOT_FOUND");
    }
}