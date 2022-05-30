package me.sample.domain;

import org.hibernate.query.criteria.internal.predicate.ComparisonPredicate;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Спецификация, инкапсулирующая настройки фильтрации при выборке из базы набора сущностей
 */
public final class Specifications {

    public static final Map<PropFilter.Sign, ComparisonPredicate.ComparisonOperator> OPS;

    static {
        Map<PropFilter.Sign, ComparisonPredicate.ComparisonOperator> ops = new LinkedHashMap<>();
        ops.put(PropFilter.Sign.LESS, ComparisonPredicate.ComparisonOperator.LESS_THAN);
        ops.put(PropFilter.Sign.LESS_OR_EQUAL, ComparisonPredicate.ComparisonOperator.LESS_THAN_OR_EQUAL);
        ops.put(PropFilter.Sign.EQUAL, ComparisonPredicate.ComparisonOperator.EQUAL);
        ops.put(PropFilter.Sign.GREATER_OR_EQUAL, ComparisonPredicate.ComparisonOperator.GREATER_THAN_OR_EQUAL);
        ops.put(PropFilter.Sign.GREATER, ComparisonPredicate.ComparisonOperator.GREATER_THAN);

        OPS = Collections.unmodifiableMap(ops);
    }

    public static <X> Specification<X> any() {
        return (Root<X> root, CriteriaQuery<?> query, CriteriaBuilder builder) ->
                builder.conjunction();
    }

    public static <X> Specification<X> none() {
        return (Root<X> root, CriteriaQuery<?> query, CriteriaBuilder builder) ->
                builder.disjunction();
    }
}
