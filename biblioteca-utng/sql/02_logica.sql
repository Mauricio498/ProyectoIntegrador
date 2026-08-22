/* ============================================================================
   BIBLIOTECA UTNG - CAPA DE LOGICA EN BASE DE DATOS
   Ejecutar DESPUES de 01_esquema.sql

   Contiene:
     1. Tabla de parametros del sistema (tarifas, topes)
     2. Vistas de consulta que usa la aplicacion Java
     3. Funciones de apoyo
     4. Procedimientos almacenados (operaciones transaccionales)
   ============================================================================ */

USE BibliotecaUTNG;
GO


/* ============================================================================
   1. PARAMETROS DEL SISTEMA
   El esquema original no tenia donde guardar la tarifa de multa ni los topes
   globales, por eso se agrega esta tabla clave/valor.
   ============================================================================ */

CREATE TABLE ParametroSistema (
    clave       VARCHAR(50)  NOT NULL PRIMARY KEY,
    valor       VARCHAR(200) NOT NULL,
    descripcion VARCHAR(255) NULL
);
GO

INSERT INTO ParametroSistema (clave, valor, descripcion) VALUES
    ('MULTA_TARIFA_DIARIA', '5.00',  'Pesos que se cobran por cada dia de retraso'),
    ('MULTA_TOPE',          '500.00','Monto maximo que puede alcanzar una multa'),
    ('DIAS_GRACIA',         '0',     'Dias de tolerancia antes de generar multa'),
    ('DIAS_POR_VENCER',     '3',     'Dias previos a la fecha limite para marcar "por vencer"'),
    ('ADEUDO_MAXIMO',       '0.00',  'Adeudo pendiente que bloquea nuevos prestamos'),
    ('MULTA_DIAS_HABILES',  '1',     'Si es 1, el retraso solo cuenta lunes a viernes');
GO


CREATE FUNCTION dbo.FN_Parametro (@clave VARCHAR(50))
RETURNS DECIMAL(10,2)
AS
BEGIN
    DECLARE @v DECIMAL(10,2);
    SELECT @v = TRY_CAST(valor AS DECIMAL(10,2)) FROM ParametroSistema WHERE clave = @clave;
    RETURN ISNULL(@v, 0);
END
GO


/* ============================================================================
   1.1 CALENDARIO DE PRESTAMOS
   Estas funciones van ANTES de las vistas a propósito: VW_PrestamoDetalle
   calcula el retraso con FN_DiasHabilesEntre, y SQL Server exige que la
   función exista al momento de crear la vista.
   ============================================================================ */

/* ----------------------------------------------------------------------------
   FN_EsDiaHabil
   1 si la fecha cae de lunes a viernes.

   El cálculo NO usa DATEPART(WEEKDAY) porque ese depende de SET DATEFIRST,
   que cambia según la configuración de la sesión y haría que el mismo
   préstamo diera fechas distintas en dos equipos.

   En su lugar se ancla en 1900-01-01, que fue lunes:
     DATEDIFF(DAY, '19000101', fecha) % 7  ->  0=lunes ... 5=sábado, 6=domingo
   -------------------------------------------------------------------------- */
CREATE FUNCTION dbo.FN_EsDiaHabil (@fecha DATE)
RETURNS BIT
AS
BEGIN
    RETURN CASE WHEN (DATEDIFF(DAY, '19000101', @fecha) % 7) < 5 THEN 1 ELSE 0 END;
END
GO


/* ----------------------------------------------------------------------------
   FN_SiguienteDiaHabil
   Si la fecha cae en fin de semana, la recorre al lunes siguiente.
   -------------------------------------------------------------------------- */
CREATE FUNCTION dbo.FN_SiguienteDiaHabil (@fecha DATE)
RETURNS DATE
AS
BEGIN
    DECLARE @dow INT = DATEDIFF(DAY, '19000101', @fecha) % 7;   -- 0=lunes

    IF @dow = 5 RETURN DATEADD(DAY, 2, @fecha);   -- sábado -> lunes
    IF @dow = 6 RETURN DATEADD(DAY, 1, @fecha);   -- domingo -> lunes

    RETURN @fecha;
END
GO


/* ----------------------------------------------------------------------------
   FN_SumarDiasHabiles
   Suma N días hábiles a una fecha, saltando sábados y domingos.

   Se resuelve con una fórmula en lugar de un ciclo: por cada 5 días hábiles
   que se avanzan hay que sumar 2 de fin de semana.

     total = N + 2 * ((diaDeLaSemana + N) / 5)

   Ejemplos (con la biblioteca cerrada sábado y domingo):
     lunes    + 5 hábiles -> lunes siguiente
     viernes  + 5 hábiles -> viernes siguiente
     miércoles+ 10 hábiles-> miércoles de dos semanas después
   -------------------------------------------------------------------------- */
CREATE FUNCTION dbo.FN_SumarDiasHabiles (@fecha DATE, @dias INT)
RETURNS DATE
AS
BEGIN
    IF @dias IS NULL OR @dias <= 0
        RETURN dbo.FN_SiguienteDiaHabil(@fecha);

    -- Se parte siempre de un día hábil.
    DECLARE @inicio DATE = dbo.FN_SiguienteDiaHabil(@fecha);
    DECLARE @dow    INT  = DATEDIFF(DAY, '19000101', @inicio) % 7;   -- 0..4

    RETURN DATEADD(DAY, @dias + 2 * ((@dow + @dias) / 5), @inicio);
END
GO


/* ----------------------------------------------------------------------------
   FN_DiasHabilesEntre
   Días hábiles transcurridos entre dos fechas, sin contar la inicial.

   Se usa para el retraso: si un material vencía el viernes y se devuelve el
   lunes, la biblioteca estuvo cerrada de por medio, así que se cobra 1 día y
   no 3. El comportamiento se controla con el parámetro MULTA_DIAS_HABILES.
   -------------------------------------------------------------------------- */
