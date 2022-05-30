package me.sample.domain;

import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.Collection;
import java.util.UUID;

/**
 * Спецификация, инкапсулирующая настройки фильтрации
 * при выборке из базы набора сущностей {@link Notification}
 */
public final class NotificationSpecifications {

    public static Specification<Notification> terminalApplicationCompanyAuthorityUserIdEqualTo(Long id) {
        return (Root<Notification> root, CriteriaQuery<?> query, CriteriaBuilder builder) ->
                builder.equal(root.join("terminal").join("application").join("company").join("authorities").get("userId"), id);
    }

    public static Specification<Notification> terminalIdIn(Collection<UUID> ids) {
        return (Root<Notification> root, CriteriaQuery<?> query, CriteriaBuilder builder) ->
                root.join("terminal").get("id").in(ids);
    }
}
