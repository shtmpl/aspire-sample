package me.sample.domain;

import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.UUID;

/**
 * Спецификация, инкапсулирующая настройки фильтрации
 * при выборке из базы набора сущностей {@link Category}
 */
public final class CategorySpecifications {

    public static Specification<Category> companyAuthorityUserIdEqualTo(Long id) {
        return (Root<Category> root, CriteriaQuery<?> query, CriteriaBuilder builder) ->
                builder.equal(root.join("company").join("authorities").get("userId"), id);
    }

    public static Specification<Category> idEqualTo(UUID id) {
        return (Root<Category> root, CriteriaQuery<?> query, CriteriaBuilder builder) ->
                builder.equal(root.get("id"), id);
    }
}
