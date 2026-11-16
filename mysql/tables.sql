CREATE DATABASE IF NOT EXISTS autos_colombia
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE autos_colombia;

CREATE TABLE IF NOT EXISTS usuarios (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tipo_documento VARCHAR(10) NOT NULL,
    numero_documento VARCHAR(25) NOT NULL UNIQUE,
    nombres VARCHAR(100) NOT NULL,
    telefono VARCHAR(30),
    correo VARCHAR(120),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS cuentas_personal (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nombre_usuario VARCHAR(50) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    rol ENUM('ADMINISTRADOR', 'OPERARIO') NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_cuenta_personal_usuario
        UNIQUE (nombre_usuario)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS vehiculos (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    placa VARCHAR(10) NOT NULL UNIQUE,
    usuario_id BIGINT NULL,
    propietario VARCHAR(100) NOT NULL,
    telefono VARCHAR(30),
    tipo VARCHAR(40),
    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_vehiculo_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuarios(id)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS celdas (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    codigo VARCHAR(15) NOT NULL UNIQUE,
    tipo VARCHAR(30) NOT NULL,
    estado ENUM(
        'DISPONIBLE',
        'OCUPADA',
        'MANTENIMIENTO'
    ) NOT NULL DEFAULT 'DISPONIBLE'
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS estancias (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    vehiculo_id BIGINT NOT NULL,
    celda_id BIGINT NOT NULL,
    entrada DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    salida DATETIME NULL,
    registrado_por BIGINT NULL,

    CONSTRAINT fk_estancia_vehiculo
        FOREIGN KEY (vehiculo_id)
        REFERENCES vehiculos(id),

    CONSTRAINT fk_estancia_celda
        FOREIGN KEY (celda_id)
        REFERENCES celdas(id),

    CONSTRAINT fk_estancia_usuario
        FOREIGN KEY (registrado_por)
        REFERENCES usuarios(id),

    INDEX ix_estancia_vehiculo_salida (vehiculo_id, salida),
    INDEX ix_estancia_entrada (entrada),
    INDEX ix_estancia_celda_salida (celda_id, salida)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS pagos (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    estancia_id BIGINT NOT NULL UNIQUE,
    usuario_id BIGINT NULL,
    valor DECIMAL(12,2) NOT NULL,
    fecha_pago DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    metodo ENUM(
        'EFECTIVO',
        'TARJETA',
        'TRANSFERENCIA'
    ) NOT NULL,

    estado ENUM(
        'PENDIENTE',
        'PAGADO',
        'ANULADO'
    ) NOT NULL DEFAULT 'PAGADO',

    referencia VARCHAR(80),

    CONSTRAINT fk_pago_estancia
        FOREIGN KEY (estancia_id)
        REFERENCES estancias(id),

    CONSTRAINT fk_pago_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuarios(id),

    CONSTRAINT ck_pago_valor CHECK (valor > 0),

    INDEX ix_pago_fecha (fecha_pago),
    INDEX ix_pago_usuario (usuario_id),
    INDEX ix_pago_estado (estado)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS membresias_mensuales (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    usuario_id BIGINT NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE NULL,
    estado ENUM('ACTIVA', 'FINALIZADA') NOT NULL DEFAULT 'ACTIVA',

    CONSTRAINT fk_membresia_mensual_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuarios(id),

    CONSTRAINT uq_membresia_usuario_inicio
        UNIQUE (usuario_id, fecha_inicio),

    INDEX ix_membresia_usuario_vigencia (usuario_id, fecha_inicio, fecha_fin)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS pagos_mensuales (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    usuario_id BIGINT NOT NULL,
    periodo CHAR(7) NOT NULL COMMENT 'Formato YYYY-MM',
    valor DECIMAL(12,2) NOT NULL,
    fecha_pago DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    metodo ENUM('EFECTIVO', 'TARJETA', 'TRANSFERENCIA') NOT NULL,
    estado ENUM('PAGADO', 'ANULADO') NOT NULL DEFAULT 'PAGADO',
    referencia VARCHAR(80),

    CONSTRAINT fk_pago_mensual_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuarios(id),

    CONSTRAINT uq_pago_mensual_usuario_periodo
        UNIQUE (usuario_id, periodo),

    CONSTRAINT ck_pago_mensual_valor
        CHECK (valor > 0),

    INDEX ix_pago_mensual_fecha (fecha_pago),
    INDEX ix_pago_mensual_periodo (periodo)
) ENGINE = InnoDB;

INSERT INTO celdas (codigo, tipo, estado)
VALUES
    ('CA-01', 'CARRO', 'DISPONIBLE'),
    ('CA-02', 'CARRO', 'DISPONIBLE'),
    ('CA-03', 'CARRO', 'DISPONIBLE'),
    ('CA-04', 'CARRO', 'DISPONIBLE'),
    ('CA-05', 'CARRO', 'DISPONIBLE'),
    ('CA-06', 'CARRO', 'DISPONIBLE'),
    ('CA-07', 'CARRO', 'DISPONIBLE'),
    ('CA-08', 'CARRO', 'DISPONIBLE'),
    ('CA-09', 'CARRO', 'DISPONIBLE'),
    ('CA-10', 'CARRO', 'DISPONIBLE'),

    ('MO-01', 'MOTOCICLETA', 'DISPONIBLE'),
    ('MO-02', 'MOTOCICLETA', 'DISPONIBLE'),
    ('MO-03', 'MOTOCICLETA', 'DISPONIBLE'),
    ('MO-04', 'MOTOCICLETA', 'DISPONIBLE'),
    ('MO-05', 'MOTOCICLETA', 'DISPONIBLE'),
    ('MO-06', 'MOTOCICLETA', 'DISPONIBLE'),
    ('MO-07', 'MOTOCICLETA', 'DISPONIBLE'),
    ('MO-08', 'MOTOCICLETA', 'DISPONIBLE'),
    ('MO-09', 'MOTOCICLETA', 'DISPONIBLE'),
    ('MO-10', 'MOTOCICLETA', 'DISPONIBLE'),

    ('CM-01', 'CAMION', 'DISPONIBLE'),
    ('CM-02', 'CAMION', 'DISPONIBLE'),
    ('CM-03', 'CAMION', 'DISPONIBLE'),
    ('CM-04', 'CAMION', 'DISPONIBLE'),
    ('CM-05', 'CAMION', 'DISPONIBLE'),
    ('CM-06', 'CAMION', 'DISPONIBLE'),
    ('CM-07', 'CAMION', 'DISPONIBLE'),
    ('CM-08', 'CAMION', 'DISPONIBLE'),
    ('CM-09', 'CAMION', 'DISPONIBLE'),
    ('CM-10', 'CAMION', 'DISPONIBLE')
ON DUPLICATE KEY UPDATE
    codigo = codigo;

SHOW TABLES;

SELECT
    tipo,
    COUNT(*) AS total_celdas,
    SUM(estado = 'DISPONIBLE') AS disponibles,
    SUM(estado = 'OCUPADA') AS ocupadas,
    SUM(estado = 'MANTENIMIENTO') AS mantenimiento
FROM celdas
GROUP BY tipo
ORDER BY FIELD(tipo, 'CARRO', 'MOTOCICLETA', 'CAMION');

SELECT
    p.id AS pago_id,
    p.estancia_id,
    v.placa,
    e.entrada,
    e.salida,
    p.valor,
    p.metodo,
    p.estado,
    p.fecha_pago
FROM pagos p
INNER JOIN estancias e
    ON e.id = p.estancia_id
INNER JOIN vehiculos v
    ON v.id = e.vehiculo_id
ORDER BY p.fecha_pago DESC;