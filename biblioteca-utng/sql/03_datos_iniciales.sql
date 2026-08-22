/* ============================================================================
   BIBLIOTECA UTNG - DATOS INICIALES / DEMO
   Ejecutar DESPUES de 01_esquema.sql y 02_logica.sql

   Los passwordHash son SHA-256 en hexadecimal en minúsculas, calculados igual
   que utiliza utng.biblioteca.util.PasswordUtil.hash(...)

   Credenciales de prueba:
     root       / Root123*      -> SuperAdministrador
     admin      / Admin123*     -> Administrador
     profesor   / Profe123*     -> Profesor
     estudiante / Estudia123*   -> Estudiante
     estadia    / Estadia123*   -> Estudiante Estadia

   El rol Invitado no tiene cuenta: se entra con el botón "Entrar sin cuenta"
   de la pantalla de acceso, que abre la biblioteca en modo consulta.
   ============================================================================ */

USE BibliotecaUTNG;
GO

/* ---------------------------------------------------------------------------
   ROLES
   maxMateriales / diasPrestamo son los limites que aplica SP_RegistrarPrestamo.
   maxMateriales = 0 significa "este rol no solicita prestamos".
   --------------------------------------------------------------------------- */
INSERT INTO Rol (nombre, descripcion, maxMateriales, diasPrestamo, unidadPlazo, estado) VALUES
    ('SuperAdministrador', 'Control total del sistema y de los catálogos',
        0, 0, 'HABILES', 1),

    ('Administrador',      'Opera préstamos, devoluciones, multas y administra el acervo',
        0, 0, 'HABILES', 1),

    -- 10 días hábiles = dos semanas de calendario
    ('Profesor',           'Personal docente de la institución',
        3, 10, 'HABILES', 1),

    -- 5 días hábiles = una semana de calendario
    ('Estudiante',         'Alumno inscrito en la institución',
        3, 5,  'HABILES', 1),

    -- Alumno en estadía: está fuera del plantel, por eso el plazo es de un mes
    ('Estudiante Estadia', 'Alumno que cursa una estadía fuera del plantel',
        3, 1,  'MESES',   1),

    ('Invitado',           'Consulta del catálogo sin derecho a préstamos',
        0, 0, 'HABILES', 1);
GO

/* ---------------------------------------------------------------------------
   BIBLIOTECAS
   --------------------------------------------------------------------------- */
INSERT INTO Biblioteca (nombre, ubicacion) VALUES
    ('Biblioteca Central',      'Edificio A - Biblioteca Central'),
    ('Sala de Consulta TIC',    'Edificio C - Sala de Consulta TIC');
GO

/* ---------------------------------------------------------------------------
   USUARIOS
   --------------------------------------------------------------------------- */
DECLARE @rSuper INT = (SELECT idRol FROM Rol WHERE nombre = 'SuperAdministrador');
DECLARE @rAdmin INT = (SELECT idRol FROM Rol WHERE nombre = 'Administrador');
DECLARE @rProf  INT = (SELECT idRol FROM Rol WHERE nombre = 'Profesor');
DECLARE @rEst   INT = (SELECT idRol FROM Rol WHERE nombre = 'Estudiante');
DECLARE @rEsta  INT = (SELECT idRol FROM Rol WHERE nombre = 'Estudiante Estadia');

INSERT INTO Usuario (numeroControl, nombre, correo, telefono, usuario, passwordHash, idRol) VALUES
    (NULL,'Dirección de Sistemas', 'sistemas@utng.edu.mx', NULL,'root','9e63ff7c54ee9343dcd9f62fe7bd79fe031b42f5f56d9d674e630194b2ab4b47', @rSuper),
    ('EMP-0001','Laura Gutiérrez Rivas',  'lgutierrez@utng.edu.mx','4181000001','admin',      '0a5bc3e342432f1bad92ffd51b785343ec72906cdba6a26131060b008e786656', @rAdmin),
    ('DOC-0114','Ricardo Salas Méndez',   'rsalas@utng.edu.mx',    '4181000114','profesor',   '4d41f8feee69fbe1ae65ad01aa2106f83611c9e8a39df1629185e795dd38d774', @rProf),
    ('A00123456','Ana Martínez Jasso',    'amartinez@utng.edu.mx', '4181234567','estudiante', 'b6350f881a8b44bd20333c7107effa423ed52f678f6f5da849cb0c13e60e1dd7', @rEst),
    ('A00123460','Sofía Herrera Vega',    'sherrera@utng.edu.mx',  '4181234570','estadia',    'fa5559ad7422dc607c952836d4f52e2f29e0ca0b53c856f890022d51e6fb9e8e', @rEsta);

