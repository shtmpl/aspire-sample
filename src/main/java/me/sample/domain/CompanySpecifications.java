package me.sample.domain;

import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

/**
 * Спецификация, инкапсулирующая настройки фильтрации
 * при выборке из базы набора сущностей {@link Company}
 */
public final class CompanySpecifications {

    public static Specification<Company> authorityUserIdEqualTo(Long id) {
        return (Root<Company> root, CriteriaQuery<?> query, CriteriaBuilder builder) ->
                builder.equal(root.join("authorities").get("userId"), id);
    }
}