CREATE FUNCTION dbo.FN_DiasHabilesEntre (@desde DATE, @hasta DATE)
RETURNS INT
AS
BEGIN
    IF @desde IS NULL OR @hasta IS NULL OR @hasta <= @desde
        RETURN 0;

    DECLARE @totales  INT = DATEDIFF(DAY, @desde, @hasta);
    DECLARE @dowDesde INT = DATEDIFF(DAY, '19000101', @desde) % 7;   -- 0=lunes

    -- Semanas completas: 5 hábiles cada una.
    DECLARE @habiles INT = (@totales / 7) * 5;

    -- Días sueltos de la última semana incompleta.
    DECLARE @resto INT = @totales % 7;
    DECLARE @i     INT = 1;

    WHILE @i <= @resto
    BEGIN
        IF ((@dowDesde + @i) % 7) < 5
            SET @habiles = @habiles + 1;
        SET @i = @i + 1;
    END

    RETURN @habiles;
END
GO


/* ----------------------------------------------------------------------------
   FN_CalcularFechaLimite
   Fecha de devolución según el plazo del rol.

     HABILES   -> N días hábiles (estudiantes y profesores)
     NATURALES -> N días corridos
     MESES     -> N meses de calendario (estadías), recorrido a día hábil

   Devolver siempre en día hábil evita fechas límite en fin de semana, cuando
   la biblioteca está cerrada y nadie podría entregar.
   -------------------------------------------------------------------------- */
CREATE FUNCTION dbo.FN_CalcularFechaLimite
    (@desde DATE, @cantidad INT, @unidad VARCHAR(10))
RETURNS DATE
AS
BEGIN
    DECLARE @cant INT = ISNULL(@cantidad, 0);

    IF @unidad = 'MESES'
        RETURN dbo.FN_SiguienteDiaHabil(DATEADD(MONTH, @cant, @desde));

    IF @unidad = 'NATURALES'
        RETURN dbo.FN_SiguienteDiaHabil(DATEADD(DAY, @cant, @desde));

    RETURN dbo.FN_SumarDiasHabiles(@desde, @cant);
END
GO


/* ============================================================================
   2. VISTAS
   ============================================================================ */

-- Catalogo: una fila por Material, con autores concatenados y conteo de ejemplares.
CREATE VIEW VW_CatalogoMaterial AS
SELECT
    M.idMaterial,
    M.titulo,
    M.isbn,
    M.anioPublicacion,
    M.clasificacion,
    M.estado                AS estadoMaterial,
    M.fechaRegistro,
    TM.idTipoMaterial,
    TM.nombre               AS tipoMaterial,
    TM.esPrestable,
    E.idEditorial,
    ISNULL(E.nombre, '')    AS editorial,
    ISNULL(STUFF((
        SELECT ', ' + A.nombre
        FROM MaterialAutor MA
        INNER JOIN Autor A ON A.idAutor = MA.idAutor
        WHERE MA.idMaterial = M.idMaterial
        ORDER BY A.nombre
        FOR XML PATH(''), TYPE).value('.', 'VARCHAR(MAX)'), 1, 2, ''), '') AS autores,
    (SELECT COUNT(*) FROM Ejemplar EJ
      WHERE EJ.idMaterial = M.idMaterial
        AND EJ.estado <> 'Baja')                     AS totalEjemplares,
    (SELECT COUNT(*) FROM Ejemplar EJ
      WHERE EJ.idMaterial = M.idMaterial
        AND EJ.estado = 'Disponible')                AS ejemplaresDisponibles
FROM Material M
INNER JOIN TipoMaterial TM ON TM.idTipoMaterial = M.idTipoMaterial
LEFT  JOIN Editorial   E  ON E.idEditorial     = M.idEditorial;
GO

-- Usuarios con su rol resuelto y sus limites de prestamo.
CREATE VIEW VW_Usuario AS
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
    R.unidadPlazo,
    R.estado        AS rolActivo
FROM Usuario U
INNER JOIN Rol R ON R.idRol = U.idRol;
GO

-- PENDIENTE
-- Una fila por ejemplar prestado, con el retraso ya calculado.
CREATE VIEW VW_PrestamoDetalle AS
SELECT
    DP.idDetallePrestamo,
    P.idPrestamo,
    P.idUsuario,
    U.nombre                AS usuarioNombre,
    U.numeroControl,
    P.idAdministrador,
    P.fechaPrestamo,
    P.fechaLimite,
    P.estado                AS estadoPrestamo,
    DP.idEjemplar,
    EJ.codigoEjemplar,
    EJ.estado               AS estadoEjemplar,
    M.idMaterial,
    M.titulo,
    M.isbn,
    B.idBiblioteca,
    B.nombre                AS biblioteca,
    DP.fechaDevolucion,
    DP.estado               AS estadoDetalle,
    CASE
        WHEN DP.estado = 'Prestado' AND CAST(GETDATE() AS DATE) > P.fechaLimite
            THEN CASE WHEN dbo.FN_Parametro('MULTA_DIAS_HABILES') = 1
                      THEN dbo.FN_DiasHabilesEntre(P.fechaLimite, CAST(GETDATE() AS DATE))
                      ELSE DATEDIFF(DAY, P.fechaLimite, CAST(GETDATE() AS DATE)) END
        WHEN DP.estado = 'Devuelto' AND DP.fechaDevolucion > P.fechaLimite
            THEN CASE WHEN dbo.FN_Parametro('MULTA_DIAS_HABILES') = 1
                      THEN dbo.FN_DiasHabilesEntre(P.fechaLimite, DP.fechaDevolucion)
                      ELSE DATEDIFF(DAY, P.fechaLimite, DP.fechaDevolucion) END
        ELSE 0
    END                     AS diasRetraso
FROM DetallePrestamo DP
INNER JOIN Prestamo  P  ON P.idPrestamo   = DP.idPrestamo
INNER JOIN Usuario   U  ON U.idUsuario    = P.idUsuario
INNER JOIN Ejemplar  EJ ON EJ.idEjemplar  = DP.idEjemplar
INNER JOIN Material  M  ON M.idMaterial   = EJ.idMaterial
INNER JOIN Biblioteca B ON B.idBiblioteca = EJ.idBiblioteca;
GO


