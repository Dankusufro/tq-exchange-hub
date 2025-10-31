CREATE TABLE trade_receipts (
    id UUID PRIMARY KEY,
    trade_id UUID NOT NULL UNIQUE,
    pdf_content BYTEA NOT NULL,
    hash_signature VARCHAR(128) NOT NULL,
    generated_signature VARCHAR(128) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_trade_receipt_trade FOREIGN KEY (trade_id) REFERENCES trades(id)
);
