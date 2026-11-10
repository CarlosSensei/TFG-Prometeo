-- =========================================================
-- OPTIFLOW CRM
-- DATOS DEMO PARA BACKEND Y FRONTEND
-- H2 DATABASE (SOLO PERFIL DE DESARROLLO)
-- =========================================================


-- =========================================================
-- USERS
-- =========================================================

-- Credenciales de demostración: nunca usar en producción.
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
    '$2a$10$Wp.FxiBkFVGqrHWceLCqLebmS1OU1r89uaTlFQs3XxZrymEX0iZXm',
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
    'Susana',
    'Roldan',
    'susana@optiflow.com',
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
    surname,
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
    surname,
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
    surname,
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
    surname,
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
    '33445566R',
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
    surname,
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
    surname,
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
    '66778899D',
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
-- INVENTORY ITEMS
-- =========================================================

MERGE INTO inventory_items (
    id,
    sku,
    name,
    type,
    brand,
    description,
    sale_price,
    tax_rate,
    stock,
    active,
    created_at,
    updated_at
    )
    KEY(id)
    VALUES (
    1,
    'FRAME-RB-001',
    'Ray-Ban RX5228',
    'FRAME',
    'Ray-Ban',
    'Montura graduada acetato negro',
    135.00,
    21.00,
    8,
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
    );

MERGE INTO inventory_items (
    id,
    sku,
    name,
    type,
    brand,
    description,
    sale_price,
    tax_rate,
    stock,
    active,
    created_at,
    updated_at
    )
    KEY(id)
    VALUES (
    2,
    'FRAME-VO-001',
    'Vogue VO5276',
    'FRAME',
    'Vogue',
    'Montura graduada mujer',
    110.00,
    21.00,
    5,
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
    );

MERGE INTO inventory_items (
    id,
    sku,
    name,
    type,
    brand,
    description,
    sale_price,
    tax_rate,
    stock,
    active,
    created_at,
    updated_at
    )
    KEY(id)
    VALUES (
    3,
    'LENS-PROG-001',
    'Lente progresiva estándar',
    'LENS',
    'OptiFlow',
    'Lente progresiva orgánica con antirreflejante',
    120.00,
    21.00,
    30,
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
    );

MERGE INTO inventory_items (
    id,
    sku,
    name,
    type,
    brand,
    description,
    sale_price,
    tax_rate,
    stock,
    active,
    created_at,
    updated_at
    )
    KEY(id)
    VALUES (
    4,
    'LENS-MONO-001',
    'Lente monofocal estándar',
    'LENS',
    'OptiFlow',
    'Lente monofocal orgánica',
    65.00,
    21.00,
    40,
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
    );

MERGE INTO inventory_items (
    id,
    sku,
    name,
    type,
    brand,
    description,
    sale_price,
    tax_rate,
    stock,
    active,
    created_at,
    updated_at
    )
    KEY(id)
    VALUES (
    5,
    'SUN-RB-001',
    'Ray-Ban Aviator',
    'SUNGLASSES',
    'Ray-Ban',
    'Gafa de sol clásica',
    155.00,
    21.00,
    6,
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
    );

MERGE INTO inventory_items (
    id,
    sku,
    name,
    type,
    brand,
    description,
    sale_price,
    tax_rate,
    stock,
    active,
    created_at,
    updated_at
    )
    KEY(id)
    VALUES (
    6,
    'ACC-CASE-001',
    'Estuche premium',
    'ACCESSORY',
    'OptiFlow',
    'Estuche rígido para gafas',
    15.00,
    21.00,
    25,
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
    );

