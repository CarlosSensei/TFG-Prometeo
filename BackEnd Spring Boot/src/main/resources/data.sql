-- =========================================================
-- OPTIFLOW CRM
-- MOCK DATA
-- H2 DATABASE
-- =========================================================


-- =========================================================
-- USERS
-- =========================================================

-- Password para todos los usuarios mock:
-- password
--
-- Hash BCrypt de "password":
-- $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy


MERGE INTO users (
    id,
    first_name,
    last_name,
    email,
    password,
    role,
    active,
    created_at,
    updated_at
    )
    KEY(id)
    VALUES (
    1,
    'Carlos',
    'Administrador',
    'admin@optiflow.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'ADMIN',
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
    );


MERGE INTO users (
    id,
    first_name,
    last_name,
    email,
    password,
    role,
    active,
    created_at,
    updated_at
    )
    KEY(id)
    VALUES (
    2,
    'Laura',
    'Martínez',
    'laura@optiflow.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'SELLER',
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
    );


MERGE INTO users (
    id,
    first_name,
    last_name,
    email,
    password,
    role,
    active,
    created_at,
    updated_at
    )
    KEY(id)
    VALUES (
    3,
    'David',
    'Sánchez',
    'david@optiflow.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'OPTICIAN',
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
    );


MERGE INTO users (
    id,
    first_name,
    last_name,
    email,
    password,
    role,
    active,
    created_at,
    updated_at
    )
    KEY(id)
    VALUES (
    4,
    'Marta',
    'Gómez',
    'marta@optiflow.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'DIRECTOR',
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
    );


-- =========================================================
-- CLIENTS
-- =========================================================

MERGE INTO clients (
    id,
    name,
    first_surname,
    last_surname,
    dni,
    phone_number,
    email,
    birth_date,
    address,
    city,
    postal_code,
    notes,
    active,
    created_at,
    updated_at
    )
    KEY(id)
    VALUES (
    1,
    'Javier',
    'García',
    'López',
    '12345678Z',
    '600111222',
    'javier.garcia@email.com',
    DATE '1982-03-15',
    'Carrer Mallorca 120',
    'Barcelona',
    '08036',
    'Cliente habitual. Usa lentes progresivas.',
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
    );


MERGE INTO clients (
    id,
    name,
    first_surname,
    last_surname,
    dni,
    phone_number,
    email,
    birth_date,
    address,
    city,
    postal_code,
    notes,
    active,
    created_at,
    updated_at
    )
    KEY(id)
    VALUES (
    2,
    'Ana',
    'Martínez',
    'Ruiz',
    '87654321X',
    '600222333',
    'ana.martinez@email.com',
    DATE '1990-07-24',
    'Carrer Aragó 245',
    'Barcelona',
    '08007',
    'Interesada en monturas ligeras.',
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
    );


MERGE INTO clients (
    id,
    name,
    first_surname,
    last_surname,
    dni,
    phone_number,
    email,
    birth_date,
    address,
    city,
    postal_code,
    notes,
    active,
    created_at,
    updated_at
    )
    KEY(id)
    VALUES (
    3,
    'Marc',
    'Puig',
    'Serra',
    '45678912S',
    '600333444',
    'marc.puig@email.com',
    DATE '1975-11-02',
    'Rambla Catalunya 55',
    'Barcelona',
    '08007',
    'Revisión anual pendiente.',
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
    );


MERGE INTO clients (
    id,
    name,
    first_surname,
    last_surname,
    dni,
    phone_number,
    email,
    birth_date,
    address,
    city,
    postal_code,
    notes,
    active,
    created_at,
    updated_at
    )
    KEY(id)
    VALUES (
    4,
    'Elena',
    'Navarro',
    'Soler',
    '33445566Y',
    '600444555',
    'elena.navarro@email.com',
    DATE '1987-05-10',
    'Avinguda Diagonal 410',
    'Barcelona',
    '08037',
    NULL,
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
    );


