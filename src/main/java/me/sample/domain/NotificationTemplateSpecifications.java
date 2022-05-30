package me.sample.domain;

import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

/**
 * Спецификация, инкапсулирующая настройки фильтрации
 * при выборке из базы набора сущностей {@link NotificationTemplate}
 */
public final class NotificationTemplateSpecifications {

    public static Specification<NotificationTemplate> companyAuthorityUserIdEqualTo(Long id) {
        return (Root<NotificationTemplate> root, CriteriaQuery<?> query, CriteriaBuilder builder) ->
                builder.equal(root.join("company").join("authorities").get("userId"), id);
    }

    public static Specification<NotificationTemplate> nameLike(String string) {
        return (Root<NotificationTemplate> root, CriteriaQuery<?> query, CriteriaBuilder builder) ->
                builder.like(builder.lower(root.get("name")), "%" + string.toLowerCase() + "%");
    }

    public static Specification<NotificationTemplate> subjectLike(String string) {
        return (Root<NotificationTemplate> root, CriteriaQuery<?> query, CriteriaBuilder builder) ->
                builder.like(builder.lower(root.get("subject")), "%" + string.toLowerCase() + "%");
    }
}
