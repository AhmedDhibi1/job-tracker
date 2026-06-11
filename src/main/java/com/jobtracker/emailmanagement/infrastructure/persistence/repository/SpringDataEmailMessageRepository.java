package com.jobtracker.emailmanagement.infrastructure.persistence.repository;

import com.jobtracker.emailmanagement.infrastructure.persistence.entity.EmailMessageJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataEmailMessageRepository
        extends JpaRepository<EmailMessageJpaEntity, UUID> {
    boolean existsByGmailMessageId(String gmailMessageId);
    Optional<EmailMessageJpaEntity> findByGmailMessageId(String gmailMessageId);
    List<EmailMessageJpaEntity> findByGmailThreadId(String gmailThreadId);
}