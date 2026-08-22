USE master
GO

IF DB_ID('BibliotecaUTNG') IS NOT NULL
BEGIN
    ALTER DATABASE BibliotecaUTNG SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE BibliotecaUTNG;
END
GO

CREATE DATABASE BibliotecaUTNG
COLLATE Modern_Spanish_CI_AS;
GO

USE BibliotecaUTNG;
GO


CREATE TABLE Rol (
    idRol INT IDENTITY(1,1) PRIMARY KEY,
    nombre VARCHAR(30) NOT NULL UNIQUE,
    descripcion VARCHAR(255),

    maxMateriales INT NULL,

    -- Cantidad de plazo. Su significado depende de unidadPlazo.
    diasPrestamo INT NULL,

    /* Cómo se cuenta el plazo del préstamo:
         HABILES    -> solo lunes a viernes (la biblioteca cierra en fin de semana)
         NATURALES  -> días corridos, incluye sábado y domingo
         MESES      -> meses de calendario, para estancias fuera del plantel
       La fecha límite la calcula dbo.FN_CalcularFechaLimite. */
    unidadPlazo VARCHAR(10) NOT NULL DEFAULT 'HABILES',

    estado BIT NOT NULL DEFAULT 1,

    CONSTRAINT CK_Rol_UnidadPlazo
        CHECK (unidadPlazo IN ('HABILES', 'NATURALES', 'MESES')),

    CONSTRAINT CK_Rol_Plazo
        CHECK (diasPrestamo IS NULL OR diasPrestamo >= 0),

    CONSTRAINT CK_Rol_MaxMateriales
        CHECK (maxMateriales IS NULL OR maxMateriales >= 0)
);
GO


CREATE TABLE Biblioteca (
    idBiblioteca INT IDENTITY(1,1) PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    ubicacion VARCHAR(150),

    estado BIT NOT NULL DEFAULT 1
);
GO


CREATE TABLE Usuario (
    idUsuario INT IDENTITY(1,1) PRIMARY KEY,

    numeroControl VARCHAR(30) NULL,
    nombre VARCHAR(120) NOT NULL,
    correo VARCHAR(150) NULL,
    telefono VARCHAR(20) NULL,

    usuario VARCHAR(50) NULL,
    passwordHash VARCHAR(255) NULL,

    idRol INT NOT NULL,

    estado BIT NOT NULL DEFAULT 1,
    fechaRegistro DATETIME NOT NULL DEFAULT GETDATE(),

    -- CAMBIO: ya no son UNIQUE inline (ver indices filtrados mas abajo)

    CONSTRAINT FK_Usuario_Rol
        FOREIGN KEY (idRol)
        REFERENCES Rol(idRol)
);
GO

-- CAMBIO: indices unicos FILTRADOS: permiten muchas filas en NULL,
-- pero siguen impidiendo dos filas con el MISMO valor no nulo.
CREATE UNIQUE INDEX UQ_Usuario_NumeroControl_Filtrado
    ON Usuario(numeroControl)
    WHERE numeroControl IS NOT NULL;
GO

CREATE UNIQUE INDEX UQ_Usuario_Correo_Filtrado
    ON Usuario(correo)
    WHERE correo IS NOT NULL;
GO

CREATE UNIQUE INDEX UQ_Usuario_Usuario_Filtrado
    ON Usuario(usuario)
    WHERE usuario IS NOT NULL;
GO

-- CAMBIO: indice para acelerar joins/consultas por rol
CREATE INDEX IX_Usuario_idRol ON Usuario(idRol);
GO

CREATE TABLE TipoMaterial (
    idTipoMaterial INT IDENTITY(1,1) PRIMARY KEY,

    nombre VARCHAR(50) NOT NULL UNIQUE,
    descripcion VARCHAR(255),

    -- Determina si los ejemplares de este tipo
    -- pueden formar parte de un préstamo.
    esPrestable BIT NOT NULL DEFAULT 1,

    estado BIT NOT NULL DEFAULT 1
);
GO

CREATE TABLE Editorial (
    idEditorial INT IDENTITY(1,1) PRIMARY KEY,

    nombre VARCHAR(150) NOT NULL UNIQUE,

    estado BIT NOT NULL DEFAULT 1
);
GO

