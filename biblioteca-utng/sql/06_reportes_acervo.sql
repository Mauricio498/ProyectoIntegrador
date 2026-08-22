/* ============================================================================
   BIBLIOTECA UTNG - REPORTES DEL ACERVO
   Ejecutar DESPUÉS de los scripts 01 a 05.

   Los reportes de 04_estadisticas.sql miden ACTIVIDAD (accesos, préstamos,
   búsquedas) dentro de un rango de fechas. Estos miden el ACERVO: qué hay en
   la biblioteca hoy, cómo está repartido y qué está fallando.

   Por eso no reciben rango de fechas: describen una foto del momento.
   ============================================================================ */

USE BibliotecaUTNG;
GO


/* ----------------------------------------------------------------------------
   SP_AcervoResumen
   Cifras de encabezado del reporte de acervo.
   -------------------------------------------------------------------------- */
CREATE OR ALTER PROCEDURE SP_AcervoResumen
AS
BEGIN
    SET NOCOUNT ON;

    SELECT
        (SELECT COUNT(*) FROM Material WHERE estado = 'Activo')             AS materialesActivos,
        (SELECT COUNT(*) FROM Material WHERE estado <> 'Activo')            AS materialesBaja,
        (SELECT COUNT(*) FROM Ejemplar WHERE estado <> 'Baja')              AS ejemplaresVigentes,
        (SELECT COUNT(*) FROM Ejemplar WHERE estado = 'Disponible')         AS ejemplaresDisponibles,
        (SELECT COUNT(*) FROM Ejemplar WHERE estado = 'Prestado')           AS ejemplaresPrestados,
        (SELECT COUNT(*) FROM Ejemplar WHERE estado = 'EnReparacion')       AS ejemplaresReparacion,
        (SELECT COUNT(*) FROM Ejemplar WHERE estado = 'Extraviado')         AS ejemplaresExtraviados,
        (SELECT COUNT(*) FROM Autor)                                        AS autores,
        (SELECT COUNT(*) FROM Editorial WHERE estado = 1)                   AS editoriales,

        -- Materiales activos que no tienen ni un ejemplar utilizable
        (SELECT COUNT(*)
           FROM Material M
          WHERE M.estado = 'Activo'
            AND NOT EXISTS (SELECT 1 FROM Ejemplar E
                             WHERE E.idMaterial = M.idMaterial
                               AND E.estado <> 'Baja'))                     AS materialesSinEjemplar,

        -- Cuántos ejemplares hay en promedio por material
        (SELECT CAST(
                    CASE WHEN COUNT(DISTINCT M.idMaterial) = 0 THEN 0
                         ELSE COUNT(E.idEjemplar) * 1.0 / COUNT(DISTINCT M.idMaterial) END
                AS DECIMAL(10,2))
           FROM Material M
           LEFT JOIN Ejemplar E ON E.idMaterial = M.idMaterial AND E.estado <> 'Baja'
          WHERE M.estado = 'Activo')                                        AS ejemplaresPorMaterial;
END
GO


/* ----------------------------------------------------------------------------
   SP_AcervoPorTipo
   Cuántos materiales y ejemplares hay de cada tipo.
   -------------------------------------------------------------------------- */
CREATE OR ALTER PROCEDURE SP_AcervoPorTipo
AS
BEGIN
    SET NOCOUNT ON;

    SELECT
        TM.nombre                       AS tipo,
        COUNT(DISTINCT M.idMaterial)    AS materiales,
        COUNT(E.idEjemplar)             AS ejemplares,
        SUM(CASE WHEN E.estado = 'Disponible' THEN 1 ELSE 0 END) AS disponibles
    FROM TipoMaterial TM
    LEFT JOIN Material M ON M.idTipoMaterial = TM.idTipoMaterial
                        AND M.estado = 'Activo'
    LEFT JOIN Ejemplar E ON E.idMaterial = M.idMaterial
                        AND E.estado <> 'Baja'
    GROUP BY TM.nombre
    ORDER BY materiales DESC, tipo;
END
GO


/* ----------------------------------------------------------------------------
   SP_AcervoPorEstadoEjemplar
   Reparto de los ejemplares según su estado físico.
   -------------------------------------------------------------------------- */
CREATE OR ALTER PROCEDURE SP_AcervoPorEstadoEjemplar
AS
BEGIN
    SET NOCOUNT ON;

    SELECT estado, COUNT(*) AS cantidad
    FROM Ejemplar
    GROUP BY estado
    ORDER BY cantidad DESC;
END
GO


/* ----------------------------------------------------------------------------
   SP_AcervoPorBiblioteca
   Dónde está físicamente el acervo.
   -------------------------------------------------------------------------- */
CREATE OR ALTER PROCEDURE SP_AcervoPorBiblioteca
AS
BEGIN
    SET NOCOUNT ON;

    SELECT
        B.nombre                AS biblioteca,
        COUNT(E.idEjemplar)     AS ejemplares,
        SUM(CASE WHEN E.estado = 'Disponible' THEN 1 ELSE 0 END) AS disponibles
    FROM Biblioteca B
    LEFT JOIN Ejemplar E ON E.idBiblioteca = B.idBiblioteca AND E.estado <> 'Baja'
    WHERE B.estado = 1
    GROUP BY B.nombre
    ORDER BY ejemplares DESC;
END
GO


/* ----------------------------------------------------------------------------
   SP_AcervoPorClasificacion
   Agrupa por el primer tramo de la signatura topográfica, que es el que
   identifica la materia. Sirve para ver si el acervo está desbalanceado.
   -------------------------------------------------------------------------- */
CREATE OR ALTER PROCEDURE SP_AcervoPorClasificacion
    @limite INT = 12
