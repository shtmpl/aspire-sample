package me.sample.domain;

import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.Collection;

/**
 * Спецификация, инкапсулирующая настройки фильтрации
 * при выборке из базы набора сущностей {@link Campaign}
 */
public final class CampaignSpecifications {

    public static Specification<Campaign> companyAuthorityUserIdEqualTo(Long id) {
        return (Root<Campaign> root, CriteriaQuery<?> query, CriteriaBuilder builder) ->
                builder.equal(root.join("company").join("authorities").get("userId"), id);
    }

    public static Specification<Campaign> nameLike(String string) {
        return (Root<Campaign> root, CriteriaQuery<?> query, CriteriaBuilder builder) ->
                builder.like(builder.lower(root.get("name")), "%" + string.toLowerCase() + "%");
    }

    public static Specification<Campaign> stateIn(Collection<CampaignState> states) {
        return (Root<Campaign> root, CriteriaQuery<?> query, CriteriaBuilder builder) ->
                root.get("state").in(states);
    }
}
