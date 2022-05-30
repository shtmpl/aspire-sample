package me.sample.web.rest.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({TYPE, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = PromoPartnersAndStoresValidator.class)
@Documented
public @interface ValidPromoPartnersAndStores {

    String message() default "Promo.partners and Promo.stores should not be null or empty at the same time";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
