-- Add classification_result column (CRITICAL-4/CRITICAL-5)
ALTER TABLE email_messages ADD COLUMN IF NOT EXISTS classification_result TEXT;

-- Add index for email_accounts active column if not present (already in V2 for email_accounts)
CREATE INDEX IF NOT EXISTS idx_email_messages_email_account_id ON email_messages (email_account_id);
