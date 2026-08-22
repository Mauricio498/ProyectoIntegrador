/* ============================================================================
   BIBLIOTECA UTNG - ESTADÍSTICAS POR PERIODO
   Ejecutar DESPUÉS de 01_esquema.sql, 02_logica.sql y 03_datos_iniciales.sql

   Alimenta el módulo de reportes: series de tiempo para las gráficas y
   rankings acotados por rango de fechas.

   Todos los procedimientos reciben @desde y @hasta como DATE. El agrupamiento
   se controla con @periodo: 'Dia', 'Semana' o 'Mes'.
   ============================================================================ */

USE BibliotecaUTNG;
GO


/* ----------------------------------------------------------------------------
   FN_InicioPeriodo
   Normaliza una fecha al inicio de su día, semana o mes. Es lo que permite
   agrupar con un solo procedimiento en lugar de escribir tres.

   La semana se ancla en lunes sin depender de DATEFIRST, para que el reporte
   no cambie según la configuración de sesión del servidor.
   -------------------------------------------------------------------------- */
CREATE OR ALTER FUNCTION dbo.FN_InicioPeriodo (@fecha DATE, @periodo VARCHAR(10))
RETURNS DATE
AS
BEGIN
    IF @periodo = 'Mes'
        RETURN DATEFROMPARTS(YEAR(@fecha), MONTH(@fecha), 1);

    IF @periodo = 'Semana'
        RETURN DATEADD(DAY, -((DATEDIFF(DAY, '19000101', @fecha) + 0) % 7), @fecha);

    RETURN @fecha;
END
GO


/* ----------------------------------------------------------------------------
   SP_AccesosPorPeriodo
   Serie de tiempo de la tabla Acceso: cuántas entradas hubo y cuántas
   fallaron en cada día, semana o mes del rango.
   -------------------------------------------------------------------------- */
CREATE OR ALTER PROCEDURE SP_AccesosPorPeriodo
    @desde   DATE,
    @hasta   DATE,
    @periodo VARCHAR(10) = 'Dia'
AS
BEGIN
    SET NOCOUNT ON;

    SELECT
        dbo.FN_InicioPeriodo(CAST(A.fechaHora AS DATE), @periodo) AS periodo,
        COUNT(*)                                                    AS total,
        SUM(CASE WHEN A.resultado = 'Exitoso' THEN 1 ELSE 0 END)    AS exitosos,
        SUM(CASE WHEN A.resultado = 'Fallido' THEN 1 ELSE 0 END)    AS fallidos
    FROM Acceso A
    WHERE CAST(A.fechaHora AS DATE) BETWEEN @desde AND @hasta
      AND A.tipoAcceso = 'Login'
    GROUP BY dbo.FN_InicioPeriodo(CAST(A.fechaHora AS DATE), @periodo)
    ORDER BY periodo;
END
GO


/* ----------------------------------------------------------------------------
   SP_PrestamosPorPeriodo
   Préstamos generados y devoluciones registradas en cada periodo.
   -------------------------------------------------------------------------- */
CREATE OR ALTER PROCEDURE SP_PrestamosPorPeriodo
    @desde   DATE,
    @hasta   DATE,
    @periodo VARCHAR(10) = 'Dia'
AS
BEGIN
    SET NOCOUNT ON;

    WITH Prestados AS (
        SELECT dbo.FN_InicioPeriodo(CAST(P.fechaPrestamo AS DATE), @periodo) AS periodo,
               COUNT(*) AS cantidad
        FROM Prestamo P
        INNER JOIN DetallePrestamo DP ON DP.idPrestamo = P.idPrestamo
        WHERE CAST(P.fechaPrestamo AS DATE) BETWEEN @desde AND @hasta
        GROUP BY dbo.FN_InicioPeriodo(CAST(P.fechaPrestamo AS DATE), @periodo)
    ),
    Devueltos AS (
        SELECT dbo.FN_InicioPeriodo(DP.fechaDevolucion, @periodo) AS periodo,
               COUNT(*) AS cantidad
        FROM DetallePrestamo DP
        WHERE DP.fechaDevolucion BETWEEN @desde AND @hasta
        GROUP BY dbo.FN_InicioPeriodo(DP.fechaDevolucion, @periodo)
    )
    SELECT
        ISNULL(P.periodo, D.periodo)  AS periodo,
        ISNULL(P.cantidad, 0)         AS prestamos,
        ISNULL(D.cantidad, 0)         AS devoluciones
    FROM Prestados P
    FULL OUTER JOIN Devueltos D ON D.periodo = P.periodo
    ORDER BY periodo;
END
GO


/* ----------------------------------------------------------------------------
   SP_BusquedasPorPeriodo
   Cuántas búsquedas se hicieron en cada periodo del rango.
   -------------------------------------------------------------------------- */
CREATE OR ALTER PROCEDURE SP_BusquedasPorPeriodo
    @desde   DATE,
    @hasta   DATE,
    @periodo VARCHAR(10) = 'Dia'
AS
BEGIN
    SET NOCOUNT ON;

    SELECT
        dbo.FN_InicioPeriodo(CAST(B.fechaHora AS DATE), @periodo) AS periodo,
        COUNT(*)                                                      AS total,
        COUNT(DISTINCT B.idUsuario)                                   AS usuarios
    FROM Busqueda B
    WHERE CAST(B.fechaHora AS DATE) BETWEEN @desde AND @hasta
    GROUP BY dbo.FN_InicioPeriodo(CAST(B.fechaHora AS DATE), @periodo)
    ORDER BY periodo;
