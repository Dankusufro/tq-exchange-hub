CREATE TABLE abuse_reports (
    id UUID PRIMARY KEY,
    reason VARCHAR(2000) NOT NULL,
    reporter_id UUID NOT NULL,
    reported_user_id UUID,
    reported_item_id UUID,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    resolved_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_abuse_reports_reporter FOREIGN KEY (reporter_id) REFERENCES profiles (id),
    CONSTRAINT fk_abuse_reports_reported_user FOREIGN KEY (reported_user_id) REFERENCES profiles (id),
    CONSTRAINT fk_abuse_reports_reported_item FOREIGN KEY (reported_item_id) REFERENCES items (id)
);