-- Adeudo vigente de cada usuario (multas pendientes menos lo ya pagado).
CREATE VIEW VW_AdeudoUsuario AS
SELECT
    P.idUsuario,
    COUNT(DISTINCT MU.idMulta) AS multasPendientes,
    -- Restamos el total de multas menos el total de pagos agrupados por usuario
    ISNULL(SUM(MU.monto), 0) - ISNULL(SUM(PAG.totalPagado), 0) AS adeudo
FROM Multa MU
INNER JOIN DetallePrestamo DP ON DP.idDetallePrestamo = MU.idDetallePrestamo
INNER JOIN Prestamo        P  ON P.idPrestamo         = DP.idPrestamo
-- Unimos con el total pagado por cada multa para evitar duplicar montos
LEFT JOIN (
    SELECT idMulta, SUM(monto) AS totalPagado
    FROM Pago
    GROUP BY idMulta
) PAG ON PAG.idMulta = MU.idMulta
WHERE MU.estado = 'Pendiente'
GROUP BY P.idUsuario;
GO



-- Tarjetas del dashboard de administrador.
CREATE VIEW VW_EstadisticasGenerales AS
SELECT
    (SELECT COUNT(*) FROM Material WHERE estado = 'Activo')          AS totalMateriales,
    (SELECT COUNT(*) FROM Ejemplar WHERE estado <> 'Baja')           AS totalEjemplares,
    (SELECT COUNT(*) FROM Ejemplar WHERE estado = 'Disponible')      AS ejemplaresDisponibles,
    (SELECT COUNT(*) FROM Ejemplar WHERE estado = 'Prestado')        AS ejemplaresPrestados,
    (SELECT COUNT(*) FROM Usuario  WHERE estado = 1)                 AS usuariosActivos,
    (SELECT COUNT(*) FROM Prestamo WHERE estado = 'Activo')          AS prestamosActivos,
    (SELECT COUNT(*) FROM VW_PrestamoDetalle
      WHERE estadoDetalle = 'Prestado' AND diasRetraso > 0)          AS prestamosRetrasados,
    (SELECT COUNT(*) FROM SolicitudAdquisicion WHERE estado = 'Pendiente') AS solicitudesPendientes,
    (SELECT ISNULL(SUM(monto), 0) FROM Multa WHERE estado = 'Pendiente')   AS multasPendientes;
GO


-- Materiales mas solicitados (para "destacados" y reportes).
CREATE VIEW VW_MaterialesPopulares AS
SELECT TOP 100
    C.idMaterial,
    C.titulo,
    C.autores,
    C.tipoMaterial,
    C.editorial,
    C.ejemplaresDisponibles,
    COUNT(DP.idDetallePrestamo) AS vecesPrestado
FROM VW_CatalogoMaterial C
LEFT JOIN Ejemplar        EJ ON EJ.idMaterial = C.idMaterial
LEFT JOIN DetallePrestamo DP ON DP.idEjemplar = EJ.idEjemplar
GROUP BY C.idMaterial, C.titulo, C.autores, C.tipoMaterial, C.editorial, C.ejemplaresDisponibles
ORDER BY vecesPrestado DESC, C.titulo ASC;
GO


/* ============================================================================
   3. FUNCIONES DE APOYO
   ============================================================================ */

-- Cuantos ejemplares tiene actualmente en su poder un usuario.
CREATE FUNCTION dbo.FN_MaterialesEnPoder (@idUsuario INT)
RETURNS INT
AS
BEGIN
    DECLARE @n INT;
    SELECT @n = COUNT(*)
    FROM DetallePrestamo DP
    INNER JOIN Prestamo P ON P.idPrestamo = DP.idPrestamo
    WHERE P.idUsuario = @idUsuario
      AND DP.estado = 'Prestado';
    RETURN ISNULL(@n, 0);
END
GO


-- Adeudo pendiente de un usuario.
CREATE FUNCTION dbo.FN_AdeudoUsuario (@idUsuario INT)
RETURNS DECIMAL(10,2)
AS
BEGIN
    DECLARE @v DECIMAL(10,2);
    SELECT @v = adeudo FROM VW_AdeudoUsuario WHERE idUsuario = @idUsuario;
    RETURN ISNULL(@v, 0);
END
GO


/* ============================================================================
   4. PROCEDIMIENTOS ALMACENADOS
   ============================================================================ */

/* ----------------------------------------------------------------------------
   SP_Login
   Valida credenciales, deja constancia en Acceso y devuelve el usuario.
   @resultado: 0 = ok, 1 = credenciales invalidas, 2 = usuario inactivo
   -------------------------------------------------------------------------- */
CREATE PROCEDURE SP_Login
    @usuario      VARCHAR(50),
    @passwordHash VARCHAR(255),
    @resultado    INT OUTPUT
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @idUsuario INT, @activo BIT;

    SELECT @idUsuario = idUsuario, @activo = estado
    FROM Usuario
    WHERE usuario = @usuario AND passwordHash = @passwordHash;

    IF @idUsuario IS NULL
    BEGIN
        SET @resultado = 1;
        INSERT INTO Acceso (idUsuario, tipoAcceso, resultado)
        VALUES (NULL, 'Login', 'Fallido');
        RETURN;
    END

    IF @activo = 0
    BEGIN
        SET @resultado = 2;
        INSERT INTO Acceso (idUsuario, tipoAcceso, resultado)
        VALUES (@idUsuario, 'Login', 'Fallido');
        RETURN;
    END

    SET @resultado = 0;
    INSERT INTO Acceso (idUsuario, tipoAcceso, resultado)
    VALUES (@idUsuario, 'Login', 'Exitoso');

    SELECT * FROM VW_Usuario WHERE idUsuario = @idUsuario;
END
GO