-- Usuarios adicionales para poblar listados y realizar pruebas
INSERT INTO Usuario (numeroControl, nombre, correo, telefono, usuario, passwordHash, idRol) VALUES
    ('A00123457','Diego Ramírez Ortiz',   'dramirez@utng.edu.mx',  '4181234568', NULL, NULL, @rEst),
    ('A00123458','Fernanda López Cruz',   'flopez@utng.edu.mx',    '4181234569', NULL, NULL, @rEst),
    ('A00123459','Miguel Ángel Torres',   'mtorres@utng.edu.mx',   NULL,         NULL, NULL, @rEst),
    ('DOC-0115','Patricia Núñez Vela',    'pnunez@utng.edu.mx',    NULL,         NULL, NULL, @rProf),
    ('A00123461','Jorge Hernández Soto',    'jhernandez@utng.edu.mx', '4181234571', NULL, NULL, @rEst),
    ('A00123462','Mariana Castillo Reyes',  'mcastillo@utng.edu.mx',  '4181234572', NULL, NULL, @rEst),
    ('DOC-0116','Alejandro Gómez Ruiz',     'agomez@utng.edu.mx',     NULL,         NULL, NULL, @rProf);
GO

/* ---------------------------------------------------------------------------
   TIPOS DE MATERIAL
   --------------------------------------------------------------------------- */
INSERT INTO TipoMaterial (nombre, descripcion, esPrestable) VALUES
    ('Libro',    'Libro impreso del acervo general y especializado',                      1),
    ('Tesis',    'Trabajos de titulación y reportes de estadía',           1),
    ('Consulta', 'Obras de consulta en sala, como diccionarios y enciclopedias', 0),
    ('Otros',    'Revistas, publicaciones y otros soportes documentales',           1);
GO

/* ---------------------------------------------------------------------------
   EDITORIALES Y AUTORES
   --------------------------------------------------------------------------- */
INSERT INTO Editorial (nombre) VALUES
    ('Pearson'), ('McGraw-Hill'), ('Addison-Wesley'), ('O''Reilly Media'),
    ('Alfaomega'), ('Prentice Hall'), ('Fondo de Cultura Económica'),
    ('Microsoft Press'), ('Cengage Learning');
GO

INSERT INTO Autor (nombre) VALUES
    ('Robert C. Martin'), ('Martin Fowler'), ('Erich Gamma'), ('Richard Helm'),
    ('Abraham Silberschatz'), ('Andrew S. Tanenbaum'), ('Thomas H. Cormen'),
    ('Joshua Bloch'), ('Herbert Schildt'), ('Gabriel García Márquez'),
    ('Juan Rulfo'), ('Roger S. Pressman'), ('Ian Sommerville'), ('Kathy Sierra'),
    ('Steve McConnell'), ('Ramez Elmasri'), ('Carlos Coronel'), ('Sergio Gutiérrez');
GO

/* ---------------------------------------------------------------------------
   MATERIALES
   --------------------------------------------------------------------------- */
DECLARE @tLibro   INT = (SELECT idTipoMaterial FROM TipoMaterial WHERE nombre = 'Libro');
DECLARE @tConsul  INT = (SELECT idTipoMaterial FROM TipoMaterial WHERE nombre = 'Consulta');
DECLARE @tTesis   INT = (SELECT idTipoMaterial FROM TipoMaterial WHERE nombre = 'Tesis');

