package me.sample.web.rest.validation;

import me.sample.dto.PromoDTO;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class PromoPartnersAndStoresValidator implements ConstraintValidator<ValidPromoPartnersAndStores, PromoDTO> {

    @Override
    public boolean isValid(PromoDTO dto, ConstraintValidatorContext context) {
        if ((dto.getPartners() == null || dto.getPartners().isEmpty()) &&
                (dto.getStores() == null || dto.getStores().isEmpty())) {
            return false;
        }

        return true;
    }
}