CREATE TABLE Autor (
    idAutor INT IDENTITY(1,1) PRIMARY KEY,

    nombre VARCHAR(150) NOT NULL
);
GO

CREATE TABLE Material (
    idMaterial INT IDENTITY(1,1) PRIMARY KEY,

    titulo VARCHAR(300) NOT NULL,
    isbn VARCHAR(30) NULL,

    anioPublicacion SMALLINT NULL,
    clasificacion VARCHAR(100) NULL,

    idEditorial INT NULL,
    idTipoMaterial INT NOT NULL,

    estado VARCHAR(30) NOT NULL DEFAULT 'Activo',

    fechaRegistro DATETIME NOT NULL DEFAULT GETDATE(),

    CONSTRAINT FK_Material_Editorial
        FOREIGN KEY (idEditorial)
        REFERENCES Editorial(idEditorial),

    CONSTRAINT FK_Material_TipoMaterial
        FOREIGN KEY (idTipoMaterial)
        REFERENCES TipoMaterial(idTipoMaterial),

    -- CAMBIO: dominio de valores permitidos para estado
    CONSTRAINT CK_Material_Estado
        CHECK (estado IN ('Activo','Baja','Extraviado'))
);
GO

CREATE INDEX IX_Material_idEditorial ON Material(idEditorial);
CREATE INDEX IX_Material_idTipoMaterial ON Material(idTipoMaterial);
GO

CREATE TABLE MaterialAutor (
    idMaterial INT NOT NULL,
    idAutor INT NOT NULL,

    CONSTRAINT PK_MaterialAutor
        PRIMARY KEY (idMaterial, idAutor),

    CONSTRAINT FK_MaterialAutor_Material
        FOREIGN KEY (idMaterial)
        REFERENCES Material(idMaterial)
        ON DELETE CASCADE,

    CONSTRAINT FK_MaterialAutor_Autor
        FOREIGN KEY (idAutor)
        REFERENCES Autor(idAutor)
        ON DELETE CASCADE
);
GO

-- CAMBIO: indice para resolver "materiales de un autor" sin recorrer la PK completa
CREATE INDEX IX_MaterialAutor_idAutor ON MaterialAutor(idAutor);
GO

CREATE TABLE Ejemplar (
    idEjemplar INT IDENTITY(1,1) PRIMARY KEY,

    idMaterial INT NOT NULL,
    idBiblioteca INT NOT NULL,

    -- Código institucional propio de cada ejemplar
    codigoEjemplar VARCHAR(50) NOT NULL,

    numeroAdquisicion VARCHAR(50) NULL,

    fechaIngreso DATE NULL,

    estado VARCHAR(30) NOT NULL DEFAULT 'Disponible',

    CONSTRAINT UQ_Ejemplar_Codigo
        UNIQUE (codigoEjemplar),

    CONSTRAINT FK_Ejemplar_Material
        FOREIGN KEY (idMaterial)
        REFERENCES Material(idMaterial),

    CONSTRAINT FK_Ejemplar_Biblioteca
        FOREIGN KEY (idBiblioteca)
        REFERENCES Biblioteca(idBiblioteca),

    -- CAMBIO: dominio de valores permitidos para estado
    CONSTRAINT CK_Ejemplar_Estado
        CHECK (estado IN ('Disponible','Prestado','Baja','Extraviado','EnReparacion'))
);
GO

CREATE INDEX IX_Ejemplar_idMaterial ON Ejemplar(idMaterial);
CREATE INDEX IX_Ejemplar_idBiblioteca ON Ejemplar(idBiblioteca);
GO

CREATE TABLE Adquisicion (
    idAdquisicion INT IDENTITY(1,1) PRIMARY KEY,

    fechaAdquisicion DATE NOT NULL,

    tipoAdquisicion VARCHAR(50) NOT NULL,

    proveedor VARCHAR(150) NULL,

    idResponsable INT NULL,

    observaciones VARCHAR(MAX) NULL,

    CONSTRAINT FK_Adquisicion_Responsable
        FOREIGN KEY (idResponsable)
        REFERENCES Usuario(idUsuario),

    -- CAMBIO: dominio de valores permitidos para tipoAdquisicion
    CONSTRAINT CK_Adquisicion_Tipo
        CHECK (tipoAdquisicion IN ('Compra','Donacion','Canje'))
);
GO

