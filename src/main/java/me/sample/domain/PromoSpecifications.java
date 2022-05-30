package me.sample.domain;

import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;
import java.util.Collection;

/**
 * Спецификация, инкапсулирующая настройки фильтрации
 * при выборке из базы набора сущностей {@link Promo}
 */
public final class PromoSpecifications {

    public static Specification<Promo> partnerCompanyAuthorityUserIdEqualTo(Long id) {
        return (Root<Promo> root, CriteriaQuery<?> query, CriteriaBuilder builder) -> {
            query.distinct(true);

            return builder.equal(root
                    .join("partners", JoinType.LEFT)
                    .join("company", JoinType.LEFT)
                    .join("authorities", JoinType.LEFT)
                    .get("userId"), id);
        };
    }

    public static Specification<Promo> storePartnerCompanyAuthorityUserIdEqualTo(Long id) {
        return (Root<Promo> root, CriteriaQuery<?> query, CriteriaBuilder builder) -> {
            query.distinct(true);

            return builder.equal(root
                    .join("stores", JoinType.LEFT)
                    .join("partner", JoinType.LEFT)
                    .join("company", JoinType.LEFT)
                    .join("authorities", JoinType.LEFT)
                    .get("userId"), id);
        };
    }

    public static Specification<Promo> sourceIn(Collection<Source> sources) {
        return (Root<Promo> root, CriteriaQuery<?> query, CriteriaBuilder builder) ->
                root.get("source").in(sources);
    }

    public static Specification<Promo> stateIn(Collection<PromoState> states) {
        return (Root<Promo> root, CriteriaQuery<?> query, CriteriaBuilder builder) ->
                root.get("state").in(states);
    }

    public static Specification<Promo> nameLike(String name) {
        return (Root<Promo> root, CriteriaQuery<?> query, CriteriaBuilder builder) ->
                builder.like(builder.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

    public static Specification<Promo> descriptionLike(String description) {
        return (Root<Promo> root, CriteriaQuery<?> query, CriteriaBuilder builder) ->
                builder.like(builder.lower(root.get("description")), "%" + description.toLowerCase() + "%");
    }
}
