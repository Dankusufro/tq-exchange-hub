-- Create notifications table to persist websocket alerts
CREATE TABLE notifications (
    id UUID PRIMARY KEY,
    recipient_id UUID NOT NULL,
    type VARCHAR(30) NOT NULL,
    title VARCHAR(150) NOT NULL,
    message VARCHAR(2000) NOT NULL,
    read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    trade_id UUID,
    message_id UUID,
    CONSTRAINT fk_notifications_recipient FOREIGN KEY (recipient_id) REFERENCES profiles (id) ON DELETE CASCADE,
    CONSTRAINT fk_notifications_trade FOREIGN KEY (trade_id) REFERENCES trades (id) ON DELETE SET NULL,
    CONSTRAINT fk_notifications_message FOREIGN KEY (message_id) REFERENCES messages (id) ON DELETE SET NULL
);

CREATE INDEX idx_notifications_recipient_created_at ON notifications (recipient_id, created_at DESC);