CREATE INDEX IX_Adquisicion_idResponsable ON Adquisicion(idResponsable);
GO

CREATE TABLE DetalleAdquisicion (
    idDetalleAdquisicion INT IDENTITY(1,1) PRIMARY KEY,

    idAdquisicion INT NOT NULL,
    idMaterial INT NOT NULL,

    cantidad INT NOT NULL DEFAULT 1,

    CONSTRAINT CK_DetalleAdquisicion_Cantidad
        CHECK (cantidad > 0),

    CONSTRAINT FK_DetalleAdquisicion_Adquisicion
        FOREIGN KEY (idAdquisicion)
        REFERENCES Adquisicion(idAdquisicion)
        ON DELETE CASCADE,

    CONSTRAINT FK_DetalleAdquisicion_Material
        FOREIGN KEY (idMaterial)
        REFERENCES Material(idMaterial)
);
GO

CREATE INDEX IX_DetalleAdquisicion_idMaterial ON DetalleAdquisicion(idMaterial);
GO

CREATE TABLE SolicitudAdquisicion (
    idSolicitud INT IDENTITY(1,1) PRIMARY KEY,

    idUsuario INT NOT NULL,

    titulo VARCHAR(300) NOT NULL,
    autor VARCHAR(150) NULL,
    editorial VARCHAR(150) NULL,
    isbn VARCHAR(30) NULL,

    descripcion VARCHAR(MAX) NULL,

    fechaSolicitud DATETIME NOT NULL DEFAULT GETDATE(),

    estado VARCHAR(30) NOT NULL DEFAULT 'Pendiente',

    respuesta VARCHAR(MAX) NULL,

    idAdministrador INT NULL,
    fechaRespuesta DATETIME NULL,

    CONSTRAINT FK_Solicitud_Usuario
        FOREIGN KEY (idUsuario)
        REFERENCES Usuario(idUsuario),

    CONSTRAINT FK_Solicitud_Administrador
        FOREIGN KEY (idAdministrador)
        REFERENCES Usuario(idUsuario),

    -- CAMBIO: dominio de valores permitidos para estado
    CONSTRAINT CK_Solicitud_Estado
        CHECK (estado IN ('Pendiente','Aprobada','Rechazada'))
);
GO

CREATE INDEX IX_Solicitud_idUsuario ON SolicitudAdquisicion(idUsuario);
CREATE INDEX IX_Solicitud_idAdministrador ON SolicitudAdquisicion(idAdministrador);
GO

CREATE TABLE Prestamo (
    idPrestamo INT IDENTITY(1,1) PRIMARY KEY,

    idUsuario INT NOT NULL,

    -- Administrador que registra el préstamo
    idAdministrador INT NULL,

    fechaPrestamo DATETIME NOT NULL DEFAULT GETDATE(),

    fechaLimite DATE NOT NULL,

    estado VARCHAR(30) NOT NULL DEFAULT 'Activo',

    CONSTRAINT FK_Prestamo_Usuario
        FOREIGN KEY (idUsuario)
        REFERENCES Usuario(idUsuario),

    CONSTRAINT FK_Prestamo_Administrador
        FOREIGN KEY (idAdministrador)
        REFERENCES Usuario(idUsuario),

    -- CAMBIO: dominio de valores permitidos para estado
    CONSTRAINT CK_Prestamo_Estado
        CHECK (estado IN ('Activo','Finalizado','Vencido','Cancelado'))
);
GO

CREATE INDEX IX_Prestamo_idUsuario ON Prestamo(idUsuario);
CREATE INDEX IX_Prestamo_idAdministrador ON Prestamo(idAdministrador);
GO