/* ----------------------------------------------------------------------------
   SP_RegistrarPrestamo
   Registra un prestamo completo (1..n ejemplares) de forma transaccional.
   @idsEjemplares: lista separada por comas, por ejemplo '12,45,80'

   Reglas que valida:
     - El usuario existe y esta activo.
     - Su rol permite prestamos (maxMateriales > 0).
     - No excede maxMateriales sumando lo que ya tiene.
     - No tiene adeudo por encima de ADEUDO_MAXIMO.
     - Cada ejemplar existe, esta Disponible y su tipo es prestable.

   La fecha limite se calcula con Rol.diasPrestamo.
   -------------------------------------------------------------------------- */
CREATE PROCEDURE SP_RegistrarPrestamo
    @idUsuario       INT,
    @idAdministrador INT           = NULL,
    @idsEjemplares   VARCHAR(1000),
    @idPrestamo      INT OUTPUT,
    @mensaje         VARCHAR(300) OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    SET @idPrestamo = NULL;
    SET @mensaje    = NULL;

    DECLARE @maxMateriales INT, @diasPrestamo INT, @activo BIT, @rol VARCHAR(30);
    DECLARE @unidadPlazo VARCHAR(10);

    SELECT @maxMateriales = maxMateriales,
           @diasPrestamo  = diasPrestamo,
           @unidadPlazo   = unidadPlazo,
           @activo        = estado,
           @rol           = rol
    FROM VW_Usuario WHERE idUsuario = @idUsuario;

    IF @rol IS NULL
    BEGIN
        SET @mensaje = 'El usuario no existe.';
        RETURN;
    END

    IF @activo = 0
    BEGIN
        SET @mensaje = 'El usuario esta dado de baja.';
        RETURN;
    END

    IF ISNULL(@maxMateriales, 0) <= 0
    BEGIN
        SET @mensaje = 'El rol "' + @rol + '" no tiene permitido solicitar prestamos.';
        RETURN;
    END

    -- Ejemplares solicitados (deduplicados)
    DECLARE @sol TABLE (idEjemplar INT PRIMARY KEY);
    INSERT INTO @sol (idEjemplar)
    SELECT DISTINCT TRY_CAST(value AS INT)
    FROM STRING_SPLIT(@idsEjemplares, ',')
    WHERE TRY_CAST(value AS INT) IS NOT NULL;

    DECLARE @cantidad INT = (SELECT COUNT(*) FROM @sol);

    IF @cantidad = 0
    BEGIN
        SET @mensaje = 'No se indico ningun ejemplar.';
        RETURN;
    END

    DECLARE @enPoder INT = dbo.FN_MaterialesEnPoder(@idUsuario);

    IF @enPoder + @cantidad > @maxMateriales
    BEGIN
        SET @mensaje = 'Limite excedido: el rol "' + @rol + '" permite hasta '
                     + CAST(@maxMateriales AS VARCHAR(10)) + ' materiales y el usuario ya tiene '
                     + CAST(@enPoder AS VARCHAR(10)) + '.';
        RETURN;
    END

    IF dbo.FN_AdeudoUsuario(@idUsuario) > dbo.FN_Parametro('ADEUDO_MAXIMO')
    BEGIN
        SET @mensaje = 'El usuario tiene multas pendientes por pagar.';
        RETURN;
    END

    -- Validacion de cada ejemplar
    DECLARE @problema VARCHAR(300);

    SELECT TOP 1 @problema = 'El ejemplar ' + CAST(S.idEjemplar AS VARCHAR(10)) + ' no existe.'
    FROM @sol S WHERE NOT EXISTS (SELECT 1 FROM Ejemplar E WHERE E.idEjemplar = S.idEjemplar);

    IF @problema IS NULL
        SELECT TOP 1 @problema = 'El ejemplar ' + E.codigoEjemplar + ' no esta disponible (' + E.estado + ').'
        FROM @sol S INNER JOIN Ejemplar E ON E.idEjemplar = S.idEjemplar
        WHERE E.estado <> 'Disponible';

    IF @problema IS NULL
        SELECT TOP 1 @problema = 'El ejemplar ' + E.codigoEjemplar + ' es de un tipo no prestable.'
        FROM @sol S
        INNER JOIN Ejemplar     E  ON E.idEjemplar     = S.idEjemplar
        INNER JOIN Material     M  ON M.idMaterial     = E.idMaterial
        INNER JOIN TipoMaterial TM ON TM.idTipoMaterial = M.idTipoMaterial
        WHERE TM.esPrestable = 0;

    IF @problema IS NOT NULL
    BEGIN
        SET @mensaje = @problema;
        RETURN;
    END

    BEGIN TRY
        BEGIN TRANSACTION;

        -- El plazo depende del rol: días hábiles para estudiantes y profesores,
        -- meses para quienes están en estadía fuera del plantel.
        DECLARE @fechaLimite DATE = dbo.FN_CalcularFechaLimite(
                CAST(GETDATE() AS DATE), @diasPrestamo, @unidadPlazo);

        INSERT INTO Prestamo (idUsuario, idAdministrador, fechaPrestamo, fechaLimite, estado)
        VALUES (@idUsuario, @idAdministrador, GETDATE(), @fechaLimite, 'Activo');

        SET @idPrestamo = SCOPE_IDENTITY();

        -- El trigger TR_DetallePrestamo_Insert_MarcarPrestado
        -- pone los ejemplares en estado 'Prestado'.
        INSERT INTO DetallePrestamo (idPrestamo, idEjemplar, estado)
        SELECT @idPrestamo, idEjemplar, 'Prestado' FROM @sol;

        COMMIT TRANSACTION;

        SET @mensaje = 'Prestamo registrado correctamente.';
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        SET @idPrestamo = NULL;
        SET @mensaje = 'Error al registrar el prestamo: ' + ERROR_MESSAGE();
    END CATCH
END
GO


/* ----------------------------------------------------------------------------
   SP_RegistrarDevolucion
   Marca un ejemplar como devuelto, genera la multa si hubo retraso y cierra
   el prestamo cuando ya no quedan ejemplares pendientes.
   @perdido = 1 registra el ejemplar como extraviado en lugar de devuelto.
   -------------------------------------------------------------------------- */
