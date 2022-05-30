package me.sample.domain.geo;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import me.sample.domain.PropFilter;
import me.sample.domain.Specifications;
import me.sample.domain.Terminal;
import me.sample.domain.TerminalPlatform;
import org.hibernate.query.criteria.internal.CriteriaBuilderImpl;
import org.hibernate.query.criteria.internal.predicate.ComparisonPredicate;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Root;
import java.util.Collection;
import java.util.UUID;

/**
 * Спецификация, инкапсулирующая настройки фильтрации
 * при выборке из базы набора сущностей {@link Cluster}
 */
public final class ClusterSpecifications {

    public static Specification<Cluster> terminalApplicationCompanyAuthorityUserIdEqualTo(Long id) {
        return (Root<Cluster> root, CriteriaQuery<?> query, CriteriaBuilder builder) ->
                builder.equal(root.join("terminal").join("application").join("company").join("authorities").get("userId"), id);
    }

    public static Specification<Cluster> coordinatesWithinRadius(Double lat, Double lon, Long radius) {
        return (Root<Cluster> root, CriteriaQuery<?> query, CriteriaBuilder builder) ->
                builder.isTrue(
                        builder.function("cube_contains", Boolean.class,
                                builder.function("earth_box", Object.class,
                                        builder.function("ll_to_earth", Object.class, builder.literal(lat), builder.literal(lon)),
                                        builder.literal(radius)),
                                builder.function("ll_to_earth", Object.class, root.get("lat"), root.get("lon"))));
    }

    public static Specification<Cluster> visitCountMatches(PropFilter filter) {
        PropFilter.Sign op = filter.getSign();
        if (op == null) {
            return Specifications.none();
        }

        Integer visitCount = (Integer) filter.getValue();
        return (Root<Cluster> root, CriteriaQuery<?> query, CriteriaBuilder builder) -> {
            switch (op) {
                case LESS:
                    return builder.lessThan(root.get("visitCount"), visitCount);
                case LESS_OR_EQUAL:
                    return builder.lessThanOrEqualTo(root.get("visitCount"), visitCount);
                case EQUAL:
                    return builder.equal(root.get("visitCount"), visitCount);
                case GREATER_OR_EQUAL:
                    return builder.greaterThanOrEqualTo(root.get("visitCount"), visitCount);
                case GREATER:
                    return builder.greaterThan(root.get("visitCount"), visitCount);
                default:
                    return builder.disjunction();
            }
        };
    }


    // Relation to Terminal

    public static Specification<Cluster> createForTerminalFilters(Collection<PropFilter> filters) {
        if (filters == null) {
            return Specifications.any();
        }

        Specification<Cluster> result = Specifications.any();
        for (PropFilter filter : filters) {
            if (Terminal.FILTER_KEY_PLATFORM.equals(filter.getName()) && filter.getValue() != null) {
                result = result.and(terminalPlatformEquals(TerminalPlatform.of(String.valueOf(filter.getValue()))));
            } else if (Terminal.FILTER_KEY_CITY.equals(filter.getName()) && filter.getValue() != null) {
                result = result.and(terminalCityLike(String.valueOf(filter.getValue())));
            } else {
                result = result.and(terminalPropMatches(filter));
            }
        }

        return result;
    }

    public static Specification<Cluster> terminalIdIn(Collection<UUID> terminalIds) {
        return (Root<Cluster> root, CriteriaQuery<?> query, CriteriaBuilder builder) ->
                root.join("terminal").get("id").in(terminalIds);
    }

    public static Specification<Cluster> terminalPlatformEquals(TerminalPlatform platform) {
        return (Root<Cluster> root, CriteriaQuery<?> query, CriteriaBuilder builder) ->
                builder.equal(root.join("terminal").get("platform"), platform);
    }

    public static Specification<Cluster> terminalCityLike(String city) {
        return (Root<Cluster> root, CriteriaQuery<?> query, CriteriaBuilder builder) ->
                builder.like(builder.lower(root.join("terminal").get("city")), "%" + city.toLowerCase() + "%");
    }

    public static Specification<Cluster> terminalPropMatches(PropFilter filter) {
        PropFilter.Sign op = filter.getSign();
        if (op == null) {
            return Specifications.none();
        }

        return (Root<Cluster> root, CriteriaQuery<?> query, CriteriaBuilder builder) -> {
            Expression<JsonBinaryType> lhs = builder.function("jsonb_extract_path", JsonBinaryType.class, root.join("terminal").<String>get("props"), builder.literal(filter.getName()));
            Expression<JsonBinaryType> rhs = builder.function("to_jsonb", JsonBinaryType.class, builder.literal(filter.getValue()));

            return new ComparisonPredicate(
                    (CriteriaBuilderImpl) builder,
                    Specifications.OPS.get(op),
                    lhs,
                    rhs);
        };
    }
}
