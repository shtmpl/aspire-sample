package me.sample.domain;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import org.hibernate.query.criteria.internal.CriteriaBuilderImpl;
import org.hibernate.query.criteria.internal.predicate.ComparisonPredicate;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import java.util.Collection;
import java.util.UUID;

/**
 * Спецификация, инкапсулирующая настройки фильтрации
 * при выборке из базы набора сущностей {@link Terminal}
 */
public final class TerminalSpecifications {

    public static Specification<Terminal> createForFilters(Collection<PropFilter> filters) {
        if (filters == null) {
            return Specifications.any();
        }

        Specification<Terminal> result = Specifications.any();
        for (PropFilter filter : filters) {
            if (Terminal.FILTER_KEY_PLATFORM.equals(filter.getName()) && filter.getValue() != null) {
                result = result.and(platformEquals(TerminalPlatform.of(String.valueOf(filter.getValue()))));
            } else if (Terminal.FILTER_KEY_CITY.equals(filter.getName()) && filter.getValue() != null) {
                result = result.and(cityLike(String.valueOf(filter.getValue())));
            } else if (Terminal.FILTER_KEY_CAMPAIGN_ID.equals(filter.getName()) && filter.getValue() != null) {
                result = result.and(existsCampaignClientForCampaignId(UUID.fromString(String.valueOf(filter.getValue()))));
            } else {
                result = result.and(propMatches(filter));
            }
        }

        return result;
    }

    public static Specification<Terminal> applicationCompanyAuthorityUserIdEqualTo(Long id) {
        return (Root<Terminal> root, CriteriaQuery<?> query, CriteriaBuilder builder) ->
                builder.equal(root.join("application").join("company").join("authorities").get("userId"), id);
    }

    public static Specification<Terminal> idIn(Collection<UUID> ids) {
        return (Root<Terminal> root, CriteriaQuery<?> query, CriteriaBuilder builder) ->
                root.get("id").in(ids);
    }

    public static Specification<Terminal> hardwareIdIn(Collection<String> hardwareIds) {
        return (Root<Terminal> root, CriteriaQuery<?> query, CriteriaBuilder builder) ->
                root.get("hardwareId").in(hardwareIds);
    }

    public static Specification<Terminal> platformEquals(TerminalPlatform platform) {
        return (Root<Terminal> root, CriteriaQuery<?> query, CriteriaBuilder builder) ->
                builder.equal(root.get("platform"), platform);
    }

    public static Specification<Terminal> pushIdIsNull() {
        return (Root<Terminal> root, CriteriaQuery<?> query, CriteriaBuilder builder) ->
                builder.isNull(root.get("pushId"));
    }

    public static Specification<Terminal> vendorIn(Collection<String> vendors) {
        return (Root<Terminal> root, CriteriaQuery<?> query, CriteriaBuilder builder) ->
                root.get("vendor").in(vendors);
    }

    public static Specification<Terminal> modelIn(Collection<String> models) {
        return (Root<Terminal> root, CriteriaQuery<?> query, CriteriaBuilder builder) ->
                root.get("model").in(models);
    }

    public static Specification<Terminal> osVersionIn(Collection<String> osVersions) {
        return (Root<Terminal> root, CriteriaQuery<?> query, CriteriaBuilder builder) ->
                root.get("osVersion").in(osVersions);
    }

    public static Specification<Terminal> ipIsNull() {
        return (Root<Terminal> root, CriteriaQuery<?> query, CriteriaBuilder builder) ->
                builder.isNull(root.get("ip"));
    }

    public static Specification<Terminal> cityIsNull() {
        return (Root<Terminal> root, CriteriaQuery<?> query, CriteriaBuilder builder) ->
                builder.isNull(root.get("city"));
    }

    public static Specification<Terminal> cityIsBlank() {
        return (Root<Terminal> root, CriteriaQuery<?> query, CriteriaBuilder builder) ->
                builder.equal(
                        builder.function("coalesce", String.class,
                                builder.function("trim", String.class, root.get("city")),
                                builder.literal("")),
                        "");
    }

    public static Specification<Terminal> cityEquals(String city) {
        return (Root<Terminal> root, CriteriaQuery<?> query, CriteriaBuilder builder) ->
                builder.equal(root.get("city"), city);
    }

    public static Specification<Terminal> cityLike(String city) {
        return (Root<Terminal> root, CriteriaQuery<?> query, CriteriaBuilder builder) ->
                builder.like(builder.lower(root.get("city")), "%" + city.toLowerCase() + "%");
    }

    public static Specification<Terminal> propMatches(PropFilter filter) {
        PropFilter.Sign op = filter.getSign();
        if (op == null) {
            return Specifications.none();
        }

        return (Root<Terminal> root, CriteriaQuery<?> query, CriteriaBuilder builder) -> {
            Expression<JsonBinaryType> lhs = builder.function("jsonb_extract_path", JsonBinaryType.class,
                    root.<String>get("props"),
                    builder.literal(filter.getName()));
            Expression<JsonBinaryType> rhs = builder.function("to_jsonb", JsonBinaryType.class,
                    builder.literal(filter.getValue()));

            return new ComparisonPredicate((CriteriaBuilderImpl) builder, Specifications.OPS.get(op), lhs, rhs);
        };
    }

    public static Specification<Terminal> propClientIdIn(Collection<String> values) {
        return (Root<Terminal> root, CriteriaQuery<?> query, CriteriaBuilder builder) ->
                builder.function("jsonb_extract_path_text", String.class, root.<String>get("props"), builder.literal(Terminal.PROP_KEY_CLIENT_ID)).in(values);
    }

    public static Specification<Terminal> existsCampaignClientForCampaignId(UUID campaignId) {
        return (Root<Terminal> root, CriteriaQuery<?> query, CriteriaBuilder builder) -> {
            Subquery<CampaignClient> subquery = query.subquery(CampaignClient.class);
            Root<CampaignClient> subqueryRoot = subquery.from(CampaignClient.class);
            subquery.select(subqueryRoot).where(
                    builder.equal(
                            subqueryRoot.get("id").get("clientId"),
                            builder.function("jsonb_extract_path_text", String.class,
                                    root.<String>get("props"),
                                    builder.literal(Terminal.PROP_KEY_CLIENT_ID))),
                    builder.equal(
                            subqueryRoot.get("id").get("campaignId"),
                            campaignId));

            return builder.exists(subquery);
        };
    }
}
