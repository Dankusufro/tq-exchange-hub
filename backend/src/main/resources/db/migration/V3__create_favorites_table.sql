CREATE TABLE favorites (
    id UUID PRIMARY KEY,
    profile_id UUID NOT NULL,
    item_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_favorites_profile FOREIGN KEY (profile_id) REFERENCES profiles (id) ON DELETE CASCADE,
    CONSTRAINT fk_favorites_item FOREIGN KEY (item_id) REFERENCES items (id) ON DELETE CASCADE,
    CONSTRAINT uk_favorites_profile_item UNIQUE (profile_id, item_id)
);

INSERT INTO favorites (id, profile_id, item_id, created_at) VALUES
    ('b8b6a1b5-ec9f-4f61-9f30-8d5f1e6e7a21', '3d72ee13-171c-4b58-8004-dcf98c7feaa3', 'b40cd5e0-750d-4b5e-acf3-fd31876d6b3c', '2024-01-15T10:00:00Z'),
    ('6f57a804-4d35-4cb6-8a57-5101496cd5c3', 'd60a3ca1-9f92-44e5-a8de-7258c73bc27e', '072782b0-7af7-44c9-a017-237e991ecfc7', '2024-01-16T09:30:00Z'),
    ('b58f15cb-45d9-44a7-bb4e-6b4f52c1d7d8', '89e78102-0b5d-4260-8a23-2efa0b88e84c', '961e191c-e71a-48cb-81b7-cc7c1b7b5969', '2024-01-17T14:45:00Z');
