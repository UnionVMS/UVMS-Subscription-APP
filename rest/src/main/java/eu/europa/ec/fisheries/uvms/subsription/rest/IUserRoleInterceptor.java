package eu.europa.ec.fisheries.uvms.subsription.rest;

import eu.europa.ec.fisheries.wsdl.subscription.module.SubscriptionFeaturesEnum;

import javax.enterprise.util.Nonbinding;
import javax.interceptor.InterceptorBinding;
import java.lang.annotation.*;

@Inherited
@InterceptorBinding
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface IUserRoleInterceptor {

    @Nonbinding SubscriptionFeaturesEnum[] requiredUserRole() default SubscriptionFeaturesEnum.SUBSCRIPTIONS_ALLOWED;
}