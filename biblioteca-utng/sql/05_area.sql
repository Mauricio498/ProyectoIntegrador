/* ============================================================================
   BIBLIOTECA UTNG - ÁREA ACADÉMICA DEL USUARIO
   Ejecutar DESPUÉS de los scripts 01 a 04.

   Agrega la carrera o departamento al que pertenece cada persona. Sirve para
   filtrar el padrón y para saber qué áreas usan más la biblioteca.

   Este script es una MIGRACIÓN: se puede ejecutar sobre una base que ya tiene
   datos y volver a ejecutarse sin romper nada.

   El campo es OPCIONAL (idArea admite NULL): los usuarios que ya existen no
   se ven afectados y el personal de biblioteca puede no tener área asignada.
   ============================================================================ */

USE BibliotecaUTNG;
GO


/* ---------------------------------------------------------------------------
   1. TABLA DE ÁREAS
   --------------------------------------------------------------------------- */
IF OBJECT_ID('Area', 'U') IS NULL
BEGIN
    CREATE TABLE Area (
        idArea INT IDENTITY(1,1) PRIMARY KEY,
        nombre VARCHAR(120) NOT NULL,
        siglas VARCHAR(20)  NULL,
        estado BIT          NOT NULL DEFAULT 1,

        CONSTRAINT UQ_Area_Nombre UNIQUE (nombre)
    );

    PRINT 'Tabla Area creada.';
END
ELSE
    PRINT 'Tabla Area ya existía, se conserva.';
GO


/* ---------------------------------------------------------------------------
   2. COLUMNA EN USUARIO
   --------------------------------------------------------------------------- */
IF COL_LENGTH('Usuario', 'idArea') IS NULL
BEGIN
    ALTER TABLE Usuario ADD idArea INT NULL;
    PRINT 'Columna Usuario.idArea agregada.';
END
ELSE
    PRINT 'Columna Usuario.idArea ya existía.';
GO

IF OBJECT_ID('FK_Usuario_Area', 'F') IS NULL
BEGIN
    ALTER TABLE Usuario
        ADD CONSTRAINT FK_Usuario_Area
            FOREIGN KEY (idArea) REFERENCES Area(idArea);

    PRINT 'Llave foránea FK_Usuario_Area creada.';
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_Usuario_idArea')
    CREATE INDEX IX_Usuario_idArea ON Usuario(idArea);
GO


/* ---------------------------------------------------------------------------
   3. LAS TRES ÁREAS ACADÉMICAS
   La UTNG agrupa su oferta en tres áreas académicas. Las carreras y
   especialidades quedan fuera a propósito: para los reportes basta el área,
   y así el catálogo no hay que mantenerlo cada vez que cambia la oferta.

   El personal de biblioteca no pertenece a ninguna de las tres, así que su
   idArea queda en NULL (la columna lo permite).
   --------------------------------------------------------------------------- */
INSERT INTO Area (nombre, siglas)
SELECT V.nombre, V.siglas
FROM (VALUES
    ('Tecnologías de la Información',          'TI'),
    ('Industrial, Eléctrica y Electrónica',    'IEE'),
    ('Económico Administrativo',               'EA')
) AS V(nombre, siglas)
WHERE NOT EXISTS (SELECT 1 FROM Area A WHERE A.nombre = V.nombre);
GO


/* ---------------------------------------------------------------------------
   4. VISTAS QUE DEBEN CONOCER EL ÁREA
   --------------------------------------------------------------------------- */
ALTER VIEW VW_Usuario AS
SELECT
    U.idUsuario,
    U.numeroControl,
    U.nombre,
    U.correo,
    U.telefono,
    U.usuario,
    U.passwordHash,
    U.estado,
    U.fechaRegistro,
    R.idRol,
    R.nombre        AS rol,
    R.descripcion   AS rolDescripcion,
    R.maxMateriales,
    R.diasPrestamo,
    R.estado        AS rolActivo,
    U.idArea,
    ISNULL(A.nombre, '')  AS area,
    ISNULL(A.siglas, '')  AS areaSiglas
FROM Usuario U
INNER JOIN Rol  R ON R.idRol  = U.idRol
LEFT  JOIN Area A ON A.idArea = U.idArea;
GO


/* Catálogo de áreas activas, para los ComboBox de la interfaz. */
CREATE OR ALTER VIEW VW_AreaActiva AS
SELECT idArea, nombre, siglas, estado
FROM Area
WHERE estado = 1;
GO


/* ---------------------------------------------------------------------------
   5. ESTADÍSTICA POR ÁREA
   --------------------------------------------------------------------------- */
CREATE OR ALTER PROCEDURE SP_PrestamosPorArea
    @desde DATE,
    @hasta DATE
AS
BEGIN
    SET NOCOUNT ON;

    SELECT
        ISNULL(A.nombre, 'Sin área asignada') AS area,
        COUNT(DP.idDetallePrestamo)           AS cantidad
    FROM Usuario U
    LEFT JOIN Area            A  ON A.idArea     = U.idArea
    LEFT JOIN Prestamo        P  ON P.idUsuario  = U.idUsuario
                                AND CAST(P.fechaPrestamo AS DATE) BETWEEN @desde AND @hasta
    LEFT JOIN DetallePrestamo DP ON DP.idPrestamo = P.idPrestamo
    GROUP BY ISNULL(A.nombre, 'Sin área asignada')
    HAVING COUNT(DP.idDetallePrestamo) > 0
    ORDER BY cantidad DESC;
