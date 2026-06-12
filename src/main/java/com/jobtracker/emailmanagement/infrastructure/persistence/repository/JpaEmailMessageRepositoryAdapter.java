package com.jobtracker.emailmanagement.infrastructure.persistence.repository;

import com.jobtracker.emailmanagement.application.port.outbound.EmailMessageRepository;
import com.jobtracker.emailmanagement.domain.model.EmailMessage;
import com.jobtracker.emailmanagement.infrastructure.persistence.entity.EmailMessageJpaEntity;
import com.jobtracker.emailmanagement.infrastructure.persistence.mapper.EmailMessagePersistenceMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional(readOnly = true)
public class JpaEmailMessageRepositoryAdapter implements EmailMessageRepository {

    private final SpringDataEmailMessageRepository springDataRepository;
    private final EmailMessagePersistenceMapper mapper;

    public JpaEmailMessageRepositoryAdapter(SpringDataEmailMessageRepository springDataRepository,
                                            EmailMessagePersistenceMapper mapper) {
        this.springDataRepository = springDataRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public EmailMessage save(EmailMessage message) {
        EmailMessageJpaEntity entity = mapper.toEntity(message);
        EmailMessageJpaEntity saved = springDataRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<EmailMessage> findById(UUID id) {
        return springDataRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public boolean existsByGmailMessageId(String gmailMessageId) {
        return springDataRepository.existsByGmailMessageId(gmailMessageId);
    }

    @Override
    public Optional<EmailMessage> findByGmailMessageId(String gmailMessageId) {
        return springDataRepository.findByGmailMessageId(gmailMessageId).map(mapper::toDomain);
    }

    @Override
    public List<EmailMessage> findByGmailThreadId(String gmailThreadId) {
        return springDataRepository.findByGmailThreadId(gmailThreadId)
                .stream().map(mapper::toDomain).toList();
    }
}
