-- Schema creation
CREATE TABLE categories (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    icon VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE profiles (
    id UUID PRIMARY KEY,
    display_name VARCHAR(255) NOT NULL,
    bio TEXT,
    avatar_url TEXT,
    location VARCHAR(255),
    phone VARCHAR(50),
    rating DOUBLE PRECISION,
    total_trades INTEGER,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE user_accounts (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    profile_id UUID NOT NULL UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    role VARCHAR(20) NOT NULL,
    CONSTRAINT fk_user_accounts_profile FOREIGN KEY (profile_id) REFERENCES profiles (id)
);

CREATE TABLE items (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    category_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    condition VARCHAR(255) NOT NULL,
    estimated_value NUMERIC(19,2),
    is_available BOOLEAN,
    is_service BOOLEAN,
    location VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_items_owner FOREIGN KEY (user_id) REFERENCES profiles (id),
    CONSTRAINT fk_items_category FOREIGN KEY (category_id) REFERENCES categories (id)
);

CREATE TABLE item_images (
    item_id UUID NOT NULL,
    image_url TEXT NOT NULL,
    PRIMARY KEY (item_id, image_url),
    CONSTRAINT fk_item_images_item FOREIGN KEY (item_id) REFERENCES items (id) ON DELETE CASCADE
);

CREATE TABLE item_wishlist (
    item_id UUID NOT NULL,
    wishlist_item TEXT NOT NULL,
    PRIMARY KEY (item_id, wishlist_item),
    CONSTRAINT fk_item_wishlist_item FOREIGN KEY (item_id) REFERENCES items (id) ON DELETE CASCADE
);

CREATE TABLE trades (
    id UUID PRIMARY KEY,
    owner_id UUID NOT NULL,
    requester_id UUID NOT NULL,
    owner_item_id UUID NOT NULL,
    requester_item_id UUID,
    message TEXT,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_trades_owner FOREIGN KEY (owner_id) REFERENCES profiles (id),
    CONSTRAINT fk_trades_requester FOREIGN KEY (requester_id) REFERENCES profiles (id),
    CONSTRAINT fk_trades_owner_item FOREIGN KEY (owner_item_id) REFERENCES items (id),
    CONSTRAINT fk_trades_requester_item FOREIGN KEY (requester_item_id) REFERENCES items (id)
);

CREATE TABLE messages (
    id UUID PRIMARY KEY,
    trade_id UUID NOT NULL,
    sender_id UUID NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_messages_trade FOREIGN KEY (trade_id) REFERENCES trades (id) ON DELETE CASCADE,
    CONSTRAINT fk_messages_sender FOREIGN KEY (sender_id) REFERENCES profiles (id)
);

CREATE TABLE reviews (
    id UUID PRIMARY KEY,
    trade_id UUID NOT NULL,
    reviewer_id UUID NOT NULL,
    reviewed_id UUID NOT NULL,
    rating INTEGER NOT NULL,
    comment TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_reviews_trade FOREIGN KEY (trade_id) REFERENCES trades (id) ON DELETE CASCADE,
    CONSTRAINT fk_reviews_reviewer FOREIGN KEY (reviewer_id) REFERENCES profiles (id),
    CONSTRAINT fk_reviews_reviewed FOREIGN KEY (reviewed_id) REFERENCES profiles (id)
);

-- Seed data
INSERT INTO categories (id, name, description, icon, created_at) VALUES
    ('3b28c8ee-7724-4eab-955e-a68b7ec1d356', 'Libros y Educación', 'Material académico y lecturas inspiradoras para estudiantes.', 'lucide:book-open', '2023-12-06T12:00:00Z'),
    ('1a2d0e53-dc40-431d-aa4a-c90098405a4c', 'Ropa y Accesorios', 'Prendas únicas y accesorios en excelente estado.', 'lucide:shirt', '2023-12-08T12:00:00Z'),
    ('8a3dc0fb-ac14-4c24-9f7e-1b18eb89c75e', 'Electrónicos', 'Dispositivos y gadgets listos para un nuevo hogar.', 'lucide:smartphone', '2023-12-10T12:00:00Z'),
    ('92312e90-ce77-49d2-b3cd-685b4e27983a', 'Hogar y Jardín', 'Decoración, muebles y plantas para renovar tus espacios.', 'lucide:home', '2023-12-12T12:00:00Z'),
    ('8167d8f5-d7ef-4078-83f7-61a83a2564ae', 'Vehículos', 'Movilidad urbana y opciones sostenibles para transportarte.', 'lucide:car', '2023-12-14T12:00:00Z'),
    ('7a94b32c-e8f8-477c-b367-189872234263', 'Arte y Manualidades', 'Creaciones hechas a mano y materiales creativos.', 'lucide:palette', '2023-12-16T12:00:00Z'),
    ('52543f69-94f1-44f9-ae91-9e0d8c83dc7e', 'Deportes', 'Equipamiento y accesorios para mantenerte activo.', 'lucide:dumbbell', '2023-12-18T12:00:00Z'),
    ('bab5a72d-ac73-4f6d-940a-44a84188db67', 'Servicios', 'Talentos y habilidades listos para ayudarte.', 'lucide:wrench', '2023-12-20T12:00:00Z');

INSERT INTO profiles (id, display_name, bio, avatar_url, location, phone, rating, total_trades, created_at, updated_at) VALUES
    ('3d72ee13-171c-4b58-8004-dcf98c7feaa3', 'María López', 'Apasionada por la fotografía y los viajes en bici.', 'https://images.unsplash.com/photo-1524504388940-b1c1722653e1', 'Madrid, España', '+34 600 123 456', 4.9, 23, '2023-07-15T12:00:00Z', '2024-01-13T12:00:00Z'),
    ('d60a3ca1-9f92-44e5-a8de-7258c73bc27e', 'Luis García', 'Coleccionista de tecnología vintage y lector empedernido.', 'https://images.unsplash.com/photo-1527980965255-d3b416303d12', 'Barcelona, España', '+34 611 987 654', 4.7, 15, '2023-08-15T12:00:00Z', '2024-01-11T12:00:00Z'),
    ('89e78102-0b5d-4260-8a23-2efa0b88e84c', 'Ana Ruiz', 'Diseñadora freelance y amante de la jardinería.', 'https://images.unsplash.com/photo-1521572267360-ee0c2909d518', 'Valencia, España', '+34 622 456 789', 4.8, 11, '2023-09-15T12:00:00Z', '2024-01-10T12:00:00Z'),
    ('87993c18-3d78-4527-8ac2-72a9e982b3c9', 'Carlos Mendoza', 'Amante del deporte y las aventuras al aire libre.', 'https://images.unsplash.com/photo-1544723795-3fb6469f5b39', 'Ciudad de México, México', '+52 55 1234 5678', 4.8, 15, '2023-06-15T12:00:00Z', '2024-01-12T12:00:00Z'),
    ('f13f41a7-d724-4b27-acb5-a28ab0cc6b0b', 'Sofía Vega', 'Instructora certificada de yoga y bienestar integral.', 'https://images.unsplash.com/photo-1524504388940-b1c1722653e1?ixid=sofia', 'Puebla, México', '+52 22 9876 5432', 5.0, 19, '2023-05-15T12:00:00Z', '2024-01-14T12:00:00Z');

INSERT INTO user_accounts (id, email, password, profile_id, created_at, role) VALUES
    ('a2b5b51a-4927-4ba8-aaee-3907351fed34', 'maria@example.com', '$2b$12$aBTtH4Q5cHn7pLnachcReemx5StqD.L.6haQnbxAy48DCo7yITLoe', '3d72ee13-171c-4b58-8004-dcf98c7feaa3', '2023-07-15T12:00:00Z', 'ADMIN'),
    ('65cb5451-765c-4599-aaf1-068ec3eb11d2', 'luis@example.com', '$2b$12$aBTtH4Q5cHn7pLnachcReemx5StqD.L.6haQnbxAy48DCo7yITLoe', 'd60a3ca1-9f92-44e5-a8de-7258c73bc27e', '2023-08-15T12:00:00Z', 'USER'),
    ('383ff7c3-6d5b-41fd-915e-ed3f63f8928f', 'ana@example.com', '$2b$12$aBTtH4Q5cHn7pLnachcReemx5StqD.L.6haQnbxAy48DCo7yITLoe', '89e78102-0b5d-4260-8a23-2efa0b88e84c', '2023-09-15T12:00:00Z', 'USER'),
    ('a79ac9ed-6390-4c99-8151-bc15f77433aa', 'carlos@example.com', '$2b$12$aBTtH4Q5cHn7pLnachcReemx5StqD.L.6haQnbxAy48DCo7yITLoe', '87993c18-3d78-4527-8ac2-72a9e982b3c9', '2023-06-15T12:00:00Z', 'USER'),
    ('2bfc7d82-fc2d-4173-95ee-69e036f7fd21', 'sofia@example.com', '$2b$12$aBTtH4Q5cHn7pLnachcReemx5StqD.L.6haQnbxAy48DCo7yITLoe', 'f13f41a7-d724-4b27-acb5-a28ab0cc6b0b', '2023-05-15T12:00:00Z', 'USER');

INSERT INTO items (id, user_id, category_id, title, description, condition, estimated_value, is_available, is_service, location, created_at, updated_at) VALUES
    ('961e191c-e71a-48cb-81b7-cc7c1b7b5969', '87993c18-3d78-4527-8ac2-72a9e982b3c9', '52543f69-94f1-44f9-ae91-9e0d8c83dc7e', 'Bicicleta de montaña Trek', 'Suspensión ajustable y frenos de disco. Lista para rutas en la sierra.', 'Muy buena', 680.00, TRUE, FALSE, 'Ciudad de México, México', '2024-01-03T12:00:00Z', '2024-01-13T12:00:00Z'),
    ('b40cd5e0-750d-4b5e-acf3-fd31876d6b3c', '89e78102-0b5d-4260-8a23-2efa0b88e84c', '1a2d0e53-dc40-431d-aa4a-c90098405a4c', 'Chaqueta de cuero vintage', 'Piel auténtica, talla M. Conserva el forro original y cierres funcionales.', 'Excelente', 320.00, TRUE, FALSE, 'Valencia, España', '2024-01-06T12:00:00Z', '2024-01-12T12:00:00Z'),
    ('8ff30c8c-d5f4-406f-8b95-01073f7bc079', 'd60a3ca1-9f92-44e5-a8de-7258c73bc27e', '3b28c8ee-7724-4eab-955e-a68b7ec1d356', 'Colección de libros universitarios', 'Textos de ingeniería y matemáticas. Incluye marcadores y notas destacadas.', 'Bueno', 150.00, TRUE, FALSE, 'Barcelona, España', '2024-01-04T12:00:00Z', '2024-01-11T12:00:00Z'),
    ('05ed029c-30cb-457b-8d36-71dc7988fb6a', 'f13f41a7-d724-4b27-acb5-a28ab0cc6b0b', 'bab5a72d-ac73-4f6d-940a-44a84188db67', 'Clases de yoga personalizadas', 'Sesiones online y presenciales enfocadas en respiración y fuerza.', 'Servicio', 180.00, TRUE, TRUE, 'Puebla y modalidad virtual', '2024-01-07T12:00:00Z', '2024-01-14T12:00:00Z'),
    ('072782b0-7af7-44c9-a017-237e991ecfc7', '3d72ee13-171c-4b58-8004-dcf98c7feaa3', '8a3dc0fb-ac14-4c24-9f7e-1b18eb89c75e', 'iPhone 12 Pro 128GB', 'Pantalla impecable, batería al 89%. Incluye cargador original y funda protectora.', 'Muy bueno', 720.00, TRUE, FALSE, 'Madrid, España', '2024-01-09T12:00:00Z', '2024-01-13T12:00:00Z'),
    ('58eda8a6-6ccc-4f04-b662-5241e2dc7c51', '89e78102-0b5d-4260-8a23-2efa0b88e84c', '92312e90-ce77-49d2-b3cd-685b4e27983a', 'Conjunto de plantas de interior', 'Incluye macetas de cerámica, guía de cuidados y fertilizante orgánico.', 'Excelente', 95.00, TRUE, FALSE, 'Valencia, España', '2024-01-10T12:00:00Z', '2024-01-14T12:00:00Z'),
    ('c149ba48-5384-49e6-9265-805fc061dc45', '3d72ee13-171c-4b58-8004-dcf98c7feaa3', '7a94b32c-e8f8-477c-b367-189872234263', 'Kit de acuarela profesional', 'Incluye 24 colores, pinceles premium y papel de algodón prensado en frío.', 'Nuevo', 120.00, TRUE, FALSE, 'Madrid, España', '2024-01-11T12:00:00Z', '2024-01-14T12:00:00Z'),
    ('5b8f1cc5-5a15-4f22-b914-1e6ff2862cf7', '87993c18-3d78-4527-8ac2-72a9e982b3c9', '8167d8f5-d7ef-4078-83f7-61a83a2564ae', 'Scooter eléctrico urbano', 'Autonomía de 25 km, plegable y ligero. Incluye cargador y bolsa de transporte.', 'Muy bueno', 540.00, TRUE, FALSE, 'Ciudad de México, México', '2024-01-12T12:00:00Z', '2024-01-15T00:00:00Z');

INSERT INTO item_images (item_id, image_url) VALUES
    ('961e191c-e71a-48cb-81b7-cc7c1b7b5969', 'https://images.unsplash.com/photo-1518655048521-f130df041f66'),
    ('961e191c-e71a-48cb-81b7-cc7c1b7b5969', 'https://images.unsplash.com/photo-1509395176047-4a66953fd231'),
    ('b40cd5e0-750d-4b5e-acf3-fd31876d6b3c', 'https://images.unsplash.com/photo-1542291026-7eec264c27ff'),
    ('b40cd5e0-750d-4b5e-acf3-fd31876d6b3c', 'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab'),
    ('8ff30c8c-d5f4-406f-8b95-01073f7bc079', 'https://images.unsplash.com/photo-1523475472560-d2df97ec485c'),
    ('8ff30c8c-d5f4-406f-8b95-01073f7bc079', 'https://images.unsplash.com/photo-1521587760476-6c12a4b040da'),
    ('05ed029c-30cb-457b-8d36-71dc7988fb6a', 'https://images.unsplash.com/photo-1554306274-f23873d9a26f'),
    ('05ed029c-30cb-457b-8d36-71dc7988fb6a', 'https://images.unsplash.com/photo-1552196563-55cd4e45efb3'),
    ('072782b0-7af7-44c9-a017-237e991ecfc7', 'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9'),
    ('072782b0-7af7-44c9-a017-237e991ecfc7', 'https://images.unsplash.com/photo-1512496015851-a90fb38ba796'),
    ('58eda8a6-6ccc-4f04-b662-5241e2dc7c51', 'https://images.unsplash.com/photo-1483794344563-d27a8d18014e'),
    ('58eda8a6-6ccc-4f04-b662-5241e2dc7c51', 'https://images.unsplash.com/photo-1493663284031-b7e3aefcae8e'),
    ('c149ba48-5384-49e6-9265-805fc061dc45', 'https://images.unsplash.com/photo-1526498460520-4c246339dccb'),
    ('c149ba48-5384-49e6-9265-805fc061dc45', 'https://images.unsplash.com/photo-1513364776144-60967b0f800f'),
    ('5b8f1cc5-5a15-4f22-b914-1e6ff2862cf7', 'https://images.unsplash.com/photo-1519750157634-b6d493a0f77d'),
    ('5b8f1cc5-5a15-4f22-b914-1e6ff2862cf7', 'https://images.unsplash.com/photo-1502877338535-766e1452684a');

INSERT INTO item_wishlist (item_id, wishlist_item) VALUES
    ('961e191c-e71a-48cb-81b7-cc7c1b7b5969', 'Laptop'),
    ('961e191c-e71a-48cb-81b7-cc7c1b7b5969', 'Cámara fotográfica'),
    ('961e191c-e71a-48cb-81b7-cc7c1b7b5969', 'Guitarra eléctrica'),
    ('b40cd5e0-750d-4b5e-acf3-fd31876d6b3c', 'Botas de cuero'),
    ('b40cd5e0-750d-4b5e-acf3-fd31876d6b3c', 'Reloj clásico'),
    ('b40cd5e0-750d-4b5e-acf3-fd31876d6b3c', 'Perfume unisex'),
    ('8ff30c8c-d5f4-406f-8b95-01073f7bc079', 'Tablet'),
    ('8ff30c8c-d5f4-406f-8b95-01073f7bc079', 'Auriculares'),
    ('8ff30c8c-d5f4-406f-8b95-01073f7bc079', 'Mochila ergonómica'),
    ('05ed029c-30cb-457b-8d36-71dc7988fb6a', 'Masajes terapéuticos'),
    ('05ed029c-30cb-457b-8d36-71dc7988fb6a', 'Clases de cocina'),
    ('05ed029c-30cb-457b-8d36-71dc7988fb6a', 'Consultas nutricionales'),
    ('072782b0-7af7-44c9-a017-237e991ecfc7', 'Laptop ligera'),
    ('072782b0-7af7-44c9-a017-237e991ecfc7', 'Consola portátil'),
    ('072782b0-7af7-44c9-a017-237e991ecfc7', 'Bicicleta urbana'),
    ('58eda8a6-6ccc-4f04-b662-5241e2dc7c51', 'Libros de botánica'),
    ('58eda8a6-6ccc-4f04-b662-5241e2dc7c51', 'Herramientas de jardinería'),
    ('58eda8a6-6ccc-4f04-b662-5241e2dc7c51', 'Decoración artesanal'),
    ('c149ba48-5384-49e6-9265-805fc061dc45', 'Clases de cerámica'),
    ('c149ba48-5384-49e6-9265-805fc061dc45', 'Marco de madera'),
    ('c149ba48-5384-49e6-9265-805fc061dc45', 'Impresora fotográfica'),
    ('5b8f1cc5-5a15-4f22-b914-1e6ff2862cf7', 'Cámara deportiva'),
    ('5b8f1cc5-5a15-4f22-b914-1e6ff2862cf7', 'Curso de fotografía'),
    ('5b8f1cc5-5a15-4f22-b914-1e6ff2862cf7', 'Mochila impermeable');

INSERT INTO trades (id, owner_id, requester_id, owner_item_id, requester_item_id, message, status, created_at, updated_at) VALUES
    ('ac9abc26-a864-4d88-9b75-f36d2120bb1e', '3d72ee13-171c-4b58-8004-dcf98c7feaa3', '87993c18-3d78-4527-8ac2-72a9e982b3c9', '072782b0-7af7-44c9-a017-237e991ecfc7', '961e191c-e71a-48cb-81b7-cc7c1b7b5969', '¿Te interesa cambiar tu bicicleta por mi iPhone? Podemos ajustar accesorios.', 'COMPLETED', '2024-01-10T12:00:00Z', '2024-01-14T12:00:00Z');

INSERT INTO messages (id, trade_id, sender_id, content, created_at) VALUES
    ('acd2b964-4778-4d88-a152-cfb4c69b0b91', 'ac9abc26-a864-4d88-9b75-f36d2120bb1e', '87993c18-3d78-4527-8ac2-72a9e982b3c9', 'Hola María, el iPhone se ve impecable. ¿Incluyes la funda en el intercambio?', '2024-01-10T15:00:00Z');

INSERT INTO reviews (id, trade_id, reviewer_id, reviewed_id, rating, comment, created_at) VALUES
    ('428eb128-56d1-4f26-8966-582f855f78de', 'ac9abc26-a864-4d88-9b75-f36d2120bb1e', '87993c18-3d78-4527-8ac2-72a9e982b3c9', '3d72ee13-171c-4b58-8004-dcf98c7feaa3', 5, 'Intercambio impecable, el dispositivo llegó como se prometió.', '2024-01-14T12:00:00Z');
