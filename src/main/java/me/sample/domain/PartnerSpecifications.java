package me.sample.domain;

import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.Collection;
import java.util.UUID;

/**
 * Спецификация, инкапсулирующая настройки фильтрации
 * при выборке из базы набора сущностей {@link Partner}
 */
public final class PartnerSpecifications {

    public static Specification<Partner> companyAuthorityUserIdEqualTo(Long id) {
        return (Root<Partner> root, CriteriaQuery<?> query, CriteriaBuilder builder) ->
                builder.equal(root.join("company").join("authorities").get("userId"), id);
    }

    public static Specification<Partner> idIn(Collection<UUID> ids) {
        return (Root<Partner> root, CriteriaQuery<?> query, CriteriaBuilder builder) ->
                root.get("id").in(ids);
    }

    public static Specification<Partner> sourceIn(Collection<Source> sources) {
        return (Root<Partner> root, CriteriaQuery<?> query, CriteriaBuilder builder) ->
                root.get("source").in(sources);
    }

    public static Specification<Partner> stateIn(Collection<PartnerState> states) {
        return (Root<Partner> root, CriteriaQuery<?> query, CriteriaBuilder builder) ->
                root.get("state").in(states);
    }

    public static Specification<Partner> nameLike(String name) {
        return (Root<Partner> root, CriteriaQuery<?> query, CriteriaBuilder builder) ->
                builder.like(builder.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }
}