INSERT INTO Material (titulo, isbn, anioPublicacion, clasificacion, idEditorial, idTipoMaterial) VALUES
    ('Clean Code',                              '978-0132350884', 2008, '005.1 MAR',
        (SELECT idEditorial FROM Editorial WHERE nombre='Prentice Hall'), @tLibro),
    ('Refactoring: Improving the Design of Existing Code','978-0134757599',2018,'005.16 FOW',
        (SELECT idEditorial FROM Editorial WHERE nombre='Addison-Wesley'), @tLibro),
    ('Design Patterns',                         '978-0201633610', 1994, '005.12 GAM',
        (SELECT idEditorial FROM Editorial WHERE nombre='Addison-Wesley'), @tLibro),
    ('Fundamentos de Bases de Datos',           '978-6071503695', 2014, '005.74 SIL',
        (SELECT idEditorial FROM Editorial WHERE nombre='McGraw-Hill'), @tLibro),
    ('Redes de Computadoras',                   '978-6073238281', 2021, '004.6 TAN',
        (SELECT idEditorial FROM Editorial WHERE nombre='Pearson'), @tLibro),
    ('Introducción a Algoritmos',              '978-0262046305', 2022, '005.1 COR',
        (SELECT idEditorial FROM Editorial WHERE nombre='McGraw-Hill'), @tLibro),
    ('Effective Java',                          '978-0134685991', 2018, '005.133 BLO',
        (SELECT idEditorial FROM Editorial WHERE nombre='Addison-Wesley'), @tLibro),
    ('Java: The Complete Reference',            '978-1260463415', 2021, '005.133 SCH',
        (SELECT idEditorial FROM Editorial WHERE nombre='McGraw-Hill'), @tLibro),
    ('Ingeniería del Software',                 '978-8490355374', 2016, '005.1 SOM',
        (SELECT idEditorial FROM Editorial WHERE nombre='Pearson'), @tLibro),
    ('Ingeniería del Software: Un enfoque práctico','978-6071513878',2010,'005.1 PRE',
        (SELECT idEditorial FROM Editorial WHERE nombre='McGraw-Hill'), @tLibro),
    ('Cien años de soledad',                    '978-6073139977', 1967, 'M863 GAR',
        (SELECT idEditorial FROM Editorial WHERE nombre='Fondo de Cultura Económica'), @tLibro),
    ('Pedro Páramo',                            '978-6071607034', 1955, 'M863 RUL',
        (SELECT idEditorial FROM Editorial WHERE nombre='Fondo de Cultura Económica'), @tLibro),
    ('Diccionario Enciclopédico Ilustrado',     NULL,             2019, 'REF 030 DIC',
        (SELECT idEditorial FROM Editorial WHERE nombre='Alfaomega'), @tConsul),
    ('Sistema de control de inventarios para PyMES', NULL,        2024, 'TE 2024 TIID 015',
        NULL, @tTesis),
    ('Code Complete',                            '978-0735619678', 2004, '005.1 MCC',
        (SELECT idEditorial FROM Editorial WHERE nombre='Microsoft Press'), @tLibro),
    ('Fundamentos de Sistemas de Bases de Datos', '978-6071503817', 2017, '005.74 ELM',
        (SELECT idEditorial FROM Editorial WHERE nombre='Pearson'), @tLibro),
    ('Bases de Datos: Diseño, Implementación y Administración', NULL, 2016, '005.74 COR',
        (SELECT idEditorial FROM Editorial WHERE nombre='Cengage Learning'), @tLibro),
    ('Desarrollo de Software: Prácticas y Principios', NULL, 2023, '005.1 GUT',
        (SELECT idEditorial FROM Editorial WHERE nombre='Alfaomega'), @tLibro);
GO

/* ---------------------------------------------------------------------------
   AUTORÍA (MaterialAutor)
   --------------------------------------------------------------------------- */
INSERT INTO MaterialAutor (idMaterial, idAutor)
SELECT M.idMaterial, A.idAutor
FROM (VALUES
    ('Clean Code',                                        'Robert C. Martin'),
    ('Refactoring: Improving the Design of Existing Code','Martin Fowler'),
    ('Design Patterns',                                   'Erich Gamma'),
    ('Design Patterns',                                   'Richard Helm'),
    ('Fundamentos de Bases de Datos',                     'Abraham Silberschatz'),
    ('Redes de Computadoras',                             'Andrew S. Tanenbaum'),
    ('Introducción a Algoritmos',                        'Thomas H. Cormen'),
    ('Effective Java',                                    'Joshua Bloch'),
    ('Java: The Complete Reference',                      'Herbert Schildt'),
    ('Ingeniería del Software',                           'Ian Sommerville'),
    ('Ingeniería del Software: Un enfoque práctico',      'Roger S. Pressman'),
    ('Cien años de soledad',                              'Gabriel García Márquez'),
    ('Pedro Páramo',                                      'Juan Rulfo'),
    ('Code Complete',                                    'Steve McConnell'),
    ('Fundamentos de Sistemas de Bases de Datos',        'Ramez Elmasri'),
    ('Bases de Datos: Diseño, Implementación y Administración', 'Carlos Coronel'),
    ('Desarrollo de Software: Prácticas y Principios',   'Sergio Gutiérrez')
) AS V(titulo, autor)
INNER JOIN Material M ON M.titulo = V.titulo
INNER JOIN Autor    A ON A.nombre = V.autor;
GO

