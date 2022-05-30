package me.sample.domain;

import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

/**
 * Спецификация, инкапсулирующая настройки фильтрации
 * при выборке из базы набора сущностей {@link Distribution}
 */
public final class DistributionSpecifications {

    public static Specification<Distribution> applicationCompanyAuthorityUserIdEqualTo(Long id) {
        return (Root<Distribution> root, CriteriaQuery<?> query, CriteriaBuilder builder) ->
                builder.equal(root.join("application").join("company").join("authorities").get("userId"), id);
    }
}