END
GO


/* ----------------------------------------------------------------------------
   SP_TerminosMasBuscados
   Ranking de términos dentro de un rango de fechas.
   -------------------------------------------------------------------------- */
CREATE OR ALTER PROCEDURE SP_TerminosMasBuscados
    @desde  DATE,
    @hasta  DATE,
    @limite INT = 10
AS
BEGIN
    SET NOCOUNT ON;

    SELECT TOP (@limite)
        LTRIM(RTRIM(B.termino))     AS termino,
        COUNT(*)                    AS cantidad,
        AVG(CAST(ISNULL(B.cantidadResultados, 0) AS DECIMAL(10,2))) AS promedioResultados,
        MAX(B.fechaHora)            AS ultimaVez
    FROM Busqueda B
    WHERE CAST(B.fechaHora AS DATE) BETWEEN @desde AND @hasta
      AND LTRIM(RTRIM(ISNULL(B.termino, ''))) <> ''
    GROUP BY LTRIM(RTRIM(B.termino))
    ORDER BY cantidad DESC, termino ASC;
END
GO


/* ----------------------------------------------------------------------------
   SP_MaterialesMasPrestados
   Ranking de materiales por préstamos dentro del rango.
   -------------------------------------------------------------------------- */
CREATE OR ALTER PROCEDURE SP_MaterialesMasPrestados
    @desde  DATE,
    @hasta  DATE,
    @limite INT = 10
AS
BEGIN
    SET NOCOUNT ON;

    SELECT TOP (@limite)
        M.idMaterial,
        M.titulo,
        ISNULL(C.autores, '')       AS autores,
        C.tipoMaterial,
        COUNT(DP.idDetallePrestamo) AS vecesPrestado
    FROM DetallePrestamo DP
    INNER JOIN Prestamo           P ON P.idPrestamo  = DP.idPrestamo
    INNER JOIN Ejemplar           E ON E.idEjemplar  = DP.idEjemplar
    INNER JOIN Material           M ON M.idMaterial  = E.idMaterial
    INNER JOIN VW_CatalogoMaterial C ON C.idMaterial = M.idMaterial
    WHERE CAST(P.fechaPrestamo AS DATE) BETWEEN @desde AND @hasta
    GROUP BY M.idMaterial, M.titulo, C.autores, C.tipoMaterial
    ORDER BY vecesPrestado DESC, M.titulo ASC;
END
GO


/* ----------------------------------------------------------------------------
   SP_PrestamosPorRolEnRango
   Distribución de préstamos por rol: alimenta la gráfica de pastel.
   -------------------------------------------------------------------------- */
CREATE OR ALTER PROCEDURE SP_PrestamosPorRolEnRango
    @desde DATE,
    @hasta DATE
AS
BEGIN
    SET NOCOUNT ON;

    SELECT
        R.nombre                    AS rol,
        COUNT(DP.idDetallePrestamo) AS cantidad
    FROM Rol R
    LEFT JOIN Usuario        U  ON U.idRol      = R.idRol
    LEFT JOIN Prestamo       P  ON P.idUsuario  = U.idUsuario
                               AND CAST(P.fechaPrestamo AS DATE) BETWEEN @desde AND @hasta
    LEFT JOIN DetallePrestamo DP ON DP.idPrestamo = P.idPrestamo
    GROUP BY R.nombre
    HAVING COUNT(DP.idDetallePrestamo) > 0
    ORDER BY cantidad DESC;
END
GO


/* ----------------------------------------------------------------------------
   SP_ResumenRango
   Números del encabezado del módulo de reportes, para el rango elegido.
   -------------------------------------------------------------------------- */
CREATE OR ALTER PROCEDURE SP_ResumenRango
    @desde DATE,
    @hasta DATE
AS
BEGIN
    SET NOCOUNT ON;

    SELECT
        (SELECT COUNT(*) FROM Acceso
          WHERE tipoAcceso = 'Login' AND resultado = 'Exitoso'
            AND CAST(fechaHora AS DATE) BETWEEN @desde AND @hasta)      AS accesos,

        (SELECT COUNT(DISTINCT idUsuario) FROM Acceso
          WHERE tipoAcceso = 'Login' AND resultado = 'Exitoso'
            AND idUsuario IS NOT NULL
            AND CAST(fechaHora AS DATE) BETWEEN @desde AND @hasta)      AS usuariosDistintos,

        (SELECT COUNT(*) FROM Busqueda
          WHERE CAST(fechaHora AS DATE) BETWEEN @desde AND @hasta)    AS busquedas,

        (SELECT COUNT(DP.idDetallePrestamo)
           FROM DetallePrestamo DP
           INNER JOIN Prestamo P ON P.idPrestamo = DP.idPrestamo
          WHERE CAST(P.fechaPrestamo AS DATE) BETWEEN @desde AND @hasta)  AS prestamos,

        (SELECT COUNT(*) FROM DetallePrestamo
          WHERE fechaDevolucion BETWEEN @desde AND @hasta)                AS devoluciones,

        (SELECT COUNT(*) FROM Multa
          WHERE CAST(fechaGeneracion AS DATE) BETWEEN @desde AND @hasta)  AS multasGeneradas,

        (SELECT ISNULL(SUM(monto), 0) FROM Pago
          WHERE CAST(fechaPago AS DATE) BETWEEN @desde AND @hasta)        AS montoCobrado;
END
GO


PRINT 'Estadísticas por periodo instaladas.';
GO