/* ---------------------------------------------------------------------------
   EJEMPLARES
   Se generan 3 ejemplares por material (2 para la obra de consulta y la tesis).
   --------------------------------------------------------------------------- */
DECLARE @bCentral INT = (SELECT idBiblioteca FROM Biblioteca WHERE nombre = 'Biblioteca Central');
DECLARE @bTIC     INT = (SELECT idBiblioteca FROM Biblioteca WHERE nombre = 'Sala de Consulta TIC');

INSERT INTO Ejemplar (idMaterial, idBiblioteca, codigoEjemplar, numeroAdquisicion, fechaIngreso, estado)
SELECT
    M.idMaterial,
    CASE WHEN N.n % 3 = 0 THEN @bTIC ELSE @bCentral END,
    'UTNG-' + RIGHT('0000' + CAST(M.idMaterial AS VARCHAR(4)), 4) + '-' + CAST(N.n AS VARCHAR(2)),
    'ADQ-2025-' + RIGHT('000' + CAST(M.idMaterial AS VARCHAR(4)), 3),
    DATEADD(DAY, -M.idMaterial * 7, CAST(GETDATE() AS DATE)),
    'Disponible'
FROM Material M
CROSS JOIN (VALUES (1), (2), (3)) AS N(n)
INNER JOIN TipoMaterial TM ON TM.idTipoMaterial = M.idTipoMaterial
WHERE TM.nombre = 'Libro' OR N.n <= 2;
GO

/* ---------------------------------------------------------------------------
   ADQUISICIONES
   --------------------------------------------------------------------------- */
DECLARE @admin INT = (SELECT idUsuario FROM Usuario WHERE usuario = 'admin');

INSERT INTO Adquisicion (fechaAdquisicion, tipoAdquisicion, proveedor, idResponsable, observaciones)
VALUES (DATEADD(MONTH, -3, CAST(GETDATE() AS DATE)), 'Compra', 'Librería Universitaria S.A.', @admin,
        'Compra anual de acervo para las carreras del área de TIC'),
       (DATEADD(MONTH, -1, CAST(GETDATE() AS DATE)), 'Donacion', 'Exalumnos de la generación 2020', @admin,
        'Donación de literatura general y material de consulta');

INSERT INTO DetalleAdquisicion (idAdquisicion, idMaterial, cantidad)
SELECT 1, idMaterial, 3 FROM Material WHERE titulo IN
    ('Clean Code', 'Effective Java', 'Fundamentos de Bases de Datos');
INSERT INTO DetalleAdquisicion (idAdquisicion, idMaterial, cantidad)
SELECT 2, idMaterial, 3 FROM Material WHERE titulo IN
    ('Cien años de soledad', 'Pedro Paramo');
GO

/* ---------------------------------------------------------------------------
   SOLICITUDES DE ADQUISICIÓN
   --------------------------------------------------------------------------- */
INSERT INTO SolicitudAdquisicion (idUsuario, titulo, autor, editorial, isbn, descripcion)
SELECT idUsuario, 'The Pragmatic Programmer', 'David Thomas', 'Addison-Wesley', '978-0135957059',
       'Bibliografía complementaria para la materia de Ingeniería de Software.'
FROM Usuario WHERE usuario = 'estudiante';

INSERT INTO SolicitudAdquisicion (idUsuario, titulo, autor, editorial, isbn, descripcion)
SELECT idUsuario, 'Domain-Driven Design', 'Eric Evans', 'Addison-Wesley', '978-0321125217',
       'Bibliografía complementaria para el cuatrimestre y para el desarrollo de proyectos.'
FROM Usuario WHERE usuario = 'profesor';
GO

/* ---------------------------------------------------------------------------
   PRÉSTAMOS DE EJEMPLO
   Se generan con el procedimiento real para que respeten las reglas y los
   triggers de estado de los ejemplares.
   --------------------------------------------------------------------------- */
DECLARE @idEst   INT = (SELECT idUsuario FROM Usuario WHERE usuario = 'estudiante');
DECLARE @idAdmin INT = (SELECT idUsuario FROM Usuario WHERE usuario = 'admin');
DECLARE @idProf  INT = (SELECT idUsuario FROM Usuario WHERE usuario = 'profesor');

