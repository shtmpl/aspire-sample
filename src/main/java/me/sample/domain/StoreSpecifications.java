package me.sample.domain;

import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.Collection;
import java.util.UUID;

/**
 * Спецификация, инкапсулирующая настройки фильтрации
 * при выборке из базы набора сущностей {@link Store}
 */
public final class StoreSpecifications {

    public static Specification<Store> partnerCompanyAuthorityUserIdEqualTo(Long id) {
        return (Root<Store> root, CriteriaQuery<?> query, CriteriaBuilder builder) ->
                builder.equal(root.join("partner").join("company").join("authorities").get("userId"), id);
    }

    public static Specification<Store> idEqualTo(UUID id) {
        return (Root<Store> root, CriteriaQuery<?> query, CriteriaBuilder builder) ->
                builder.equal(root.get("id"), id);
    }

    public static Specification<Store> partnerIdIn(Collection<UUID> ids) {
        return (Root<Store> root, CriteriaQuery<?> query, CriteriaBuilder builder) ->
                root.join("partner").get("id").in(ids);
    }

    public static Specification<Store> partnerNameLike(String name) {
        return (Root<Store> root, CriteriaQuery<?> query, CriteriaBuilder builder) ->
                builder.like(builder.lower(root.join("partner").get("name")), "%" + name.toLowerCase() + "%");
    }

    public static Specification<Store> sourceIn(Collection<Source> sources) {
        return (Root<Store> root, CriteriaQuery<?> query, CriteriaBuilder builder) ->
                root.get("source").in(sources);
    }

    public static Specification<Store> stateIn(Collection<StoreState> states) {
        return (Root<Store> root, CriteriaQuery<?> query, CriteriaBuilder builder) ->
                root.get("state").in(states);
    }

    public static Specification<Store> nameLike(String name) {
        return (Root<Store> root, CriteriaQuery<?> query, CriteriaBuilder builder) ->
                builder.like(builder.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

    public static Specification<Store> latIsNull() {
        return (Root<Store> root, CriteriaQuery<?> query, CriteriaBuilder builder) ->
                builder.isNull(root.get("lat"));
    }

    public static Specification<Store> lonIsNull() {
        return (Root<Store> root, CriteriaQuery<?> query, CriteriaBuilder builder) ->
                builder.isNull(root.get("lon"));
    }

    public static Specification<Store> cityIsNull() {
        return (Root<Store> root, CriteriaQuery<?> query, CriteriaBuilder builder) ->
                builder.isNull(root.get("city"));
    }

    public static Specification<Store> cityIsBlank() {
        return (Root<Store> root, CriteriaQuery<?> query, CriteriaBuilder builder) ->
                builder.equal(
                        builder.function("coalesce", String.class,
                                builder.function("trim", String.class, root.get("city")),
                                builder.literal("")),
                        "");
    }

    public static Specification<Store> cityLike(String city) {
        return (Root<Store> root, CriteriaQuery<?> query, CriteriaBuilder builder) ->
                builder.like(builder.lower(root.get("city")), "%" + city.toLowerCase() + "%");
    }

    public static Specification<Store> cityIn(Collection<String> cities) {
        return (Root<Store> root, CriteriaQuery<?> query, CriteriaBuilder builder) ->
                root.get("city").in(cities);
    }
}
