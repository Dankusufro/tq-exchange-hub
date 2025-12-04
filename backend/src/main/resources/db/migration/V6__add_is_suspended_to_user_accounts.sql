ALTER TABLE user_accounts
    ADD COLUMN IF NOT EXISTS is_suspended BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE user_accounts
SET is_suspended = FALSE
WHERE is_suspended IS NULL;