CREATE PROCEDURE SP_RegistrarDevolucion
    @idDetallePrestamo INT,
    @idAdministrador   INT = NULL,
    @perdido           BIT = 0,
    @idMulta           INT OUTPUT,
    @mensaje           VARCHAR(300) OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    SET @idMulta = NULL;
    SET @mensaje = NULL;

    DECLARE @idPrestamo INT, @fechaLimite DATE, @estadoDetalle VARCHAR(30);

    SELECT @idPrestamo    = DP.idPrestamo,
           @fechaLimite   = P.fechaLimite,
           @estadoDetalle = DP.estado
    FROM DetallePrestamo DP
    INNER JOIN Prestamo P ON P.idPrestamo = DP.idPrestamo
    WHERE DP.idDetallePrestamo = @idDetallePrestamo;

    IF @idPrestamo IS NULL
    BEGIN
        SET @mensaje = 'El detalle de prestamo no existe.';
        RETURN;
    END

    IF @estadoDetalle <> 'Prestado'
    BEGIN
        SET @mensaje = 'Ese ejemplar ya fue cerrado (estado actual: ' + @estadoDetalle + ').';
        RETURN;
    END

    DECLARE @hoy         DATE = CAST(GETDATE() AS DATE);
    DECLARE @diasGracia  INT  = CAST(dbo.FN_Parametro('DIAS_GRACIA') AS INT);
    DECLARE @diasRetraso INT =
        CASE WHEN @hoy > DATEADD(DAY, @diasGracia, @fechaLimite)
             THEN CASE WHEN dbo.FN_Parametro('MULTA_DIAS_HABILES') = 1
                       THEN dbo.FN_DiasHabilesEntre(@fechaLimite, @hoy)
                       ELSE DATEDIFF(DAY, @fechaLimite, @hoy) END
             ELSE 0 END;

    BEGIN TRY
        BEGIN TRANSACTION;

        -- Los triggers de DetallePrestamo sincronizan el estado del Ejemplar.
        UPDATE DetallePrestamo
        SET estado          = CASE WHEN @perdido = 1 THEN 'Perdido' ELSE 'Devuelto' END,
            fechaDevolucion = @hoy
        WHERE idDetallePrestamo = @idDetallePrestamo;

        IF @diasRetraso > 0 OR @perdido = 1
        BEGIN
            DECLARE @tarifa DECIMAL(10,2) = dbo.FN_Parametro('MULTA_TARIFA_DIARIA');
            DECLARE @tope   DECIMAL(10,2) = dbo.FN_Parametro('MULTA_TOPE');
            DECLARE @monto  DECIMAL(10,2) = @tarifa * @diasRetraso;

            IF @tope > 0 AND @monto > @tope SET @monto = @tope;

            IF @perdido = 1 AND @monto = 0 SET @monto = @tope;

            INSERT INTO Multa (idDetallePrestamo, diasRetraso, tarifaDiaria, monto, motivo, estado)
            VALUES (@idDetallePrestamo, @diasRetraso, @tarifa, @monto,
                    CASE WHEN @perdido = 1 THEN 'Ejemplar extraviado'
                         ELSE 'Devolucion con ' + CAST(@diasRetraso AS VARCHAR(10)) + ' dia(s) de retraso' END,
                    'Pendiente');

            SET @idMulta = SCOPE_IDENTITY();
        END

        -- Si ya no quedan ejemplares prestados, se cierra el prestamo.
        IF NOT EXISTS (SELECT 1 FROM DetallePrestamo
                       WHERE idPrestamo = @idPrestamo AND estado = 'Prestado')
        BEGIN
            UPDATE Prestamo SET estado = 'Finalizado' WHERE idPrestamo = @idPrestamo;
        END

        COMMIT TRANSACTION;

        SET @mensaje = CASE
            WHEN @idMulta IS NOT NULL THEN 'Devolucion registrada. Se genero una multa.'
            ELSE 'Devolucion registrada sin multa.' END;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        SET @idMulta = NULL;
        SET @mensaje = 'Error al registrar la devolucion: ' + ERROR_MESSAGE();
    END CATCH
END
GO


/* ----------------------------------------------------------------------------
   SP_RegistrarPago
   Aplica un pago a una multa. El trigger TR_Pago_Insert_ValidarMonto impide
   que se sobrepague; aqui ademas se marca la multa como Pagada al saldarla.
   -------------------------------------------------------------------------- */
CREATE PROCEDURE SP_RegistrarPago
    @idMulta         INT,
    @idAdministrador INT = NULL,
    @monto           DECIMAL(10,2),
    @mensaje         VARCHAR(300) OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    DECLARE @montoMulta DECIMAL(10,2), @estado VARCHAR(30);

    SELECT @montoMulta = monto, @estado = estado FROM Multa WHERE idMulta = @idMulta;

    IF @montoMulta IS NULL
    BEGIN
        SET @mensaje = 'La multa no existe.';
        RETURN;
    END

    IF @estado <> 'Pendiente'
    BEGIN
        SET @mensaje = 'La multa ya esta ' + @estado + '.';
        RETURN;
    END

    BEGIN TRY
        BEGIN TRANSACTION;

        INSERT INTO Pago (idMulta, idAdministrador, monto)
        VALUES (@idMulta, @idAdministrador, @monto);

        DECLARE @pagado DECIMAL(10,2) =
            (SELECT ISNULL(SUM(monto), 0) FROM Pago WHERE idMulta = @idMulta);

        IF @pagado >= @montoMulta
            UPDATE Multa SET estado = 'Pagada' WHERE idMulta = @idMulta;

        COMMIT TRANSACTION;

        SET @mensaje = CASE WHEN @pagado >= @montoMulta
                            THEN 'Multa saldada.'
                            ELSE 'Pago parcial registrado. Restan '
                                 + CAST(@montoMulta - @pagado AS VARCHAR(20)) + '.' END;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        SET @mensaje = 'Error al registrar el pago: ' + ERROR_MESSAGE();
    END CATCH
END
GO