MERGE INTO clients (
    id,
    name,
    first_surname,
    last_surname,
    dni,
    phone_number,
    email,
    birth_date,
    address,
    city,
    postal_code,
    notes,
    active,
    created_at,
    updated_at
    )
    KEY(id)
    VALUES (
    5,
    'Sergio',
    'Romero',
    'Vidal',
    '11223344B',
    '600555666',
    'sergio.romero@email.com',
    DATE '1995-09-18',
    'Carrer València 189',
    'Barcelona',
    '08011',
    'Primera compra.',
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
    );


MERGE INTO clients (
    id,
    name,
    first_surname,
    last_surname,
    dni,
    phone_number,
    email,
    birth_date,
    address,
    city,
    postal_code,
    notes,
    active,
    created_at,
    updated_at
    )
    KEY(id)
    VALUES (
    6,
    'Cristina',
    'Fernández',
    'Mas',
    '66778899R',
    '600666777',
    'cristina.fernandez@email.com',
    DATE '1979-01-30',
    'Carrer Balmes 300',
    'Barcelona',
    '08006',
    'Prefiere contacto por email.',
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
    );


-- =========================================================
-- QUOTES
-- =========================================================

-- Quote 1 - borrador
MERGE INTO quotes (
    id,
    quote_number,
    client_id,
    seller_id,
    status,
    quote_date,
    expiration_date,
    subtotal,
    discount,
    tax_rate,
    tax_amount,
    total,
    notes,
    active,
    created_at,
    updated_at
    )
    KEY(id)
    VALUES (
    1,
    'Q-2026-000001',
    1,
    2,
    'DRAFT',
    DATE '2026-10-01',
    DATE '2026-10-31',
    350.00,
    0.00,
    21.00,
    73.50,
    423.50,
    'Montura + lentes progresivas.',
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
    );


-- Quote 2 - enviado
MERGE INTO quotes (
    id,
    quote_number,
    client_id,
    seller_id,
    status,
    quote_date,
    expiration_date,
    subtotal,
    discount,
    tax_rate,
    tax_amount,
    total,
    notes,
    active,
    created_at,
    updated_at
    )
    KEY(id)
    VALUES (
    2,
    'Q-2026-000002',
    2,
    2,
    'SENT',
    DATE '2026-10-02',
    DATE '2026-11-01',
    220.00,
    20.00,
    21.00,
    42.00,
    242.00,
    'Pendiente de respuesta del cliente.',
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
    );


-- Quote 3 - aceptado
MERGE INTO quotes (
    id,
    quote_number,
    client_id,
    seller_id,
    status,
    quote_date,
    expiration_date,
    subtotal,
    discount,
    tax_rate,
    tax_amount,
    total,
    notes,
    active,
    created_at,
    updated_at
    )
    KEY(id)
    VALUES (
    3,
    'Q-2026-000003',
    3,
    3,
    'ACCEPTED',
    DATE '2026-10-03',
    DATE '2026-11-02',
    180.00,
    0.00,
    21.00,
    37.80,
    217.80,
    'Presupuesto aceptado. Pendiente de generar pedido.',
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
    );


-- Quote 4 - convertido a pedido
MERGE INTO quotes (
    id,
    quote_number,
    client_id,
    seller_id,
    status,
    quote_date,
    expiration_date,
    subtotal,
    discount,
    tax_rate,
    tax_amount,
    total,
    notes,
    active,
    created_at,
    updated_at
    )
    KEY(id)
    VALUES (
    4,
    'Q-2026-000004',
    4,
    2,
    'CONVERTED',
    DATE '2026-09-25',
    DATE '2026-10-25',
    500.00,
    50.00,
    21.00,
    94.50,
    544.50,
    'Convertido en pedido.',
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
    );