DECLARE @e1 INT = (SELECT MIN(idEjemplar) FROM Ejemplar E
                   INNER JOIN Material M ON M.idMaterial = E.idMaterial
                   WHERE M.titulo = 'Clean Code');
DECLARE @e2 INT = (SELECT MIN(idEjemplar) FROM Ejemplar E
                   INNER JOIN Material M ON M.idMaterial = E.idMaterial
                   WHERE M.titulo = 'Effective Java');
DECLARE @e3 INT = (SELECT MIN(idEjemplar) FROM Ejemplar E
                   INNER JOIN Material M ON M.idMaterial = E.idMaterial
                   WHERE M.titulo = 'Redes de Computadoras');

DECLARE @idPrestamo INT, @msg VARCHAR(300);

-- Préstamo del estudiante (2 ejemplares)
DECLARE @lista VARCHAR(1000) = CAST(@e1 AS VARCHAR(10)) + ',' + CAST(@e2 AS VARCHAR(10));
EXEC SP_RegistrarPrestamo
     @idUsuario       = @idEst,
     @idAdministrador = @idAdmin,
     @idsEjemplares   = @lista,
     @idPrestamo      = @idPrestamo OUTPUT,
     @mensaje         = @msg OUTPUT;
PRINT 'Prestamo estudiante: ' + ISNULL(@msg, '');

-- Préstamo del profesor (1 ejemplar) con fecha limite ya vencida
EXEC SP_RegistrarPrestamo
     @idUsuario       = @idProf,
     @idAdministrador = @idAdmin,
     @idsEjemplares   = @e3,
     @idPrestamo      = @idPrestamo OUTPUT,
     @mensaje         = @msg OUTPUT;
PRINT 'Prestamo profesor: ' + ISNULL(@msg, '');

UPDATE Prestamo
SET fechaPrestamo = DATEADD(DAY, -40, GETDATE()),
    fechaLimite   = DATEADD(DAY, -10, CAST(GETDATE() AS DATE))
WHERE idPrestamo = @idPrestamo;
GO

DECLARE @afectados INT;
EXEC SP_ActualizarPrestamosVencidos @afectados = @afectados OUTPUT;
PRINT 'Préstamos marcados como vencidos: ' + CAST(@afectados AS VARCHAR(10));
GO

/* ---------------------------------------------------------------------------
   FAVORITOS
   --------------------------------------------------------------------------- */
INSERT INTO Favorito (idUsuario, idMaterial)
SELECT U.idUsuario, M.idMaterial
FROM Usuario U
CROSS JOIN Material M
WHERE U.usuario = 'estudiante'
  AND M.titulo IN ('Design Patterns', 'Cien años de soledad', 'Introducción a Algoritmos');
GO


/* ---------------------------------------------------------------------------
   SOLICITUDES DE PRÉSTAMO PENDIENTES
   Sirven para que la bandeja del administrador no aparezca vacía.
   --------------------------------------------------------------------------- */
DECLARE @uEst INT = (SELECT idUsuario FROM Usuario WHERE usuario = 'estudiante');
DECLARE @uPro INT = (SELECT idUsuario FROM Usuario WHERE usuario = 'profesor');
DECLARE @idSol INT, @msgSol VARCHAR(300);

DECLARE @idMatReg INT;

SELECT @idMatReg = idMaterial 
FROM Material 
WHERE titulo = 'Design Patterns';

EXEC SP_SolicitarPrestamo
     @idUsuario   = @uEst,
     @idMaterial  = @idMatReg,
     @idSolicitud = @idSol OUTPUT,
     @mensaje     = @msgSol OUTPUT;
PRINT 'Solicitud del estudiante: ' + ISNULL(@msgSol, '');

DECLARE @idMatProf INT;

SELECT @idMatProf = idMaterial
FROM Material
WHERE titulo = 'Introducción a Algoritmos';

EXEC SP_SolicitarPrestamo
     @idUsuario   = @uPro,
     @idMaterial  = @idMatProf,
     @idSolicitud = @idSol OUTPUT,
     @mensaje     = @msgSol OUTPUT;
PRINT 'Solicitud del profesor: ' + ISNULL(@msgSol, '');

PRINT '';
PRINT '=== Datos iniciales cargados correctamente ===';
SELECT * FROM VW_EstadisticasGenerales;
GO