/* ----------------------------------------------------------------------------
   SP_ActualizarPrestamosVencidos
   Marca como 'Vencido' todo prestamo activo cuya fecha limite ya paso.
   La aplicacion lo ejecuta al iniciar sesion un administrador.
   -------------------------------------------------------------------------- */
CREATE PROCEDURE SP_ActualizarPrestamosVencidos
    @afectados INT OUTPUT
AS
BEGIN
    SET NOCOUNT ON;

    UPDATE Prestamo
    SET estado = 'Vencido'
    WHERE estado = 'Activo'
      AND fechaLimite < CAST(GETDATE() AS DATE)
      AND EXISTS (SELECT 1 FROM DetallePrestamo DP
                  WHERE DP.idPrestamo = Prestamo.idPrestamo AND DP.estado = 'Prestado');

    SET @afectados = @@ROWCOUNT;
END
GO


/* ----------------------------------------------------------------------------
   SP_ResponderSolicitud
   Un administrador aprueba o rechaza una solicitud de adquisicion.
   -------------------------------------------------------------------------- */
CREATE PROCEDURE SP_ResponderSolicitud
    @idSolicitud     INT,
    @idAdministrador INT,
    @estado          VARCHAR(30),
    @respuesta       VARCHAR(MAX) = NULL,
    @mensaje         VARCHAR(300) OUTPUT
AS
BEGIN
    SET NOCOUNT ON;

    IF @estado NOT IN ('Aprobada', 'Rechazada')
    BEGIN
        SET @mensaje = 'Estado invalido. Use Aprobada o Rechazada.';
        RETURN;
    END

    IF NOT EXISTS (SELECT 1 FROM SolicitudAdquisicion
                   WHERE idSolicitud = @idSolicitud AND estado = 'Pendiente')
    BEGIN
        SET @mensaje = 'La solicitud no existe o ya fue atendida.';
        RETURN;
    END

    UPDATE SolicitudAdquisicion
    SET estado          = @estado,
        respuesta       = @respuesta,
        idAdministrador = @idAdministrador,
        fechaRespuesta  = GETDATE()
    WHERE idSolicitud = @idSolicitud;

    SET @mensaje = 'Solicitud marcada como ' + @estado + '.';
END
GO


/* ----------------------------------------------------------------------------
   SP_RegistrarBusqueda
   Guarda la bitacora de busquedas. @idUsuario NULL = invitado.
   -------------------------------------------------------------------------- */
CREATE PROCEDURE SP_RegistrarBusqueda
    @idUsuario          INT = NULL,
    @termino            VARCHAR(255),
    @filtros            VARCHAR(500) = NULL,
    @cantidadResultados INT = NULL
AS
BEGIN
    SET NOCOUNT ON;
    INSERT INTO Busqueda (idUsuario, termino, filtros, cantidadResultados)
    VALUES (@idUsuario, @termino, @filtros, @cantidadResultados);
END
GO


/* ============================================================================
   5. SOLICITUDES DE PRESTAMO
   El usuario no toma el material directamente: levanta una solicitud y el
   personal de biblioteca la aprueba o la rechaza. Al aprobar, el sistema
   elige un ejemplar libre del material y genera el prestamo real.
   ============================================================================ */

CREATE TABLE SolicitudPrestamo (
    idSolicitudPrestamo INT IDENTITY(1,1) PRIMARY KEY,

    idUsuario  INT NOT NULL,
    idMaterial INT NOT NULL,

    fechaSolicitud DATETIME NOT NULL DEFAULT GETDATE(),

    estado VARCHAR(30) NOT NULL DEFAULT 'Pendiente',

    idAdministrador INT NULL,
    fechaRespuesta  DATETIME NULL,
    observaciones   VARCHAR(300) NULL,

    -- Se llena cuando la solicitud se aprueba y nace el prestamo
    idPrestamo INT NULL,

    CONSTRAINT FK_SolicitudPrestamo_Usuario
        FOREIGN KEY (idUsuario) REFERENCES Usuario(idUsuario),

    CONSTRAINT FK_SolicitudPrestamo_Material
        FOREIGN KEY (idMaterial) REFERENCES Material(idMaterial),

    CONSTRAINT FK_SolicitudPrestamo_Administrador
        FOREIGN KEY (idAdministrador) REFERENCES Usuario(idUsuario),

    CONSTRAINT FK_SolicitudPrestamo_Prestamo
        FOREIGN KEY (idPrestamo) REFERENCES Prestamo(idPrestamo),

    CONSTRAINT CK_SolicitudPrestamo_Estado
        CHECK (estado IN ('Pendiente', 'Aprobada', 'Rechazada', 'Cancelada'))
);
GO

CREATE INDEX IX_SolicitudPrestamo_idUsuario  ON SolicitudPrestamo(idUsuario);
CREATE INDEX IX_SolicitudPrestamo_idMaterial ON SolicitudPrestamo(idMaterial);
CREATE INDEX IX_SolicitudPrestamo_estado     ON SolicitudPrestamo(estado);
GO


-- Bandeja de solicitudes con todo lo que la pantalla necesita mostrar.
CREATE VIEW VW_SolicitudPrestamo AS
SELECT
    SP.idSolicitudPrestamo,
    SP.idUsuario,
    U.nombre                AS usuarioNombre,
    U.numeroControl,
    R.nombre                AS usuarioRol,
    R.maxMateriales,
    SP.idMaterial,
    M.titulo,
    M.isbn,
    C.autores,
    C.tipoMaterial,
    C.ejemplaresDisponibles,
    SP.fechaSolicitud,
    SP.estado,
    SP.idAdministrador,
    A.nombre                AS administradorNombre,
    SP.fechaRespuesta,
    SP.observaciones,
    SP.idPrestamo,
    dbo.FN_MaterialesEnPoder(SP.idUsuario) AS materialesEnPoder,
    dbo.FN_AdeudoUsuario(SP.idUsuario)     AS adeudo
FROM SolicitudPrestamo SP
INNER JOIN Usuario             U ON U.idUsuario  = SP.idUsuario
INNER JOIN Rol                 R ON R.idRol      = U.idRol
INNER JOIN Material            M ON M.idMaterial = SP.idMaterial
INNER JOIN VW_CatalogoMaterial C ON C.idMaterial = SP.idMaterial
LEFT  JOIN Usuario             A ON A.idUsuario  = SP.idAdministrador;
GO