END
GO


/* ---------------------------------------------------------------------------
   6. EL REGISTRO DE CUENTA AHORA ACEPTA ÁREA
   --------------------------------------------------------------------------- */
CREATE OR ALTER PROCEDURE SP_RegistrarCuenta
    @nombre        VARCHAR(150),
    @numeroControl VARCHAR(20),
    @correo        VARCHAR(100),
    @telefono      VARCHAR(20) = NULL,
    @usuario       VARCHAR(50),
    @passwordHash  VARCHAR(255),
    @rol           VARCHAR(30),
    @idArea        INT = NULL,
    @idUsuario     INT OUTPUT,
    @mensaje       VARCHAR(300) OUTPUT
AS
BEGIN
    SET NOCOUNT ON;

    SET @idUsuario = NULL;
    SET @mensaje   = NULL;

    /* Solo estos dos roles se pueden elegir al crear la cuenta.
       "Estudiante Estadía" queda fuera a propósito: da un plazo de un mes,
       así que debe asignarlo el personal de biblioteca cuando compruebe que
       el alumno está en estadía, no el propio interesado. */
    IF @rol NOT IN ('Estudiante', 'Profesor')
    BEGIN
        SET @mensaje = 'Solo puedes registrarte como Estudiante o Profesor.';
        RETURN;
    END

    DECLARE @idRol INT;
    SELECT @idRol = idRol FROM Rol WHERE nombre = @rol AND estado = 1;

    IF @idRol IS NULL
    BEGIN
        SET @mensaje = 'El rol solicitado no está disponible.';
        RETURN;
    END

    IF @nombre IS NULL OR LTRIM(RTRIM(@nombre)) = ''
    BEGIN
        SET @mensaje = 'El nombre es obligatorio.';
        RETURN;
    END

    IF @usuario IS NULL OR LTRIM(RTRIM(@usuario)) = ''
    BEGIN
        SET @mensaje = 'Define un nombre de usuario.';
        RETURN;
    END

    IF @idArea IS NOT NULL
       AND NOT EXISTS (SELECT 1 FROM Area WHERE idArea = @idArea AND estado = 1)
    BEGIN
        SET @mensaje = 'El área seleccionada no está disponible.';
        RETURN;
    END

    IF EXISTS (SELECT 1 FROM Usuario WHERE usuario = @usuario)
    BEGIN
        SET @mensaje = 'Ese nombre de usuario ya está ocupado, elige otro.';
        RETURN;
    END

    IF @numeroControl IS NOT NULL AND LTRIM(RTRIM(@numeroControl)) <> ''
       AND EXISTS (SELECT 1 FROM Usuario WHERE numeroControl = @numeroControl)
    BEGIN
        SET @mensaje = 'Ya existe una cuenta con ese número de control.';
        RETURN;
    END

    IF @correo IS NOT NULL AND LTRIM(RTRIM(@correo)) <> ''
       AND EXISTS (SELECT 1 FROM Usuario WHERE correo = @correo)
    BEGIN
        SET @mensaje = 'Ya existe una cuenta con ese correo.';
        RETURN;
    END

    BEGIN TRY
        INSERT INTO Usuario (numeroControl, nombre, correo, telefono,
                             usuario, passwordHash, idRol, idArea, estado)
        VALUES (NULLIF(LTRIM(RTRIM(@numeroControl)), ''),
                LTRIM(RTRIM(@nombre)),
                NULLIF(LTRIM(RTRIM(@correo)), ''),
                NULLIF(LTRIM(RTRIM(@telefono)), ''),
                LTRIM(RTRIM(@usuario)),
                @passwordHash,
                @idRol,
                @idArea,
                1);

        SET @idUsuario = SCOPE_IDENTITY();
        SET @mensaje = 'Cuenta creada correctamente. Ya puedes iniciar sesión.';
    END TRY
    BEGIN CATCH
        SET @idUsuario = NULL;
        SET @mensaje = 'No se pudo crear la cuenta: ' + ERROR_MESSAGE();
    END CATCH
END
GO


/* ---------------------------------------------------------------------------
   7. ÁREA DE LOS USUARIOS DE DEMOSTRACIÓN
   El personal de biblioteca se queda sin área: no cursa ni imparte en ninguna.
   --------------------------------------------------------------------------- */
UPDATE U
SET idArea = (SELECT idArea FROM Area WHERE siglas = 'TI')
FROM Usuario U
WHERE U.usuario IN ('estudiante', 'profesor', 'estadia') AND U.idArea IS NULL;

UPDATE U
SET idArea = (SELECT idArea FROM Area WHERE siglas = 'IEE')
FROM Usuario U
WHERE U.numeroControl IN ('A00123457', 'DOC-0115') AND U.idArea IS NULL;

UPDATE U
SET idArea = (SELECT idArea FROM Area WHERE siglas = 'EA')
FROM Usuario U
WHERE U.numeroControl IN ('A00123458', 'A00123459') AND U.idArea IS NULL;
GO


PRINT 'Área académica instalada.';
GO
