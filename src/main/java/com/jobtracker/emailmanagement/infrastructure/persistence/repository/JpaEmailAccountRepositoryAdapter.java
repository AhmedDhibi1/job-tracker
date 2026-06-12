package com.jobtracker.emailmanagement.infrastructure.persistence.repository;

import com.jobtracker.emailmanagement.application.port.outbound.EmailAccountRepository;
import com.jobtracker.emailmanagement.domain.model.EmailAccount;
import com.jobtracker.emailmanagement.infrastructure.persistence.entity.EmailAccountJpaEntity;
import com.jobtracker.emailmanagement.infrastructure.persistence.mapper.EmailAccountPersistenceMapper;
import com.jobtracker.shared.domain.valueobject.EmailAddress;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional(readOnly = true)
public class JpaEmailAccountRepositoryAdapter implements EmailAccountRepository {

    private final SpringDataEmailAccountRepository springDataRepository;
    private final EmailAccountPersistenceMapper mapper;

    public JpaEmailAccountRepositoryAdapter(SpringDataEmailAccountRepository springDataRepository,
                                            EmailAccountPersistenceMapper mapper) {
        this.springDataRepository = springDataRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<EmailAccount> findById(UUID id) {
        return springDataRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<EmailAccount> findAll() {
        return springDataRepository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<EmailAccount> findByEmailAddress(EmailAddress emailAddress) {
        return springDataRepository.findByEmailAddress(emailAddress.value()).map(mapper::toDomain);
    }

    @Override
    public List<EmailAccount> findAllActive() {
        return springDataRepository.findAllByActiveTrue().stream().map(mapper::toDomain).toList();
    }

    @Override
    @Transactional
    public EmailAccount save(EmailAccount account) {
        EmailAccountJpaEntity entity = mapper.toEntity(account);
        EmailAccountJpaEntity saved = springDataRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public boolean existsByEmailAddress(EmailAddress emailAddress) {
        return springDataRepository.existsByEmailAddress(emailAddress.value());
    }
}