/* ----------------------------------------------------------------------------
   SP_SolicitarPrestamo
   El usuario pide un material. No se reserva ningun ejemplar todavia: la
   asignacion ocurre hasta que un administrador aprueba.

   Valida por adelantado lo mismo que validara el prestamo, para no dejar
   pasar solicitudes que de todas formas se rechazarian.
   -------------------------------------------------------------------------- */
CREATE PROCEDURE SP_SolicitarPrestamo
    @idUsuario   INT,
    @idMaterial  INT,
    @idSolicitud INT OUTPUT,
    @mensaje     VARCHAR(300) OUTPUT
AS
BEGIN
    SET NOCOUNT ON;

    SET @idSolicitud = NULL;
    SET @mensaje     = NULL;

    DECLARE @maxMateriales INT, @activo BIT, @rol VARCHAR(30);

    SELECT @maxMateriales = maxMateriales, @activo = estado, @rol = rol
    FROM VW_Usuario WHERE idUsuario = @idUsuario;

    IF @rol IS NULL
    BEGIN
        SET @mensaje = 'El usuario no existe.';
        RETURN;
    END

    IF @activo = 0
    BEGIN
        SET @mensaje = 'Tu cuenta esta dada de baja, acude a la biblioteca.';
        RETURN;
    END

    IF ISNULL(@maxMateriales, 0) <= 0
    BEGIN
        SET @mensaje = 'El rol "' + @rol + '" solo tiene acceso de consulta.';
        RETURN;
    END

    IF NOT EXISTS (SELECT 1 FROM Material WHERE idMaterial = @idMaterial AND estado = 'Activo')
    BEGIN
        SET @mensaje = 'El material no esta disponible en el catalogo.';
        RETURN;
    END

    IF NOT EXISTS (
        SELECT 1
        FROM Material M
        INNER JOIN TipoMaterial TM ON TM.idTipoMaterial = M.idTipoMaterial
        WHERE M.idMaterial = @idMaterial AND TM.esPrestable = 1)
    BEGIN
        SET @mensaje = 'Ese material es de consulta en sala, no se presta a domicilio.';
        RETURN;
    END

    IF EXISTS (SELECT 1 FROM SolicitudPrestamo
               WHERE idUsuario = @idUsuario AND idMaterial = @idMaterial AND estado = 'Pendiente')
    BEGIN
        SET @mensaje = 'Ya tienes una solicitud pendiente de ese material.';
        RETURN;
    END

    IF dbo.FN_AdeudoUsuario(@idUsuario) > dbo.FN_Parametro('ADEUDO_MAXIMO')
    BEGIN
        SET @mensaje = 'Tienes multas pendientes por pagar, no puedes solicitar prestamos.';
        RETURN;
    END

    -- El cupo cuenta lo que ya trae mas lo que tiene pedido y sin responder.
    DECLARE @enPoder     INT = dbo.FN_MaterialesEnPoder(@idUsuario);
    DECLARE @enSolicitud INT = (SELECT COUNT(*) FROM SolicitudPrestamo
                                WHERE idUsuario = @idUsuario AND estado = 'Pendiente');

    IF @enPoder + @enSolicitud >= @maxMateriales
    BEGIN
        SET @mensaje = 'Alcanzaste el limite de tu rol: ' + CAST(@maxMateriales AS VARCHAR(10))
                     + ' materiales (tienes ' + CAST(@enPoder AS VARCHAR(10))
                     + ' en tu poder y ' + CAST(@enSolicitud AS VARCHAR(10)) + ' en solicitud).';
        RETURN;
    END

    INSERT INTO SolicitudPrestamo (idUsuario, idMaterial)
    VALUES (@idUsuario, @idMaterial);

    SET @idSolicitud = SCOPE_IDENTITY();
    SET @mensaje = 'Solicitud enviada. El personal de biblioteca la revisara.';
END
GO


/* ----------------------------------------------------------------------------
   SP_ResponderSolicitudPrestamo
   El administrador aprueba o rechaza. Al aprobar se toma el primer ejemplar
   disponible del material y se delega en SP_RegistrarPrestamo, que es quien
   sabe calcular la fecha limite y validar las reglas.
   -------------------------------------------------------------------------- */
CREATE PROCEDURE SP_ResponderSolicitudPrestamo
    @idSolicitud     INT,
    @idAdministrador INT,
    @aprobar         BIT,
    @observaciones   VARCHAR(300) = NULL,
    @idPrestamo      INT OUTPUT,
    @mensaje         VARCHAR(300) OUTPUT
AS
BEGIN
    SET NOCOUNT ON;

    SET @idPrestamo = NULL;
    SET @mensaje    = NULL;

    DECLARE @idUsuario INT, @idMaterial INT, @estado VARCHAR(30);

    SELECT @idUsuario  = idUsuario,
           @idMaterial = idMaterial,
           @estado     = estado
    FROM SolicitudPrestamo WHERE idSolicitudPrestamo = @idSolicitud;

    IF @estado IS NULL
    BEGIN
        SET @mensaje = 'La solicitud no existe.';
        RETURN;
    END

    IF @estado <> 'Pendiente'
    BEGIN
        SET @mensaje = 'Esa solicitud ya fue atendida (estado actual: ' + @estado + ').';
        RETURN;
    END

    IF @aprobar = 0
    BEGIN
        UPDATE SolicitudPrestamo
        SET estado          = 'Rechazada',
            idAdministrador = @idAdministrador,
            fechaRespuesta  = GETDATE(),
            observaciones   = @observaciones
        WHERE idSolicitudPrestamo = @idSolicitud;

        SET @mensaje = 'Solicitud rechazada.';
        RETURN;
    END

    -- Aprobacion: se busca un ejemplar libre del material solicitado.
    DECLARE @idEjemplar INT;

    SELECT TOP 1 @idEjemplar = idEjemplar
    FROM Ejemplar
    WHERE idMaterial = @idMaterial AND estado = 'Disponible'
    ORDER BY codigoEjemplar;

    IF @idEjemplar IS NULL
    BEGIN
        SET @mensaje = 'No hay ejemplares disponibles de ese material en este momento.';
        RETURN;
    END

    DECLARE @msgPrestamo VARCHAR(300);

    EXEC SP_RegistrarPrestamo
         @idUsuario       = @idUsuario,
         @idAdministrador = @idAdministrador,
         @idsEjemplares   = @idEjemplar,
         @idPrestamo      = @idPrestamo OUTPUT,
         @mensaje         = @msgPrestamo OUTPUT;

    IF @idPrestamo IS NULL
    BEGIN
        SET @mensaje = 'No se pudo aprobar: ' + ISNULL(@msgPrestamo, 'error desconocido');
        RETURN;
    END

    UPDATE SolicitudPrestamo
    SET estado          = 'Aprobada',
        idAdministrador = @idAdministrador,
        fechaRespuesta  = GETDATE(),
        observaciones   = @observaciones,
        idPrestamo      = @idPrestamo
    WHERE idSolicitudPrestamo = @idSolicitud;

    SET @mensaje = 'Solicitud aprobada y prestamo registrado.';
