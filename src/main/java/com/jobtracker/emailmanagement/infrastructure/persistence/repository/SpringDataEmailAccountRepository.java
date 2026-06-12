package com.jobtracker.emailmanagement.infrastructure.persistence.repository;

import com.jobtracker.emailmanagement.infrastructure.persistence.entity.EmailAccountJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataEmailAccountRepository
        extends JpaRepository<EmailAccountJpaEntity, UUID>,
                JpaSpecificationExecutor<EmailAccountJpaEntity> {
    Optional<EmailAccountJpaEntity> findByEmailAddress(String emailAddress);
    boolean existsByEmailAddress(String emailAddress);
    List<EmailAccountJpaEntity> findAllByActiveTrue();
    boolean existsByIsPrimaryTrue();
}
