package com.jobtracker.emailmanagement.infrastructure.outbox;

import com.jobtracker.emailmanagement.application.port.outbound.OutboxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OutboxPoller {

    private static final Logger log = LoggerFactory.getLogger(OutboxPoller.class);

    private static final int BATCH_SIZE = 100;

    private final OutboxRepository outboxRepository;

    public OutboxPoller(OutboxRepository outboxRepository) {
        this.outboxRepository = outboxRepository;
    }

    @Scheduled(fixedDelay = 5000)
    public void publishOutboxEvents() {
        var events = outboxRepository.findUnpublished(BATCH_SIZE);
        for (var event : events) {
            try {
                outboxRepository.markPublished(event.getId());
            } catch (Exception e) {
                log.error("Failed to mark outbox event {} as published", event.getId(), e);
            }
        }
        if (!events.isEmpty()) {
            log.debug("Processed {} outbox events", events.size());
        }
    }
}