END
GO


/* ----------------------------------------------------------------------------
   SP_CancelarSolicitudPrestamo
   El propio usuario retira su solicitud mientras siga pendiente.
   -------------------------------------------------------------------------- */
CREATE PROCEDURE SP_CancelarSolicitudPrestamo
    @idSolicitud INT,
    @idUsuario   INT,
    @mensaje     VARCHAR(300) OUTPUT
AS
BEGIN
    SET NOCOUNT ON;

    IF NOT EXISTS (SELECT 1 FROM SolicitudPrestamo
                   WHERE idSolicitudPrestamo = @idSolicitud
                     AND idUsuario = @idUsuario
                     AND estado = 'Pendiente')
    BEGIN
        SET @mensaje = 'No se encontro una solicitud pendiente tuya con ese numero.';
        RETURN;
    END

    UPDATE SolicitudPrestamo
    SET estado         = 'Cancelada',
        fechaRespuesta = GETDATE()
    WHERE idSolicitudPrestamo = @idSolicitud;

    SET @mensaje = 'Solicitud cancelada.';
END
GO


/* ----------------------------------------------------------------------------
   Se reconstruye la vista de estadisticas para incluir las solicitudes de
   prestamo pendientes, que ahora son parte de la operacion diaria.
   -------------------------------------------------------------------------- */
ALTER VIEW VW_EstadisticasGenerales AS
SELECT
    (SELECT COUNT(*) FROM Material WHERE estado = 'Activo')          AS totalMateriales,
    (SELECT COUNT(*) FROM Ejemplar WHERE estado <> 'Baja')           AS totalEjemplares,
    (SELECT COUNT(*) FROM Ejemplar WHERE estado = 'Disponible')      AS ejemplaresDisponibles,
    (SELECT COUNT(*) FROM Ejemplar WHERE estado = 'Prestado')        AS ejemplaresPrestados,
    (SELECT COUNT(*) FROM Usuario  WHERE estado = 1)                 AS usuariosActivos,
    (SELECT COUNT(*) FROM Prestamo WHERE estado = 'Activo')          AS prestamosActivos,
    (SELECT COUNT(*) FROM VW_PrestamoDetalle
      WHERE estadoDetalle = 'Prestado' AND diasRetraso > 0)          AS prestamosRetrasados,
    (SELECT COUNT(*) FROM SolicitudAdquisicion WHERE estado = 'Pendiente') AS solicitudesPendientes,
    (SELECT COUNT(*) FROM SolicitudPrestamo    WHERE estado = 'Pendiente') AS solicitudesPrestamo,
    (SELECT ISNULL(SUM(monto), 0) FROM Multa WHERE estado = 'Pendiente')   AS multasPendientes;
GO


/* ============================================================================
   6. REGISTRO DE CUENTA POR EL PROPIO USUARIO
   La pantalla de acceso permite crear cuenta, pero solo de Estudiante o
   Profesor: las cuentas administrativas las sigue dando de alta el
   superadministrador desde el panel de gestión.
   ============================================================================ */

CREATE PROCEDURE SP_RegistrarCuenta
    @nombre        VARCHAR(150),
    @numeroControl VARCHAR(20),
    @correo        VARCHAR(100),
    @telefono      VARCHAR(20) = NULL,
    @usuario       VARCHAR(50),
    @passwordHash  VARCHAR(255),
    @rol           VARCHAR(30),
    @idUsuario     INT OUTPUT,
    @mensaje       VARCHAR(300) OUTPUT
AS
BEGIN
    SET NOCOUNT ON;

    SET @idUsuario = NULL;
    SET @mensaje   = NULL;

    -- Solo se permiten los dos roles de usuario final.
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

    -- Los índices únicos del esquema ya protegen estos casos, pero validarlos
    -- aquí permite devolver un mensaje entendible en lugar de un error de motor.
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
                             usuario, passwordHash, idRol, estado)
        VALUES (NULLIF(LTRIM(RTRIM(@numeroControl)), ''),
                LTRIM(RTRIM(@nombre)),
                NULLIF(LTRIM(RTRIM(@correo)), ''),
                NULLIF(LTRIM(RTRIM(@telefono)), ''),
                LTRIM(RTRIM(@usuario)),
                @passwordHash,
                @idRol,
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


/* ----------------------------------------------------------------------------
   VW_RolPublico
   Roles que se ofrecen en la pantalla de registro. Se consulta desde Java
   para no tener los nombres escritos a mano en la interfaz.
   -------------------------------------------------------------------------- */
CREATE VIEW VW_RolPublico AS
SELECT idRol, nombre, descripcion, maxMateriales, diasPrestamo, unidadPlazo, estado
FROM Rol
WHERE estado = 1 AND nombre IN ('Estudiante', 'Profesor');
GO