CREATE TABLE DetallePrestamo (
    idDetallePrestamo INT IDENTITY(1,1) PRIMARY KEY,

    idPrestamo INT NOT NULL,
    idEjemplar INT NOT NULL,

    fechaDevolucion DATE NULL,

    estado VARCHAR(30) NOT NULL DEFAULT 'Prestado',

    CONSTRAINT UQ_DetallePrestamo_Ejemplar
        UNIQUE (idPrestamo, idEjemplar),

    CONSTRAINT FK_DetallePrestamo_Prestamo
        FOREIGN KEY (idPrestamo)
        REFERENCES Prestamo(idPrestamo)
        ON DELETE CASCADE,

    CONSTRAINT FK_DetallePrestamo_Ejemplar
        FOREIGN KEY (idEjemplar)
        REFERENCES Ejemplar(idEjemplar),

    -- CAMBIO: dominio de valores permitidos para estado
    CONSTRAINT CK_DetallePrestamo_Estado
        CHECK (estado IN ('Prestado','Devuelto','Perdido'))
);
GO

-- CAMBIO: indice sobre idEjemplar (el UNIQUE compuesto solo indexa por idPrestamo primero)
CREATE INDEX IX_DetallePrestamo_idEjemplar ON DetallePrestamo(idEjemplar);
GO

CREATE TABLE Multa (
    idMulta INT IDENTITY(1,1) PRIMARY KEY,

    idDetallePrestamo INT NOT NULL,

    fechaGeneracion DATETIME NOT NULL DEFAULT GETDATE(),

    diasRetraso INT NOT NULL,
    tarifaDiaria DECIMAL(10,2) NOT NULL,
    monto DECIMAL(10,2) NOT NULL,

    motivo VARCHAR(255) NULL,

    estado VARCHAR(30) NOT NULL DEFAULT 'Pendiente',

    CONSTRAINT CK_Multa_DiasRetraso
        CHECK (diasRetraso >= 0),

    CONSTRAINT CK_Multa_Tarifa
        CHECK (tarifaDiaria >= 0),

    CONSTRAINT CK_Multa_Monto
        CHECK (monto >= 0),

    CONSTRAINT FK_Multa_DetallePrestamo
        FOREIGN KEY (idDetallePrestamo)
        REFERENCES DetallePrestamo(idDetallePrestamo),

    -- CAMBIO: dominio de valores permitidos para estado
    CONSTRAINT CK_Multa_Estado
        CHECK (estado IN ('Pendiente','Pagada','Condonada'))
);
GO

CREATE INDEX IX_Multa_idDetallePrestamo ON Multa(idDetallePrestamo);
GO

CREATE TABLE Pago (
    idPago INT IDENTITY(1,1) PRIMARY KEY,

    idMulta INT NOT NULL,

    idAdministrador INT NULL,

    monto DECIMAL(10,2) NOT NULL,

    fechaPago DATETIME NOT NULL DEFAULT GETDATE(),

    CONSTRAINT CK_Pago_Monto
        CHECK (monto > 0),

    CONSTRAINT FK_Pago_Multa
        FOREIGN KEY (idMulta)
        REFERENCES Multa(idMulta),

    CONSTRAINT FK_Pago_Administrador
        FOREIGN KEY (idAdministrador)
        REFERENCES Usuario(idUsuario)
);
GO

CREATE INDEX IX_Pago_idMulta ON Pago(idMulta);
CREATE INDEX IX_Pago_idAdministrador ON Pago(idAdministrador);
GO

CREATE TABLE Favorito (
    idUsuario INT NOT NULL,
    idMaterial INT NOT NULL,

    fechaAgregado DATETIME NOT NULL DEFAULT GETDATE(),

    CONSTRAINT PK_Favorito
        PRIMARY KEY (idUsuario, idMaterial),

    CONSTRAINT FK_Favorito_Usuario
        FOREIGN KEY (idUsuario)
        REFERENCES Usuario(idUsuario)
        ON DELETE CASCADE,

    CONSTRAINT FK_Favorito_Material
        FOREIGN KEY (idMaterial)
        REFERENCES Material(idMaterial)
        ON DELETE CASCADE
);
GO

CREATE INDEX IX_Favorito_idMaterial ON Favorito(idMaterial);
GO

