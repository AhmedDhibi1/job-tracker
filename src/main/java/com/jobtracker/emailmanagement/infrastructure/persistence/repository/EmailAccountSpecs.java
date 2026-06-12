package com.jobtracker.emailmanagement.infrastructure.persistence.repository;

import com.jobtracker.emailmanagement.infrastructure.persistence.entity.EmailAccountJpaEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public final class EmailAccountSpecs {

    private EmailAccountSpecs() {}

    public static Specification<EmailAccountJpaEntity> isActive() {
        return (root, query, cb) -> cb.isTrue(root.get("active"));
    }

    public static Specification<EmailAccountJpaEntity> isPrimary() {
        return (root, query, cb) -> cb.isTrue(root.get("isPrimary"));
    }

    public static Specification<EmailAccountJpaEntity> watchExpired(Instant now) {
        return (root, query, cb) -> cb.lessThan(root.get("watchExpiration"), now);
    }

    public static Specification<EmailAccountJpaEntity> pushEnabled() {
        return (root, query, cb) -> cb.isTrue(root.get("pushEnabled"));
    }

    public static Specification<EmailAccountJpaEntity> hasEmailAddress(String email) {
        return (root, query, cb) -> cb.equal(root.get("emailAddress"), email);
    }

    public static Specification<EmailAccountJpaEntity> activeAndWatchExpired(Instant now) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isTrue(root.get("active")));
            predicates.add(cb.lessThan(root.get("watchExpiration"), now));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
