-- Seed 100,000 synthetic profiles, accounts, and items for load testing
-- Runs automatically via Flyway on application startup.

CREATE EXTENSION IF NOT EXISTS pgcrypto;

DO $$
DECLARE
    target_records INT := 100000;
    batch_size INT := 2000;
    remaining INT := target_records;
    current_batch INT;
BEGIN
    WHILE remaining > 0 LOOP
        current_batch := LEAST(batch_size, remaining);

        WITH new_profiles AS (
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
                gen_random_uuid(),
                format('Usuario Demo %s', target_records - remaining + gs),
                'Perfil generado automáticamente para pruebas de rendimiento.',
                'https://images.unsplash.com/photo-1524504388940-b1c1722653e1',
                format('Ciudad Demo %s', (random() * 100)::INT),
                format('+34 600 %06s', target_records - remaining + gs),
                ROUND((random() * 2 + 3)::NUMERIC, 2),
                (random() * 50)::INT,
                NOW() - (random() * 365 || ' days')::INTERVAL,
                NOW()
            FROM generate_series(1, current_batch) AS gs
            RETURNING id, display_name
        ), new_accounts AS (
            INSERT INTO user_accounts (
                id,
                email,
                password,
                profile_id,
                created_at,
                role
            )
            SELECT
                gen_random_uuid(),
                format('demo_user_%s@example.com', target_records - remaining + ROW_NUMBER() OVER (ORDER BY np.id)),
                '$2b$12$aBTtH4Q5cHn7pLnachcReemx5StqD.L.6haQnbxAy48DCo7yITLoe',
                np.id,
                NOW(),
                'USER'
            FROM new_profiles np
            RETURNING profile_id
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
            gen_random_uuid(),
            np.id,
            (SELECT id FROM categories ORDER BY random() LIMIT 1),
            format('Artículo demo %s', target_records - remaining + ROW_NUMBER() OVER (ORDER BY np.id)),
            'Artículo generado automáticamente para pruebas de carga.',
            'Bueno',
            (random() * 900 + 50)::NUMERIC(19, 2),
            TRUE,
            FALSE,
            format('Ciudad Demo %s', (random() * 100)::INT),
            NOW() - (random() * 180 || ' days')::INTERVAL,
            NOW()
        FROM new_profiles np;

        remaining := remaining - current_batch;
    END LOOP;
END $$;