CREATE TABLE Busqueda (
    idBusqueda BIGINT IDENTITY(1,1) PRIMARY KEY,

    -- NULL = búsqueda realizada como invitado
    idUsuario INT NULL,

    termino VARCHAR(255) NOT NULL,

    filtros VARCHAR(500) NULL,

    cantidadResultados INT NULL,

    fechaHora DATETIME NOT NULL DEFAULT GETDATE(),

    CONSTRAINT FK_Busqueda_Usuario
        FOREIGN KEY (idUsuario)
        REFERENCES Usuario(idUsuario)
        ON DELETE SET NULL,

    CONSTRAINT CK_Busqueda_Resultados
        CHECK (cantidadResultados IS NULL OR cantidadResultados >= 0)
);
GO

CREATE INDEX IX_Busqueda_idUsuario ON Busqueda(idUsuario);
GO

CREATE TABLE Acceso (
    idAcceso BIGINT IDENTITY(1,1) PRIMARY KEY,

    -- NULL = acceso como invitado
    idUsuario INT NULL,

    tipoAcceso VARCHAR(30) NOT NULL,
    resultado VARCHAR(30) NOT NULL,

    fechaHora DATETIME NOT NULL DEFAULT GETDATE(),

    CONSTRAINT FK_Acceso_Usuario
        FOREIGN KEY (idUsuario)
        REFERENCES Usuario(idUsuario)
        ON DELETE SET NULL,

    -- CAMBIO: dominio de valores permitidos
    CONSTRAINT CK_Acceso_Tipo
        CHECK (tipoAcceso IN ('Login','Logout')),

    CONSTRAINT CK_Acceso_Resultado
        CHECK (resultado IN ('Exitoso','Fallido'))
);
GO

CREATE INDEX IX_Acceso_idUsuario ON Acceso(idUsuario);
GO


/* ============================================================================
   TRIGGERS
   ============================================================================ */

-- CAMBIO: sincroniza automaticamente Ejemplar.estado al registrar un prestamo
CREATE TRIGGER TR_DetallePrestamo_Insert_MarcarPrestado
ON DetallePrestamo
AFTER INSERT
AS
BEGIN
    SET NOCOUNT ON;

    UPDATE E
        SET E.estado = 'Prestado'
    FROM Ejemplar E
    INNER JOIN inserted I ON I.idEjemplar = E.idEjemplar;
END
GO

-- CAMBIO: sincroniza automaticamente Ejemplar.estado al cerrar/devolver un prestamo
CREATE TRIGGER TR_DetallePrestamo_Update_MarcarDisponible
ON DetallePrestamo
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;

    -- Ejemplar vuelve a 'Disponible' cuando el detalle pasa a 'Devuelto'
    UPDATE E
        SET E.estado = 'Disponible'
    FROM Ejemplar E
    INNER JOIN inserted I ON I.idEjemplar = E.idEjemplar
    INNER JOIN deleted D ON D.idDetallePrestamo = I.idDetallePrestamo
    WHERE I.estado = 'Devuelto' AND D.estado <> 'Devuelto';

    -- Si se marca como Perdido, el ejemplar se da de baja
    UPDATE E
        SET E.estado = 'Extraviado'
    FROM Ejemplar E
    INNER JOIN inserted I ON I.idEjemplar = E.idEjemplar
    INNER JOIN deleted D ON D.idDetallePrestamo = I.idDetallePrestamo
    WHERE I.estado = 'Perdido' AND D.estado <> 'Perdido';
END
GO

-- CAMBIO: impide registrar un pago que sobre-pague una multa
CREATE TRIGGER TR_Pago_Insert_ValidarMonto
ON Pago
AFTER INSERT
AS
BEGIN
    SET NOCOUNT ON;

    IF EXISTS (
        SELECT 1
        FROM inserted I
        INNER JOIN Multa M ON M.idMulta = I.idMulta
        WHERE (
            SELECT ISNULL(SUM(P.monto), 0)
            FROM Pago P
            WHERE P.idMulta = M.idMulta
        ) > M.monto
    )
    BEGIN
        RAISERROR('El pago excede el monto pendiente de la multa.', 16, 1);
        ROLLBACK TRANSACTION;
    END
END
GO
