CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE email_accounts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email_address VARCHAR(320) NOT NULL,
    display_name VARCHAR(255) NOT NULL,
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    encrypted_access_token TEXT NOT NULL,
    encrypted_refresh_token TEXT NOT NULL,
    token_expiry TIMESTAMPTZ NOT NULL,
    sync_history_id VARCHAR(255),
    watch_expiration TIMESTAMPTZ,
    push_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    empty_poll_count INTEGER NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uq_email_accounts_email_address UNIQUE (email_address)
);

CREATE TABLE application_threads (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    company_domain VARCHAR(255) NOT NULL,
    job_application_id UUID,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE job_applications (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    company_name VARCHAR(255) NOT NULL,
    position_title VARCHAR(255) NOT NULL,
    job_url VARCHAR(2048),
    status VARCHAR(64) NOT NULL,
    manually_overridden BOOLEAN NOT NULL DEFAULT FALSE,
    notes TEXT,
    email_account_id UUID NOT NULL,
    application_thread_id UUID,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE email_messages (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    gmail_message_id VARCHAR(255) NOT NULL,
    gmail_thread_id VARCHAR(255) NOT NULL,
    subject VARCHAR(1000),
    body_text TEXT,
    body_html TEXT,
    sender_email VARCHAR(320) NOT NULL,
    direction VARCHAR(32) NOT NULL,
    classification VARCHAR(64),
    confidence_score DECIMAL(5,4),
    processed BOOLEAN NOT NULL DEFAULT FALSE,
    sent_at TIMESTAMPTZ NOT NULL,
    email_account_id UUID NOT NULL,
    application_thread_id UUID,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uq_email_messages_gmail_message_id UNIQUE (gmail_message_id)
);

CREATE TABLE state_transition_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    job_application_id UUID NOT NULL,
    from_status VARCHAR(64) NOT NULL,
    to_status VARCHAR(64) NOT NULL,
    trigger_event VARCHAR(64) NOT NULL,
    triggering_message_id UUID,
    occurred_at TIMESTAMPTZ NOT NULL,
    compensated BOOLEAN NOT NULL DEFAULT FALSE
);

ALTER TABLE job_applications
    ADD CONSTRAINT fk_job_applications_email_account
        FOREIGN KEY (email_account_id) REFERENCES email_accounts (id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_job_applications_application_thread
        FOREIGN KEY (application_thread_id) REFERENCES application_threads (id) ON DELETE SET NULL;

ALTER TABLE application_threads
    ADD CONSTRAINT fk_application_threads_job_application
        FOREIGN KEY (job_application_id) REFERENCES job_applications (id) ON DELETE SET NULL;

ALTER TABLE email_messages
    ADD CONSTRAINT fk_email_messages_email_account
        FOREIGN KEY (email_account_id) REFERENCES email_accounts (id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_email_messages_application_thread
        FOREIGN KEY (application_thread_id) REFERENCES application_threads (id) ON DELETE SET NULL;

ALTER TABLE state_transition_logs
    ADD CONSTRAINT fk_state_transition_logs_job_application
        FOREIGN KEY (job_application_id) REFERENCES job_applications (id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_state_transition_logs_triggering_message
        FOREIGN KEY (triggering_message_id) REFERENCES email_messages (id) ON DELETE SET NULL;

CREATE INDEX idx_email_messages_gmail_thread_id ON email_messages (gmail_thread_id);
CREATE INDEX idx_email_messages_email_account_id ON email_messages (email_account_id);
CREATE INDEX idx_email_messages_application_thread_id ON email_messages (application_thread_id);
CREATE INDEX idx_email_messages_processed ON email_messages (processed);
CREATE INDEX idx_email_messages_sent_at ON email_messages (sent_at);

CREATE INDEX idx_job_applications_status ON job_applications (status);
CREATE INDEX idx_job_applications_email_account_id ON job_applications (email_account_id);
CREATE INDEX idx_job_applications_application_thread_id ON job_applications (application_thread_id);

CREATE INDEX idx_application_threads_company_domain ON application_threads (company_domain);
CREATE INDEX idx_application_threads_job_application_id ON application_threads (job_application_id);

CREATE INDEX idx_state_transition_logs_job_application_id ON state_transition_logs (job_application_id);
CREATE INDEX idx_state_transition_logs_occurred_at ON state_transition_logs (occurred_at);