MERGE INTO inventory_items (
    id,
    sku,
    name,
    type,
    brand,
    description,
    sale_price,
    tax_rate,
    stock,
    active,
    created_at,
    updated_at
    )
    KEY(id)
    VALUES (
    7,
    'SERVICE-ASSEMBLY',
    'Montaje y ajuste',
    'SERVICE',
    'OptiFlow',
    'Servicio de montaje y ajuste de gafas',
    30.00,
    21.00,
    0,
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


-- =========================================================
-- QUOTE ITEMS
-- Los importes de las líneas reflejan el precio pactado para
-- cada presupuesto (el catálogo puede tener otro precio actual).
-- subtotal de cabecera = bruto; discount = descuento global;
-- subtotal de línea = neto, total de línea = neto + IVA.
-- =========================================================
MERGE INTO quote_items (id, quote_id, inventory_item_id, description, quantity, unit_price, discount, tax_rate, subtotal, tax_amount, total) KEY(id) VALUES
(1, 1, 1, 'Pack montura y lentes progresivas', 1, 350.00, 0.00, 21.00, 350.00, 73.50, 423.50),
(2, 2, 2, 'Pack montura y lentes monofocales', 1, 220.00, 20.00, 21.00, 200.00, 42.00, 242.00),
(3, 3, 4, 'Pack óptico monofocal', 1, 180.00, 0.00, 21.00, 180.00, 37.80, 217.80),
(4, 4, 3, 'Pack óptico progresivo premium', 1, 500.00, 50.00, 21.00, 450.00, 94.50, 544.50),
(5, 5, 1, 'Pack óptico presupuestado', 1, 300.00, 25.00, 21.00, 275.00, 57.75, 332.75),
(6, 6, 3, 'Pack óptico progresivo', 1, 275.00, 0.00, 21.00, 275.00, 57.75, 332.75);

-- =========================================================
-- ORDER ITEMS
-- =========================================================
MERGE INTO order_items (id, order_id, inventory_item_id, description, quantity, unit_price, discount, tax_rate, subtotal, tax_amount, total) KEY(id) VALUES
(1, 1, 3, 'Pack óptico progresivo premium', 1, 500.00, 50.00, 21.00, 450.00, 94.50, 544.50),
(2, 2, 3, 'Pack óptico progresivo', 1, 275.00, 0.00, 21.00, 275.00, 57.75, 332.75),
(3, 3, 5, 'Gafas de sol', 1, 120.00, 0.00, 21.00, 120.00, 25.20, 145.20),
(4, 4, 2, 'Pack de gafas graduadas', 1, 150.00, 10.00, 21.00, 140.00, 29.40, 169.40);

-- =========================================================
-- FOLLOW UPS
-- Incluye pendiente futuro, completado y vencido ya notificado.
-- El scheduler no debe duplicar el tercero.
-- =========================================================
MERGE INTO follow_ups (id, quote_id, type, status, scheduled_at, completed_at, notes, result, automated, active, created_at, updated_at, due_event_published_at) KEY(id) VALUES
(1, 2, 'CALL', 'PENDING', DATEADD('DAY', 2, CURRENT_TIMESTAMP), NULL, 'Llamar para conocer decisión del presupuesto.', NULL, FALSE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
(2, 3, 'EMAIL', 'COMPLETED', DATE '2026-10-05', TIMESTAMP '2026-10-05 11:30:00', 'Confirmación de aceptación.', 'Cliente acepta presupuesto.', FALSE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
(3, 5, 'CALL', 'PENDING', DATE '2026-10-20', NULL, 'Seguimiento de presupuesto rechazado.', NULL, TRUE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- =========================================================
-- 20 CLIENTES ADICIONALES (IDS 7-26)
-- =========================================================
MERGE INTO clients (id, name, surname, last_surname, dni, phone_number, email, birth_date, address, city, postal_code, notes, active, created_at, updated_at) KEY(id) VALUES
(7, 'Marta', 'Soler', 'Costa', '30255433Z', '620164199', 'cliente07@demo.optiflow.test', DATE '1982-10-09', 'Carrer Demo 49', 'Mataró', '08301', 'Prefiere avisos por WhatsApp', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(8, 'Pau', 'Vidal', 'Roca', '30263352K', '620187656', 'cliente08@demo.optiflow.test', DATE '1989-01-14', 'Carrer Demo 56', 'Badalona', '08911', 'Cliente recurrente', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(9, 'Núria', 'Casals', 'Ferrer', '30271271M', '620211113', 'cliente09@demo.optiflow.test', DATE '1996-04-19', 'Carrer Demo 63', 'Sabadell', '08201', 'Busca progresivos', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(10, 'Albert', 'Rovira', 'Pons', '30279190N', '620234570', 'cliente10@demo.optiflow.test', DATE '2003-07-24', 'Carrer Demo 70', 'Terrassa', '08221', 'Solicita monturas ligeras', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(11, 'Gemma', 'Torres', 'Puig', '30287109L', '620258027', 'cliente11@demo.optiflow.test', DATE '1974-10-02', 'Carrer Demo 77', 'Barcelona', '08018', 'Revisión anual', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(12, 'Jordi', 'Font', 'Mas', '30295028A', '620281484', 'cliente12@demo.optiflow.test', DATE '1981-01-07', 'Carrer Demo 84', 'Granollers', '08401', 'Lentes de contacto', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(13, 'Laia', 'Bosch', 'Serra', '30302947X', '620304941', 'cliente13@demo.optiflow.test', DATE '1988-04-12', 'Carrer Demo 91', 'Vilassar de Mar', '08340', 'Interesada en gafas de sol', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(14, 'Oriol', 'Pérez', 'Ribas', '30310866V', '620328398', 'cliente14@demo.optiflow.test', DATE '1995-07-17', 'Carrer Demo 98', 'Premià de Mar', '08330', 'Primera visita', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(15, 'Clara', 'Martí', 'Sanz', '30318785R', '620351855', 'cliente15@demo.optiflow.test', DATE '2002-10-22', 'Carrer Demo 105', 'Barcelona', '08012', 'Descuento fidelidad', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(16, 'Roger', 'Costa', 'Gil', '30326704P', '620375312', 'cliente16@demo.optiflow.test', DATE '1973-01-27', 'Carrer Demo 112', 'Mataró', '08302', 'Busca montura resistente', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(17, 'Aina', 'López', 'Navarro', '30334623S', '620398769', 'cliente17@demo.optiflow.test', DATE '1980-04-05', 'Carrer Demo 119', 'El Masnou', '08320', 'Consulta de lentes monofocales', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(18, 'Pol', 'Giménez', 'Roig', '30342542E', '620422226', 'cliente18@demo.optiflow.test', DATE '1987-07-10', 'Carrer Demo 126', 'Barcelona', '08015', 'Seguimiento telefónico', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(19, 'Sílvia', 'Rius', 'Dalmau', '30350461Y', '620445683', 'cliente19@demo.optiflow.test', DATE '1994-10-15', 'Carrer Demo 133', 'Badalona', '08912', 'Cliente habitual', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(20, 'Arnau', 'Serrano', 'Valls', '30358380J', '620469140', 'cliente20@demo.optiflow.test', DATE '2001-01-20', 'Carrer Demo 140', 'Sabadell', '08203', 'Acostumbra a comprar accesorios', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(21, 'Irene', 'Molina', 'Soler', '30366299C', '620492597', 'cliente21@demo.optiflow.test', DATE '1972-04-25', 'Carrer Demo 147', 'Terrassa', '08224', 'Quiere presupuesto por email', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(22, 'Xavier', 'Serra', 'Ferrer', '30374218G', '620516054', 'cliente22@demo.optiflow.test', DATE '1979-07-03', 'Carrer Demo 154', 'Barcelona', '08029', 'Interesado en gama premium', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(23, 'Berta', 'Pujol', 'Ortiz', '30382137B', '620539511', 'cliente23@demo.optiflow.test', DATE '1986-10-08', 'Carrer Demo 161', 'Vilassar de Mar', '08340', 'Cambio de graduación', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(24, 'Hugo', 'Moreno', 'Castro', '30390056H', '620562968', 'cliente24@demo.optiflow.test', DATE '1993-01-13', 'Carrer Demo 168', 'Mataró', '08303', 'Interesado en lentillas', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(25, 'Eva', 'García', 'Roca', '30397975W', '620586425', 'cliente25@demo.optiflow.test', DATE '2000-04-18', 'Carrer Demo 175', 'Barcelona', '08024', 'Consulta de segunda montura', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(26, 'Daniel', 'Sánchez', 'Puig', '30405894D', '620609882', 'cliente26@demo.optiflow.test', DATE '1971-07-23', 'Carrer Demo 182', 'Granollers', '08402', 'Cliente nuevo', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);


-- =========================================================
-- 20 ARTÍCULOS ADICIONALES (IDS 8-27)
-- =========================================================
MERGE INTO inventory_items (id, sku, name, type, brand, description, sale_price, tax_rate, stock, active, created_at, updated_at) KEY(id) VALUES
(8, 'FRAME-OAK-002', 'Oakley OX8156', 'FRAME', 'Oakley', 'Montura deportiva ligera', 165, 21, 12, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(9, 'FRAME-PR-003', 'Prada PR 18WV', 'FRAME', 'Prada', 'Montura de acetato premium', 245, 21, 7, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(10, 'FRAME-PO-004', 'Polaroid PLD D424', 'FRAME', 'Polaroid', 'Montura urbana flexible', 89, 21, 15, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(11, 'FRAME-ES-005', 'Etnia Barcelona Brera', 'FRAME', 'Etnia Barcelona', 'Montura de diseño color carey', 195, 21, 9, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(12, 'FRAME-TM-006', 'Tommy Hilfiger TH 1856', 'FRAME', 'Tommy Hilfiger', 'Montura metálica fina', 125, 21, 11, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(13, 'LENS-BL-002', 'Monofocal Blue Control', 'LENS', 'OptiFlow', 'Lente con filtro selectivo', 95, 21, 45, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(14, 'LENS-PR-003', 'Progresiva Advanced', 'LENS', 'OptiFlow', 'Progresiva de campo amplio', 195, 21, 28, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(15, 'LENS-PH-004', 'Fotocromática Premium', 'LENS', 'OptiFlow', 'Lente fotocromática', 170, 21, 21, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(16, 'LENS-OC-005', 'Ocupacional Office', 'LENS', 'OptiFlow', 'Lente ocupacional para oficina', 145, 21, 18, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(17, 'SUN-PO-002', 'Polaroid PLD 6069', 'SUNGLASSES', 'Polaroid', 'Gafa solar polarizada', 79, 21, 18, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(18, 'SUN-OA-003', 'Oakley Holbrook', 'SUNGLASSES', 'Oakley', 'Gafa deportiva polarizada', 159, 21, 9, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(19, 'SUN-ET-004', 'Etnia Barcelona Sunset', 'SUNGLASSES', 'Etnia Barcelona', 'Gafa solar de diseño', 139, 21, 13, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(20, 'CONTACT-DAILY-001', 'Lentillas diarias 30u', 'CONTACT_LENS', 'OptiFlow', 'Caja de treinta lentes diarias', 32, 21, 60, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(21, 'CONTACT-MONTH-002', 'Lentillas mensuales 6u', 'CONTACT_LENS', 'OptiFlow', 'Caja de seis lentes mensuales', 48, 21, 40, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(22, 'CONTACT-TORIC-003', 'Lentillas tóricas 6u', 'CONTACT_LENS', 'OptiFlow', 'Caja lentes para astigmatismo', 69, 21, 25, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(23, 'ACC-CLEAN-002', 'Kit de limpieza', 'ACCESSORY', 'OptiFlow', 'Spray y bayeta de microfibra', 12, 21, 80, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(24, 'ACC-CHAIN-003', 'Cordón para gafas', 'ACCESSORY', 'OptiFlow', 'Cordón ajustable', 9, 21, 55, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(25, 'ACC-CASE-004', 'Funda plegable', 'ACCESSORY', 'OptiFlow', 'Funda compacta', 18, 21, 35, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(26, 'SERVICE-ADJUST-002', 'Ajuste de montura', 'SERVICE', 'OptiFlow', 'Servicio de ajuste mecánico', 15, 21, 0, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(27, 'SERVICE-VISION-003', 'Revisión visual básica', 'SERVICE', 'OptiFlow', 'Servicio de revisión visual', 25, 21, 0, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);


-- =========================================================
-- 20 PRESUPUESTOS ADICIONALES (IDS 7-26)
-- 7-16 CONVERTED Y VINCULADOS A PEDIDOS 5-14
-- =========================================================
MERGE INTO quotes (id, quote_number, client_id, seller_id, status, quote_date, expiration_date, subtotal, discount, tax_rate, tax_amount, total, notes, active, created_at, updated_at) KEY(id) VALUES
(7, 'Q-2026-000007', 7, 1, 'CONVERTED', DATE '2026-10-01', DATE '2026-10-31', 310.00, 5.00, 21, 64.05, 369.05, 'Demo comercial: converted (cliente #7)', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(8, 'Q-2026-000008', 8, 2, 'CONVERTED', DATE '2026-10-03', DATE '2026-11-02', 490.00, 0.00, 21, 102.90, 592.90, 'Demo comercial: converted (cliente #8)', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(9, 'Q-2026-000009', 9, 3, 'CONVERTED', DATE '2026-10-05', DATE '2026-11-04', 426.00, 0.00, 21, 89.46, 515.46, 'Demo comercial: converted (cliente #9)', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(10, 'Q-2026-000010', 10, 4, 'CONVERTED', DATE '2026-10-07', DATE '2026-11-06', 195.00, 0.00, 21, 40.95, 235.95, 'Demo comercial: converted (cliente #10)', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(11, 'Q-2026-000011', 11, 1, 'CONVERTED', DATE '2026-10-09', DATE '2026-11-08', 282.00, 0.00, 21, 59.22, 341.22, 'Demo comercial: converted (cliente #11)', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(12, 'Q-2026-000012', 12, 2, 'CONVERTED', DATE '2026-10-11', DATE '2026-11-10', 285.00, 5.00, 21, 58.80, 338.80, 'Demo comercial: converted (cliente #12)', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(13, 'Q-2026-000013', 13, 3, 'CONVERTED', DATE '2026-10-13', DATE '2026-11-12', 264.00, 0.00, 21, 55.44, 319.44, 'Demo comercial: converted (cliente #13)', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(14, 'Q-2026-000014', 14, 4, 'CONVERTED', DATE '2026-10-15', DATE '2026-11-14', 340.00, 0.00, 21, 71.40, 411.40, 'Demo comercial: converted (cliente #14)', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(15, 'Q-2026-000015', 15, 1, 'CONVERTED', DATE '2026-10-17', DATE '2026-11-16', 444.00, 0.00, 21, 93.24, 537.24, 'Demo comercial: converted (cliente #15)', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(16, 'Q-2026-000016', 16, 2, 'CONVERTED', DATE '2026-10-19', DATE '2026-11-18', 79.00, 0.00, 21, 16.59, 95.59, 'Demo comercial: converted (cliente #16)', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(17, 'Q-2026-000017', 17, 3, 'DRAFT', DATE '2026-10-21', DATE '2026-11-20', 333.00, 5.00, 21, 68.88, 396.88, 'Demo comercial: draft (cliente #17)', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(18, 'Q-2026-000018', 18, 4, 'SENT', DATE '2026-10-23', DATE '2026-11-22', 417.00, 0.00, 21, 87.57, 504.57, 'Demo comercial: sent (cliente #18)', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(19, 'Q-2026-000019', 19, 1, 'ACCEPTED', DATE '2026-10-25', DATE '2026-11-24', 197.00, 0.00, 21, 41.37, 238.37, 'Demo comercial: accepted (cliente #19)', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(20, 'Q-2026-000020', 20, 2, 'REJECTED', DATE '2026-10-27', DATE '2026-11-26', 96.00, 0.00, 21, 20.16, 116.16, 'Demo comercial: rejected (cliente #20)', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(21, 'Q-2026-000021', 21, 3, 'SENT', DATE '2026-10-29', DATE '2026-11-28', 296.00, 0.00, 21, 62.16, 358.16, 'Demo comercial: sent (cliente #21)', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(22, 'Q-2026-000022', 22, 4, 'DRAFT', DATE '2026-10-31', DATE '2026-11-30', 12.00, 5.00, 21, 1.47, 8.47, 'Demo comercial: draft (cliente #22)', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(23, 'Q-2026-000023', 23, 1, 'EXPIRED', DATE '2026-11-02', DATE '2026-12-02', 143.00, 0.00, 21, 30.03, 173.03, 'Demo comercial: expired (cliente #23)', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(24, 'Q-2026-000024', 24, 2, 'SENT', DATE '2026-11-04', DATE '2026-12-04', 54.00, 0.00, 21, 11.34, 65.34, 'Demo comercial: sent (cliente #24)', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(25, 'Q-2026-000025', 25, 3, 'ACCEPTED', DATE '2026-11-06', DATE '2026-12-06', 210.00, 0.00, 21, 44.10, 254.10, 'Demo comercial: accepted (cliente #25)', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(26, 'Q-2026-000026', 26, 4, 'SENT', DATE '2026-11-08', DATE '2026-12-08', 50.00, 0.00, 21, 10.50, 60.50, 'Demo comercial: sent (cliente #26)', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);


-- =========================================================
-- LÍNEAS DE LOS 20 PRESUPUESTOS ADICIONALES
-- =========================================================
MERGE INTO quote_items (id, quote_id, inventory_item_id, description, quantity, unit_price, discount, tax_rate, subtotal, tax_amount, total) KEY(id) VALUES
(7, 7, 8, 'Oakley OX8156', 1, 165, 5.00, 21, 160.00, 33.60, 193.60),
(8, 7, 16, 'Ocupacional Office', 1, 145, 0, 21, 145.00, 30.45, 175.45),
(9, 8, 9, 'Prada PR 18WV', 2, 245, 0, 21, 490.00, 102.90, 592.90),
(10, 9, 10, 'Polaroid PLD D424', 3, 89, 0, 21, 267.00, 56.07, 323.07),
(11, 9, 18, 'Oakley Holbrook', 1, 159, 0, 21, 159.00, 33.39, 192.39),
(12, 10, 11, 'Etnia Barcelona Brera', 1, 195, 0, 21, 195.00, 40.95, 235.95),
(13, 11, 12, 'Tommy Hilfiger TH 1856', 2, 125, 0, 21, 250.00, 52.50, 302.50),
(14, 11, 20, 'Lentillas diarias 30u', 1, 32, 0, 21, 32.00, 6.72, 38.72),
(15, 12, 13, 'Monofocal Blue Control', 3, 95, 5.00, 21, 280.00, 58.80, 338.80),
(16, 13, 14, 'Progresiva Advanced', 1, 195, 0, 21, 195.00, 40.95, 235.95),
(17, 13, 22, 'Lentillas tóricas 6u', 1, 69, 0, 21, 69.00, 14.49, 83.49),
(18, 14, 15, 'Fotocromática Premium', 2, 170, 0, 21, 340.00, 71.40, 411.40),
(19, 15, 16, 'Ocupacional Office', 3, 145, 0, 21, 435.00, 91.35, 526.35),
(20, 15, 24, 'Cordón para gafas', 1, 9, 0, 21, 9.00, 1.89, 10.89),
(21, 16, 17, 'Polaroid PLD 6069', 1, 79, 0, 21, 79.00, 16.59, 95.59),
(22, 17, 18, 'Oakley Holbrook', 2, 159, 5.00, 21, 313.00, 65.73, 378.73),
(23, 17, 26, 'Ajuste de montura', 1, 15, 0, 21, 15.00, 3.15, 18.15),
(24, 18, 19, 'Etnia Barcelona Sunset', 3, 139, 0, 21, 417.00, 87.57, 504.57),
(25, 19, 20, 'Lentillas diarias 30u', 1, 32, 0, 21, 32.00, 6.72, 38.72),
(26, 19, 8, 'Oakley OX8156', 1, 165, 0, 21, 165.00, 34.65, 199.65),
(27, 20, 21, 'Lentillas mensuales 6u', 2, 48, 0, 21, 96.00, 20.16, 116.16),
(28, 21, 22, 'Lentillas tóricas 6u', 3, 69, 0, 21, 207.00, 43.47, 250.47),
(29, 21, 10, 'Polaroid PLD D424', 1, 89, 0, 21, 89.00, 18.69, 107.69),
(30, 22, 23, 'Kit de limpieza', 1, 12, 5.00, 21, 7.00, 1.47, 8.47),
(31, 23, 24, 'Cordón para gafas', 2, 9, 0, 21, 18.00, 3.78, 21.78),
(32, 23, 12, 'Tommy Hilfiger TH 1856', 1, 125, 0, 21, 125.00, 26.25, 151.25),
(33, 24, 25, 'Funda plegable', 3, 18, 0, 21, 54.00, 11.34, 65.34),
(34, 25, 26, 'Ajuste de montura', 1, 15, 0, 21, 15.00, 3.15, 18.15),
(35, 25, 14, 'Progresiva Advanced', 1, 195, 0, 21, 195.00, 40.95, 235.95),
(36, 26, 27, 'Revisión visual básica', 2, 25, 0, 21, 50.00, 10.50, 60.50);


-- =========================================================
-- 20 PEDIDOS ADICIONALES (IDS 5-24)
-- 10 DESDE PRESUPUESTO + 10 DIRECTOS
-- =========================================================
MERGE INTO orders (id, order_number, client_id, quote_id, seller_id, status, order_date, expected_delivery_date, delivery_date, subtotal, discount, tax_rate, tax_amount, total, notes, active, created_at, updated_at) KEY(id) VALUES
(5, 'O-2026-000005', 7, 7, 1, 'PENDING', DATE '2026-10-02', DATE '2026-10-12', NULL, 310.00, 5.00, 21, 64.05, 369.05, 'Pedido demo: pending', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(6, 'O-2026-000006', 8, 8, 2, 'CONFIRMED', DATE '2026-10-04', DATE '2026-10-14', NULL, 490.00, 0.00, 21, 102.90, 592.90, 'Pedido demo: confirmed', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(7, 'O-2026-000007', 9, 9, 3, 'IN_PROGRESS', DATE '2026-10-06', DATE '2026-10-16', NULL, 426.00, 0.00, 21, 89.46, 515.46, 'Pedido demo: in_progress', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(8, 'O-2026-000008', 10, 10, 4, 'READY', DATE '2026-10-08', DATE '2026-10-18', NULL, 195.00, 0.00, 21, 40.95, 235.95, 'Pedido demo: ready', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(9, 'O-2026-000009', 11, 11, 1, 'DELIVERED', DATE '2026-10-10', DATE '2026-10-20', DATE '2026-10-19', 282.00, 0.00, 21, 59.22, 341.22, 'Pedido demo: delivered', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(10, 'O-2026-000010', 12, 12, 2, 'CANCELLED', DATE '2026-10-12', DATE '2026-10-22', NULL, 285.00, 5.00, 21, 58.80, 338.80, 'Pedido demo: cancelled', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(11, 'O-2026-000011', 13, 13, 3, 'PENDING', DATE '2026-10-14', DATE '2026-10-24', NULL, 264.00, 0.00, 21, 55.44, 319.44, 'Pedido demo: pending', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(12, 'O-2026-000012', 14, 14, 4, 'CONFIRMED', DATE '2026-10-16', DATE '2026-10-26', NULL, 340.00, 0.00, 21, 71.40, 411.40, 'Pedido demo: confirmed', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(13, 'O-2026-000013', 15, 15, 1, 'IN_PROGRESS', DATE '2026-10-18', DATE '2026-10-28', NULL, 444.00, 0.00, 21, 93.24, 537.24, 'Pedido demo: in_progress', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(14, 'O-2026-000014', 16, 16, 2, 'READY', DATE '2026-10-20', DATE '2026-10-30', NULL, 79.00, 0.00, 21, 16.59, 95.59, 'Pedido demo: ready', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(15, 'O-2026-000015', 17, NULL, 3, 'DELIVERED', DATE '2026-10-20', DATE '2026-10-30', DATE '2026-10-29', 96.00, 0.00, 21, 20.16, 116.16, 'Pedido demo: delivered', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(16, 'O-2026-000016', 18, NULL, 4, 'CANCELLED', DATE '2026-10-21', DATE '2026-10-31', NULL, 207.00, 0.00, 21, 43.47, 250.47, 'Pedido demo: cancelled', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(17, 'O-2026-000017', 19, NULL, 1, 'PENDING', DATE '2026-10-22', DATE '2026-11-01', NULL, 12.00, 0.00, 21, 2.52, 14.52, 'Pedido demo: pending', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(18, 'O-2026-000018', 20, NULL, 2, 'CONFIRMED', DATE '2026-10-23', DATE '2026-11-02', NULL, 18.00, 0.00, 21, 3.78, 21.78, 'Pedido demo: confirmed', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(19, 'O-2026-000019', 21, NULL, 3, 'IN_PROGRESS', DATE '2026-10-24', DATE '2026-11-03', NULL, 54.00, 0.00, 21, 11.34, 65.34, 'Pedido demo: in_progress', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(20, 'O-2026-000020', 22, NULL, 4, 'READY', DATE '2026-10-25', DATE '2026-11-04', NULL, 15.00, 0.00, 21, 3.15, 18.15, 'Pedido demo: ready', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(21, 'O-2026-000021', 23, NULL, 1, 'DELIVERED', DATE '2026-10-26', DATE '2026-11-05', DATE '2026-11-04', 50.00, 0.00, 21, 10.50, 60.50, 'Pedido demo: delivered', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(22, 'O-2026-000022', 24, NULL, 2, 'CANCELLED', DATE '2026-10-27', DATE '2026-11-06', NULL, 495.00, 0.00, 21, 103.95, 598.95, 'Pedido demo: cancelled', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(23, 'O-2026-000023', 25, NULL, 3, 'PENDING', DATE '2026-10-28', DATE '2026-11-07', NULL, 245.00, 0.00, 21, 51.45, 296.45, 'Pedido demo: pending', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(24, 'O-2026-000024', 26, NULL, 4, 'CONFIRMED', DATE '2026-10-29', DATE '2026-11-08', NULL, 178.00, 0.00, 21, 37.38, 215.38, 'Pedido demo: confirmed', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);


-- =========================================================
-- LÍNEAS DE LOS 20 PEDIDOS ADICIONALES
-- =========================================================
MERGE INTO order_items (id, order_id, inventory_item_id, description, quantity, unit_price, discount, tax_rate, subtotal, tax_amount, total) KEY(id) VALUES
(5, 5, 8, 'Oakley OX8156', 1, 165, 5.00, 21, 160.00, 33.60, 193.60),
(6, 5, 16, 'Ocupacional Office', 1, 145, 0, 21, 145.00, 30.45, 175.45),
(7, 6, 9, 'Prada PR 18WV', 2, 245, 0, 21, 490.00, 102.90, 592.90),
(8, 7, 10, 'Polaroid PLD D424', 3, 89, 0, 21, 267.00, 56.07, 323.07),
(9, 7, 18, 'Oakley Holbrook', 1, 159, 0, 21, 159.00, 33.39, 192.39),
(10, 8, 11, 'Etnia Barcelona Brera', 1, 195, 0, 21, 195.00, 40.95, 235.95),
(11, 9, 12, 'Tommy Hilfiger TH 1856', 2, 125, 0, 21, 250.00, 52.50, 302.50),
(12, 9, 20, 'Lentillas diarias 30u', 1, 32, 0, 21, 32.00, 6.72, 38.72),
(13, 10, 13, 'Monofocal Blue Control', 3, 95, 5.00, 21, 280.00, 58.80, 338.80),
(14, 11, 14, 'Progresiva Advanced', 1, 195, 0, 21, 195.00, 40.95, 235.95),
(15, 11, 22, 'Lentillas tóricas 6u', 1, 69, 0, 21, 69.00, 14.49, 83.49),
(16, 12, 15, 'Fotocromática Premium', 2, 170, 0, 21, 340.00, 71.40, 411.40),
(17, 13, 16, 'Ocupacional Office', 3, 145, 0, 21, 435.00, 91.35, 526.35),
(18, 13, 24, 'Cordón para gafas', 1, 9, 0, 21, 9.00, 1.89, 10.89),
(19, 14, 17, 'Polaroid PLD 6069', 1, 79, 0, 21, 79.00, 16.59, 95.59),
(20, 15, 21, 'Lentillas mensuales 6u', 2, 48, 0, 21, 96.00, 20.16, 116.16),
(21, 16, 22, 'Lentillas tóricas 6u', 3, 69, 0, 21, 207.00, 43.47, 250.47),
(22, 17, 23, 'Kit de limpieza', 1, 12, 0, 21, 12.00, 2.52, 14.52),
(23, 18, 24, 'Cordón para gafas', 2, 9, 0, 21, 18.00, 3.78, 21.78),
(24, 19, 25, 'Funda plegable', 3, 18, 0, 21, 54.00, 11.34, 65.34),
(25, 20, 26, 'Ajuste de montura', 1, 15, 0, 21, 15.00, 3.15, 18.15),
(26, 21, 27, 'Revisión visual básica', 2, 25, 0, 21, 50.00, 10.50, 60.50),
(27, 22, 8, 'Oakley OX8156', 3, 165, 0, 21, 495.00, 103.95, 598.95),
(28, 23, 9, 'Prada PR 18WV', 1, 245, 0, 21, 245.00, 51.45, 296.45),
(29, 24, 10, 'Polaroid PLD D424', 2, 89, 0, 21, 178.00, 37.38, 215.38);


-- =========================================================
-- 20 SEGUIMIENTOS ADICIONALES (IDS 4-23)
-- YA PUBLICADOS PARA EVITAR DUPLICACIÓN EN SCHEDULER
-- =========================================================
MERGE INTO follow_ups (id, quote_id, type, status, scheduled_at, completed_at, notes, result, automated, active, created_at, updated_at, due_event_published_at) KEY(id) VALUES
(4, 7, 'CALL', 'PENDING', TIMESTAMP '2026-10-09 10:30:00', NULL, 'Seguimiento comercial del presupuesto 7', NULL, FALSE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TIMESTAMP '2026-10-09 11:00:00'),
(5, 8, 'EMAIL', 'PENDING', TIMESTAMP '2026-10-10 10:30:00', NULL, 'Seguimiento comercial del presupuesto 8', NULL, TRUE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TIMESTAMP '2026-10-10 11:00:00'),
(6, 9, 'WHATSAPP', 'PENDING', TIMESTAMP '2026-10-11 10:30:00', NULL, 'Seguimiento comercial del presupuesto 9', NULL, FALSE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TIMESTAMP '2026-10-11 11:00:00'),
(7, 10, 'REMINDER', 'PENDING', TIMESTAMP '2026-10-12 10:30:00', NULL, 'Seguimiento comercial del presupuesto 10', NULL, TRUE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TIMESTAMP '2026-10-12 11:00:00'),
(8, 11, 'CALL', 'PENDING', TIMESTAMP '2026-10-13 10:30:00', NULL, 'Seguimiento comercial del presupuesto 11', NULL, FALSE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TIMESTAMP '2026-10-13 11:00:00'),
(9, 12, 'EMAIL', 'PENDING', TIMESTAMP '2026-10-14 10:30:00', NULL, 'Seguimiento comercial del presupuesto 12', NULL, TRUE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TIMESTAMP '2026-10-14 11:00:00'),
(10, 13, 'WHATSAPP', 'PENDING', TIMESTAMP '2026-10-15 10:30:00', NULL, 'Seguimiento comercial del presupuesto 13', NULL, FALSE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TIMESTAMP '2026-10-15 11:00:00'),
(11, 14, 'REMINDER', 'PENDING', TIMESTAMP '2026-10-16 10:30:00', NULL, 'Seguimiento comercial del presupuesto 14', NULL, TRUE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TIMESTAMP '2026-10-16 11:00:00'),
(12, 15, 'CALL', 'PENDING', TIMESTAMP '2026-10-17 10:30:00', NULL, 'Seguimiento comercial del presupuesto 15', NULL, FALSE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TIMESTAMP '2026-10-17 11:00:00'),
(13, 16, 'EMAIL', 'PENDING', TIMESTAMP '2026-10-18 10:30:00', NULL, 'Seguimiento comercial del presupuesto 16', NULL, TRUE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TIMESTAMP '2026-10-18 11:00:00'),
(14, 17, 'WHATSAPP', 'PENDING', TIMESTAMP '2026-10-19 10:30:00', NULL, 'Seguimiento comercial del presupuesto 17', NULL, FALSE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TIMESTAMP '2026-10-19 11:00:00'),
(15, 18, 'REMINDER', 'PENDING', TIMESTAMP '2026-10-20 10:30:00', NULL, 'Seguimiento comercial del presupuesto 18', NULL, TRUE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TIMESTAMP '2026-10-20 11:00:00'),
(16, 19, 'CALL', 'PENDING', TIMESTAMP '2026-10-21 10:30:00', NULL, 'Seguimiento comercial del presupuesto 19', NULL, FALSE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TIMESTAMP '2026-10-21 11:00:00'),
(17, 20, 'EMAIL', 'PENDING', TIMESTAMP '2026-10-22 10:30:00', NULL, 'Seguimiento comercial del presupuesto 20', NULL, TRUE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TIMESTAMP '2026-10-22 11:00:00'),
(18, 21, 'WHATSAPP', 'PENDING', TIMESTAMP '2026-10-23 10:30:00', NULL, 'Seguimiento comercial del presupuesto 21', NULL, FALSE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TIMESTAMP '2026-10-23 11:00:00'),
(19, 22, 'REMINDER', 'PENDING', TIMESTAMP '2026-10-24 10:30:00', NULL, 'Seguimiento comercial del presupuesto 22', NULL, TRUE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TIMESTAMP '2026-10-24 11:00:00'),
(20, 23, 'CALL', 'PENDING', TIMESTAMP '2026-10-25 10:30:00', NULL, 'Seguimiento comercial del presupuesto 23', NULL, FALSE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TIMESTAMP '2026-10-25 11:00:00'),
(21, 24, 'EMAIL', 'PENDING', TIMESTAMP '2026-10-26 10:30:00', NULL, 'Seguimiento comercial del presupuesto 24', NULL, TRUE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TIMESTAMP '2026-10-26 11:00:00'),
(22, 25, 'WHATSAPP', 'PENDING', TIMESTAMP '2026-10-27 10:30:00', NULL, 'Seguimiento comercial del presupuesto 25', NULL, FALSE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TIMESTAMP '2026-10-27 11:00:00'),
(23, 26, 'REMINDER', 'PENDING', TIMESTAMP '2026-10-28 10:30:00', NULL, 'Seguimiento comercial del presupuesto 26', NULL, TRUE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TIMESTAMP '2026-10-28 11:00:00');


-- =========================================================
-- NOTIFICACIONES POR DESTINATARIO
-- 1 EXISTENTE + 20 ADICIONALES; 5 PARA ADMIN CARLOS
-- =========================================================
MERGE INTO notifications (id, type, title, message, follow_up_id, quote_id, recipient_user_id, read, read_at, active, created_at) KEY(id) VALUES
(1, 'FOLLOW_UP_DUE', 'Seguimiento pendiente', 'El seguimiento 3 del presupuesto 5 está pendiente.', 3, 5, 2, FALSE, NULL, TRUE, CURRENT_TIMESTAMP),
(4, 'FOLLOW_UP_DUE', 'Seguimiento pendiente · Q-2026-000007', 'Contactar con cliente del presupuesto Q-2026-000007', 4, 7, 1, TRUE, TIMESTAMP '2026-10-09 12:00:00', TRUE, CURRENT_TIMESTAMP),
(5, 'FOLLOW_UP_DUE', 'Seguimiento pendiente · Q-2026-000008', 'Contactar con cliente del presupuesto Q-2026-000008', 5, 8, 2, FALSE, NULL, TRUE, CURRENT_TIMESTAMP),
(6, 'FOLLOW_UP_DUE', 'Seguimiento pendiente · Q-2026-000009', 'Contactar con cliente del presupuesto Q-2026-000009', 6, 9, 3, FALSE, NULL, TRUE, CURRENT_TIMESTAMP),
(7, 'FOLLOW_UP_DUE', 'Seguimiento pendiente · Q-2026-000010', 'Contactar con cliente del presupuesto Q-2026-000010', 7, 10, 4, TRUE, TIMESTAMP '2026-10-12 12:00:00', TRUE, CURRENT_TIMESTAMP),
(8, 'FOLLOW_UP_DUE', 'Seguimiento pendiente · Q-2026-000011', 'Contactar con cliente del presupuesto Q-2026-000011', 8, 11, 1, FALSE, NULL, TRUE, CURRENT_TIMESTAMP),
(9, 'FOLLOW_UP_DUE', 'Seguimiento pendiente · Q-2026-000012', 'Contactar con cliente del presupuesto Q-2026-000012', 9, 12, 2, FALSE, NULL, TRUE, CURRENT_TIMESTAMP),
(10, 'FOLLOW_UP_DUE', 'Seguimiento pendiente · Q-2026-000013', 'Contactar con cliente del presupuesto Q-2026-000013', 10, 13, 3, TRUE, TIMESTAMP '2026-10-15 12:00:00', TRUE, CURRENT_TIMESTAMP),
(11, 'FOLLOW_UP_DUE', 'Seguimiento pendiente · Q-2026-000014', 'Contactar con cliente del presupuesto Q-2026-000014', 11, 14, 4, FALSE, NULL, TRUE, CURRENT_TIMESTAMP),
(12, 'FOLLOW_UP_DUE', 'Seguimiento pendiente · Q-2026-000015', 'Contactar con cliente del presupuesto Q-2026-000015', 12, 15, 1, FALSE, NULL, TRUE, CURRENT_TIMESTAMP),
(13, 'FOLLOW_UP_DUE', 'Seguimiento pendiente · Q-2026-000016', 'Contactar con cliente del presupuesto Q-2026-000016', 13, 16, 2, TRUE, TIMESTAMP '2026-10-18 12:00:00', TRUE, CURRENT_TIMESTAMP),
(14, 'FOLLOW_UP_DUE', 'Seguimiento pendiente · Q-2026-000017', 'Contactar con cliente del presupuesto Q-2026-000017', 14, 17, 3, FALSE, NULL, TRUE, CURRENT_TIMESTAMP),
(15, 'FOLLOW_UP_DUE', 'Seguimiento pendiente · Q-2026-000018', 'Contactar con cliente del presupuesto Q-2026-000018', 15, 18, 4, FALSE, NULL, TRUE, CURRENT_TIMESTAMP),
(16, 'FOLLOW_UP_DUE', 'Seguimiento pendiente · Q-2026-000019', 'Contactar con cliente del presupuesto Q-2026-000019', 16, 19, 1, TRUE, TIMESTAMP '2026-10-21 12:00:00', TRUE, CURRENT_TIMESTAMP),
(17, 'FOLLOW_UP_DUE', 'Seguimiento pendiente · Q-2026-000020', 'Contactar con cliente del presupuesto Q-2026-000020', 17, 20, 2, FALSE, NULL, TRUE, CURRENT_TIMESTAMP),
(18, 'FOLLOW_UP_DUE', 'Seguimiento pendiente · Q-2026-000021', 'Contactar con cliente del presupuesto Q-2026-000021', 18, 21, 3, FALSE, NULL, TRUE, CURRENT_TIMESTAMP),
(19, 'FOLLOW_UP_DUE', 'Seguimiento pendiente · Q-2026-000022', 'Contactar con cliente del presupuesto Q-2026-000022', 19, 22, 4, TRUE, TIMESTAMP '2026-10-24 12:00:00', TRUE, CURRENT_TIMESTAMP),
(20, 'FOLLOW_UP_DUE', 'Seguimiento pendiente · Q-2026-000023', 'Contactar con cliente del presupuesto Q-2026-000023', 20, 23, 1, FALSE, NULL, TRUE, CURRENT_TIMESTAMP),
(21, 'FOLLOW_UP_DUE', 'Seguimiento pendiente · Q-2026-000024', 'Contactar con cliente del presupuesto Q-2026-000024', 21, 24, 2, FALSE, NULL, TRUE, CURRENT_TIMESTAMP),
(22, 'FOLLOW_UP_DUE', 'Seguimiento pendiente · Q-2026-000025', 'Contactar con cliente del presupuesto Q-2026-000025', 22, 25, 3, TRUE, TIMESTAMP '2026-10-27 12:00:00', TRUE, CURRENT_TIMESTAMP),
(23, 'FOLLOW_UP_DUE', 'Seguimiento pendiente · Q-2026-000026', 'Contactar con cliente del presupuesto Q-2026-000026', 23, 26, 4, FALSE, NULL, TRUE, CURRENT_TIMESTAMP);


-- =========================================================
-- NOTA DE AUDITORÍA
-- Los pedidos históricos demo no han ejecutado el workflow real de
-- confirmación. Sus existencias representan un snapshot de demostración.
-- No se insertan movimientos de stock inventados en stock_movements.
-- =========================================================


-- =========================================================
-- IDENTITY SEQUENCES H2
-- Evita colisiones entre altas JPA y registros demo.
-- =========================================================
ALTER TABLE users ALTER COLUMN id RESTART WITH 1001;
ALTER TABLE clients ALTER COLUMN id RESTART WITH 1001;
ALTER TABLE inventory_items ALTER COLUMN id RESTART WITH 1001;
ALTER TABLE quotes ALTER COLUMN id RESTART WITH 1001;
ALTER TABLE quote_items ALTER COLUMN id RESTART WITH 1001;
ALTER TABLE orders ALTER COLUMN id RESTART WITH 1001;
ALTER TABLE order_items ALTER COLUMN id RESTART WITH 1001;
ALTER TABLE follow_ups ALTER COLUMN id RESTART WITH 1001;
ALTER TABLE notifications ALTER COLUMN id RESTART WITH 1001;
-- stock_movements: si existe, utiliza su generador de identidad propio.
