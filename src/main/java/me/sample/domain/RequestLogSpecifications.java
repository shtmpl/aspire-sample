package me.sample.domain;

import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Спецификация, инкапсулирующая настройки фильтрации
 * при выборке из базы набора сущностей {@link RequestLog}
 */
public final class RequestLogSpecifications {

    public static Specification<RequestLog> terminalApplicationCompanyAuthorityUserIdEqualTo(Long id) {
        return (Root<RequestLog> root, CriteriaQuery<?> query, CriteriaBuilder builder) ->
                builder.equal(root.join("terminal").join("application").join("company").join("authorities").get("userId"), id);
    }

    public static Specification<RequestLog> terminalIdEqualTo(UUID id) {
        return (Root<RequestLog> root, CriteriaQuery<?> query, CriteriaBuilder builder) ->
                builder.equal(root.join("terminal").get("id"), id);
    }

    public static Specification<RequestLog> createdAtAfter(LocalDateTime dateTime) {
        return (Root<RequestLog> root, CriteriaQuery<?> query, CriteriaBuilder builder) ->
                builder.greaterThanOrEqualTo(root.get("createdDate"), dateTime);
    }

    public static Specification<RequestLog> createdAtBefore(LocalDateTime dateTime) {
        return (Root<RequestLog> root, CriteriaQuery<?> query, CriteriaBuilder builder) ->
                builder.lessThanOrEqualTo(root.get("createdDate"), dateTime);
    }

    public static Specification<RequestLog> requestEqualTo(String string) {
        return (Root<RequestLog> root, CriteriaQuery<?> query, CriteriaBuilder builder) ->
                builder.equal(root.get("request"), string);
    }
}