-- Quote 5 - rechazado
MERGE INTO quotes (
    id,
    quote_number,
    client_id,
    seller_id,
    status,
    quote_date,
    expiration_date,
    subtotal,
    discount,
    tax_rate,
    tax_amount,
    total,
    notes,
    active,
    created_at,
    updated_at
    )
    KEY(id)
    VALUES (
    5,
    'Q-2026-000005',
    5,
    2,
    'REJECTED',
    DATE '2026-09-20',
    DATE '2026-10-20',
    300.00,
    25.00,
    21.00,
    57.75,
    332.75,
    'Cliente considera el precio demasiado elevado.',
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
    );


-- Quote 6 - convertido
MERGE INTO quotes (
    id,
    quote_number,
    client_id,
    seller_id,
    status,
    quote_date,
    expiration_date,
    subtotal,
    discount,
    tax_rate,
    tax_amount,
    total,
    notes,
    active,
    created_at,
    updated_at
    )
    KEY(id)
    VALUES (
    6,
    'Q-2026-000006',
    6,
    3,
    'CONVERTED',
    DATE '2026-09-28',
    DATE '2026-10-28',
    275.00,
    0.00,
    21.00,
    57.75,
    332.75,
    'Cliente confirma el pedido.',
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
    );


-- =========================================================
-- ORDERS
-- =========================================================

-- Pedido generado desde Quote 4
MERGE INTO orders (
    id,
    order_number,
    client_id,
    quote_id,
    seller_id,
    status,
    order_date,
    expected_delivery_date,
    delivery_date,
    subtotal,
    discount,
    tax_rate,
    tax_amount,
    total,
    notes,
    active,
    created_at,
    updated_at
    )
    KEY(id)
    VALUES (
    1,
    'O-2026-000001',
    4,
    4,
    2,
    'IN_PROGRESS',
    DATE '2026-09-26',
    DATE '2026-10-10',
    NULL,
    500.00,
    50.00,
    21.00,
    94.50,
    544.50,
    'Pedido en laboratorio.',
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
    );


-- Pedido generado desde Quote 6
MERGE INTO orders (
    id,
    order_number,
    client_id,
    quote_id,
    seller_id,
    status,
    order_date,
    expected_delivery_date,
    delivery_date,
    subtotal,
    discount,
    tax_rate,
    tax_amount,
    total,
    notes,
    active,
    created_at,
    updated_at
    )
    KEY(id)
    VALUES (
    2,
    'O-2026-000002',
    6,
    6,
    3,
    'READY',
    DATE '2026-09-29',
    DATE '2026-10-08',
    NULL,
    275.00,
    0.00,
    21.00,
    57.75,
    332.75,
    'Pedido listo para recoger.',
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
    );


-- Pedido directo sin presupuesto previo
MERGE INTO orders (
    id,
    order_number,
    client_id,
    quote_id,
    seller_id,
    status,
    order_date,
    expected_delivery_date,
    delivery_date,
    subtotal,
    discount,
    tax_rate,
    tax_amount,
    total,
    notes,
    active,
    created_at,
    updated_at
    )
    KEY(id)
    VALUES (
    3,
    'O-2026-000003',
    1,
    NULL,
    2,
    'CONFIRMED',
    DATE '2026-10-05',
    DATE '2026-10-17',
    NULL,
    120.00,
    0.00,
    21.00,
    25.20,
    145.20,
    'Pedido directo de gafas de sol.',
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
    );


-- Pedido entregado
MERGE INTO orders (
    id,
    order_number,
    client_id,
    quote_id,
    seller_id,
    status,
    order_date,
    expected_delivery_date,
    delivery_date,
    subtotal,
    discount,
    tax_rate,
    tax_amount,
    total,
    notes,
    active,
    created_at,
    updated_at
    )
    KEY(id)
    VALUES (
    4,
    'O-2026-000004',
    2,
    NULL,
    2,
    'DELIVERED',
    DATE '2026-09-15',
    DATE '2026-09-25',
    DATE '2026-09-24',
    150.00,
    10.00,
    21.00,
    29.40,
    169.40,
    'Pedido entregado correctamente.',
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
    );