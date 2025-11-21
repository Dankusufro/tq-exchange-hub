-- Seed 100,000 synthetic profiles, accounts, and items for load testing
-- Runs automatically via Flyway on application startup.

-- Compatible with both PostgreSQL and H2; avoids database-specific extensions or functions.

-- Insert synthetic profiles
WITH RECURSIVE seq(n) AS (
    SELECT 1
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 100000
)
INSERT INTO profiles (
    id,
    display_name,
    bio,
    avatar_url,
    location,
    phone,
    rating,
    total_trades,
    created_at,
    updated_at
)
SELECT
    CAST(CONCAT('00000000-0000-0000-0000-', LPAD(CAST(n AS VARCHAR), 12, '0')) AS UUID) AS id,
    CONCAT('Usuario Demo ', n) AS display_name,
    'Perfil generado automáticamente para pruebas de rendimiento.' AS bio,
    'https://images.unsplash.com/photo-1524504388940-b1c1722653e1' AS avatar_url,
    CONCAT('Ciudad Demo ', MOD(n, 100)) AS location,
    CONCAT('+34 600 ', LPAD(CAST(n AS VARCHAR), 6, '0')) AS phone,
    ROUND(3 + (MOD(n, 200) / 100.0), 2) AS rating,
    MOD(n, 50) AS total_trades,
    DATEADD('DAY', -MOD(n, 365), CURRENT_TIMESTAMP) AS created_at,
    CURRENT_TIMESTAMP AS updated_at
FROM seq;

-- Insert synthetic user accounts bound to the generated profiles
WITH RECURSIVE seq(n) AS (
    SELECT 1
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 100000
)
INSERT INTO user_accounts (
    id,
    email,
    password,
    profile_id,
    created_at,
    role
)
SELECT
    CAST(CONCAT('10000000-0000-0000-0000-', LPAD(CAST(n AS VARCHAR), 12, '0')) AS UUID) AS id,
    CONCAT('demo_user_', n, '@example.com') AS email,
    '$2b$12$aBTtH4Q5cHn7pLnachcReemx5StqD.L.6haQnbxAy48DCo7yITLoe' AS password,
    CAST(CONCAT('00000000-0000-0000-0000-', LPAD(CAST(n AS VARCHAR), 12, '0')) AS UUID) AS profile_id,
    CURRENT_TIMESTAMP AS created_at,
    'USER' AS role
FROM seq;

-- Insert synthetic items linked to the generated profiles and existing categories
WITH RECURSIVE seq(n) AS (
    SELECT 1
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 100000
)
INSERT INTO items (
    id,
    user_id,
    category_id,
    title,
    description,
    condition,
    estimated_value,
    is_available,
    is_service,
    location,
    created_at,
    updated_at
)
SELECT
    CAST(CONCAT('20000000-0000-0000-0000-', LPAD(CAST(n AS VARCHAR), 12, '0')) AS UUID) AS id,
    CAST(CONCAT('00000000-0000-0000-0000-', LPAD(CAST(n AS VARCHAR), 12, '0')) AS UUID) AS user_id,
    (
        SELECT id
        FROM (
            SELECT id, ROW_NUMBER() OVER (ORDER BY id) AS rn
            FROM categories
        ) c
        WHERE c.rn = MOD(n - 1, (SELECT COUNT(*) FROM categories)) + 1
    ) AS category_id,
    CONCAT('Artículo demo ', n) AS title,
    'Artículo generado automáticamente para pruebas de carga.' AS description,
    'Bueno' AS condition,
    CAST(50 + MOD(n, 950) AS DECIMAL(19, 2)) AS estimated_value,
    TRUE AS is_available,
    FALSE AS is_service,
    CONCAT('Ciudad Demo ', MOD(n, 100)) AS location,
    DATEADD('DAY', -MOD(n, 180), CURRENT_TIMESTAMP) AS created_at,
    CURRENT_TIMESTAMP AS updated_at
FROM seq;
