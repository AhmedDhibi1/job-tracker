-- Add missing partial unique index for primary account enforcement (Issue #1)
CREATE UNIQUE INDEX idx_unique_primary_account
    ON email_accounts (is_primary) WHERE is_primary = true;

-- Add missing index for active accounts filter (Issue #25)
CREATE INDEX idx_email_account_active ON email_accounts (active);

-- Add missing index for gmail_message_id lookups (Issue #19)
CREATE INDEX idx_email_messages_gmail_message_id ON email_messages (gmail_message_id);

-- Remove circular FK: application_threads.job_application_id -> job_applications.id (Issue #15)
ALTER TABLE application_threads DROP CONSTRAINT IF EXISTS fk_application_threads_job_application;
ALTER TABLE application_threads DROP COLUMN IF EXISTS job_application_id;

-- Add missing tables for @ElementCollection mappings (Issue #7)
CREATE TABLE email_message_recipients (
    email_message_id UUID NOT NULL REFERENCES email_messages(id) ON DELETE CASCADE,
    recipient_email VARCHAR(320) NOT NULL
);

CREATE TABLE email_message_evidence (
    email_message_id UUID NOT NULL REFERENCES email_messages(id) ON DELETE CASCADE,
    evidence_keyword VARCHAR(100) NOT NULL
);

-- email_attachments table already exists from @Entity mapping;
-- ensure FK and index are present
CREATE INDEX IF NOT EXISTS idx_email_attachments_message_id
    ON email_attachments (email_message_id);

-- Add token_refresh_audits table for token rotation audit logging (Improvement #3)
CREATE TABLE token_refresh_audits (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    account_id UUID NOT NULL REFERENCES email_accounts(id) ON DELETE CASCADE,
    attempted_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    success BOOLEAN NOT NULL,
    failure_reason VARCHAR(500)
);

-- Add outbox_events table for Transactional Outbox Pattern (Improvement #1)
CREATE TABLE outbox_events (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    aggregate_type VARCHAR(100) NOT NULL,
    aggregate_id UUID NOT NULL,
    event_type VARCHAR(200) NOT NULL,
    payload TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    published BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_outbox_events_published ON outbox_events (published, created_at);
