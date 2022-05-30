package me.sample.domain;

import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

/**
 * Спецификация, инкапсулирующая настройки фильтрации
 * при выборке из базы набора сущностей {@link Application}
 */
public final class ApplicationSpecifications {

    public static Specification<Application> companyAuthorityUserIdEqualTo(Long id) {
        return (Root<Application> root, CriteriaQuery<?> query, CriteriaBuilder builder) ->
                builder.equal(root.join("company").join("authorities").get("userId"), id);
    }

    public static Specification<Application> companyAuthorityPermissionEqualTo(Permission permission) {
        return (Root<Application> root, CriteriaQuery<?> query, CriteriaBuilder builder) ->
                builder.equal(root.join("company").join("authorities").get("permission"), permission);
    }

    public static Specification<Application> nameLike(String string) {
        return (Root<Application> root, CriteriaQuery<?> query, CriteriaBuilder builder) ->
                builder.like(builder.lower(root.get("name")), "%" + string.toLowerCase() + "%");
    }

    public static Specification<Application> apiKeyLike(String string) {
        return (Root<Application> root, CriteriaQuery<?> query, CriteriaBuilder builder) ->
                builder.like(builder.lower(root.get("apiKey")), "%" + string.toLowerCase() + "%");
    }
}
