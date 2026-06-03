package com.jobtracker.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@SpringBootTest(classes = AsyncConfig.class)
class AsyncConfigIntegrationTest {
    @Autowired
    @Qualifier("projectionExecutor")
    private TaskExecutor projectionExecutor;

    @Autowired
    @Qualifier("notificationExecutor")
    private TaskExecutor notificationExecutor;

    @Autowired
    @Qualifier("auditExecutor")
    private TaskExecutor auditExecutor;

    @Autowired
    @Qualifier("gmailApiExecutor")
    private TaskExecutor gmailApiExecutor;

    @Test
    void executorsArePresentWithExpectedTypes() {
        assertThat(projectionExecutor).isInstanceOf(ThreadPoolTaskExecutor.class);
        assertThat(notificationExecutor).isInstanceOf(ThreadPoolTaskExecutor.class);
        assertThat(auditExecutor).isInstanceOf(ThreadPoolTaskExecutor.class);
        assertThat(gmailApiExecutor).isInstanceOf(ConcurrentTaskExecutor.class);
    }
}