AS
BEGIN
    SET NOCOUNT ON;

    SELECT TOP (@limite)
        CASE
            WHEN M.clasificacion IS NULL OR LTRIM(RTRIM(M.clasificacion)) = ''
                THEN 'Sin clasificar'
            WHEN CHARINDEX(' ', LTRIM(M.clasificacion)) > 0
                THEN LEFT(LTRIM(M.clasificacion), CHARINDEX(' ', LTRIM(M.clasificacion)) - 1)
            ELSE LTRIM(RTRIM(M.clasificacion))
        END                     AS clasificacion,
        COUNT(*)                AS materiales
    FROM Material M
    WHERE M.estado = 'Activo'
    GROUP BY
        CASE
            WHEN M.clasificacion IS NULL OR LTRIM(RTRIM(M.clasificacion)) = ''
                THEN 'Sin clasificar'
            WHEN CHARINDEX(' ', LTRIM(M.clasificacion)) > 0
                THEN LEFT(LTRIM(M.clasificacion), CHARINDEX(' ', LTRIM(M.clasificacion)) - 1)
            ELSE LTRIM(RTRIM(M.clasificacion))
        END
    ORDER BY materiales DESC, clasificacion;
END
GO


/* ----------------------------------------------------------------------------
   SP_AcervoPorEditorial
   Editoriales con más títulos en el acervo.
   -------------------------------------------------------------------------- */
CREATE OR ALTER PROCEDURE SP_AcervoPorEditorial
    @limite INT = 10
AS
BEGIN
    SET NOCOUNT ON;

    SELECT TOP (@limite)
        ISNULL(E.nombre, 'Sin editorial') AS editorial,
        COUNT(*)                          AS materiales
    FROM Material M
    LEFT JOIN Editorial E ON E.idEditorial = M.idEditorial
    WHERE M.estado = 'Activo'
    GROUP BY ISNULL(E.nombre, 'Sin editorial')
    ORDER BY materiales DESC, editorial;
END
GO


/* ----------------------------------------------------------------------------
   SP_AcervoInventario
   Listado completo para revisar o exportar: un renglón por material, con sus
   ejemplares contados por estado.
   -------------------------------------------------------------------------- */
CREATE OR ALTER PROCEDURE SP_AcervoInventario
    @soloConProblemas BIT = 0
AS
BEGIN
    SET NOCOUNT ON;

    SELECT
        M.idMaterial,
        M.titulo,
        ISNULL(M.isbn, '')            AS isbn,
        ISNULL(M.clasificacion, '')   AS clasificacion,
        TM.nombre                     AS tipo,
        ISNULL(E.nombre, '')          AS editorial,
        ISNULL(C.autores, '')         AS autores,
        M.anioPublicacion,
        M.estado                      AS estadoMaterial,

        ISNULL(EJ.total, 0)           AS ejemplares,
        ISNULL(EJ.disponibles, 0)     AS disponibles,
        ISNULL(EJ.prestados, 0)       AS prestados,
        ISNULL(EJ.noUtilizables, 0)   AS noUtilizables,

        ISNULL(PR.vecesPrestado, 0)   AS vecesPrestado,

        /* Señala qué revisar de cada material:
             - sin ejemplares utilizables no se puede prestar aunque esté activo
             - nunca prestado puede indicar que está mal clasificado */
        CASE
            WHEN ISNULL(EJ.total, 0) = 0                 THEN 'Sin ejemplares'
            WHEN ISNULL(EJ.disponibles, 0) = 0
             AND ISNULL(EJ.prestados, 0) = 0             THEN 'Ninguno utilizable'
            WHEN ISNULL(PR.vecesPrestado, 0) = 0         THEN 'Nunca prestado'
            ELSE ''
        END                           AS observacion

    FROM Material M
    INNER JOIN TipoMaterial      TM ON TM.idTipoMaterial = M.idTipoMaterial
    LEFT  JOIN Editorial         E  ON E.idEditorial     = M.idEditorial
    LEFT  JOIN VW_CatalogoMaterial C ON C.idMaterial     = M.idMaterial

    /* Los conteos van en subconsultas agrupadas aparte y no como SUM sobre el
       join: si se juntaran ejemplares y préstamos en la misma consulta, cada
       ejemplar multiplicaría las filas y los totales saldrían inflados. */
    LEFT JOIN (
        SELECT idMaterial,
               COUNT(*)                                                  AS total,
               SUM(CASE WHEN estado = 'Disponible' THEN 1 ELSE 0 END)    AS disponibles,
               SUM(CASE WHEN estado = 'Prestado'   THEN 1 ELSE 0 END)    AS prestados,
               SUM(CASE WHEN estado IN ('EnReparacion', 'Extraviado')
                        THEN 1 ELSE 0 END)                               AS noUtilizables
        FROM Ejemplar
        WHERE estado <> 'Baja'
        GROUP BY idMaterial
    ) EJ ON EJ.idMaterial = M.idMaterial

    LEFT JOIN (
        SELECT E2.idMaterial, COUNT(*) AS vecesPrestado
        FROM DetallePrestamo DP
        INNER JOIN Ejemplar E2 ON E2.idEjemplar = DP.idEjemplar
        GROUP BY E2.idMaterial
    ) PR ON PR.idMaterial = M.idMaterial

    WHERE @soloConProblemas = 0
       OR ISNULL(EJ.total, 0) = 0
       OR ISNULL(PR.vecesPrestado, 0) = 0
       OR ISNULL(EJ.noUtilizables, 0) > 0

    ORDER BY M.titulo;
END
GO


PRINT 'Reportes del acervo instalados.';
GO
