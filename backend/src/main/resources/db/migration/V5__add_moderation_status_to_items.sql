ALTER TABLE items
    ADD COLUMN moderation_status VARCHAR(30) NOT NULL DEFAULT 'APPROVED';

UPDATE items
SET moderation_status = 'APPROVED'
WHERE moderation_status IS NULL;
