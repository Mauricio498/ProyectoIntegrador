package utng.biblioteca.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import utng.biblioteca.dto.Conteo;
import utng.biblioteca.dto.Periodo;
import utng.biblioteca.dto.PuntoSerie;
import utng.biblioteca.dto.ResumenRango;
import utng.biblioteca.service.EstadisticaService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReporteExcel {

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ================================================================
    // GENERAR REPORTE
    // ================================================================

    public void generar(
            File archivo,
            LocalDate desde,
            LocalDate hasta,
            Periodo periodo,
            EstadisticaService estadisticaService) throws IOException {

        try (Workbook libro = new XSSFWorkbook()) {

            // ========================================================
            // OBTENER DATOS
            // ========================================================

            ResumenRango resumen =
                    estadisticaService.resumen(desde, hasta);

            List<PuntoSerie> accesos =
                    estadisticaService.accesos(desde, hasta, periodo);

            List<PuntoSerie> circulacion =
                    estadisticaService.circulacion(desde, hasta, periodo);

            List<PuntoSerie> busquedas =
                    estadisticaService.busquedas(desde, hasta, periodo);

            List<Conteo> terminos =
                    estadisticaService.terminosMasBuscados(
                            desde, hasta, 10);

            List<Conteo> materiales =
                    estadisticaService.materialesMasPrestados(
                            desde, hasta, 10);

            List<Conteo> roles =
                    estadisticaService.prestamosPorRol(
                            desde, hasta);

            List<Conteo> areas =
                    estadisticaService.prestamosPorArea(
                            desde, hasta);

            // ========================================================
            // ESTILOS
            // ========================================================

            CellStyle estiloTitulo =
                    crearEstiloTitulo(libro);

            CellStyle estiloSubtitulo =
                    crearEstiloSubtitulo(libro);

            CellStyle estiloEncabezado =
                    crearEstiloEncabezado(libro);

            CellStyle estiloCelda =
                    crearEstiloCelda(libro);

            CellStyle estiloNumero =
                    crearEstiloNumero(libro);

            CellStyle estiloFecha =
                    crearEstiloFecha(libro);

            CellStyle estiloMoneda =
                    crearEstiloMoneda(libro);

            CellStyle estiloResumenEtiqueta =
                    crearEstiloResumenEtiqueta(libro);

            CellStyle estiloResumenValor =
                    crearEstiloResumenValor(libro);

            // ========================================================
            // HOJA RESUMEN
            // ========================================================

            crearResumen(
                    libro,
                    resumen,
                    desde,
                    hasta,
                    estiloTitulo,
                    estiloSubtitulo,
                    estiloEncabezado,
                    estiloFecha,
                    estiloMoneda,
                    estiloResumenEtiqueta,
                    estiloResumenValor
            );

            // ========================================================
            // HOJAS DE SERIES
            // ========================================================

            crearSerie(
                    libro,
                    "Accesos",
                    accesos,
                    "Periodo",
                    "Valor",
                    "Valor secundario",
                    estiloTitulo,
                    estiloEncabezado,
                    estiloCelda,
                    estiloFecha,
                    estiloNumero
            );

            crearSerie(
                    libro,
                    "Circulacion",
                    circulacion,
                    "Periodo",
                    "Prestamos",
                    "Devoluciones",
                    estiloTitulo,
                    estiloEncabezado,
                    estiloCelda,
                    estiloFecha,
                    estiloNumero
            );

            crearSerie(
                    libro,
                    "Busquedas",
                    busquedas,
                    "Periodo",
                    "Busquedas",
                    "Valor secundario",
                    estiloTitulo,
                    estiloEncabezado,
                    estiloCelda,
                    estiloFecha,
                    estiloNumero
            );

            // ========================================================
            // HOJAS DE CONTEOS
            // ========================================================

            crearConteos(
                    libro,
                    "Terminos buscados",
                    terminos,
                    "Termino",
                    estiloTitulo,
                    estiloEncabezado,
                    estiloCelda,
                    estiloNumero
            );

            crearConteos(
                    libro,
                    "Materiales prestados",
                    materiales,
                    "Material",
                    estiloTitulo,
                    estiloEncabezado,
                    estiloCelda,
                    estiloNumero
            );

            crearConteos(
                    libro,
                    "Prestamos por rol",
                    roles,
                    "Rol",
                    estiloTitulo,
                    estiloEncabezado,
                    estiloCelda,
                    estiloNumero
            );

            crearConteos(
                    libro,
                    "Prestamos por area",
                    areas,
                    "Area",
                    estiloTitulo,
                    estiloEncabezado,
                    estiloCelda,
                    estiloNumero
            );

            // ========================================================
            // GUARDAR
            // ========================================================

            try (FileOutputStream salida =
                         new FileOutputStream(archivo)) {

                libro.write(salida);
            }
        }
    }

    // ================================================================
    // RESUMEN
    // ================================================================

    private void crearResumen(
            Workbook libro,
            ResumenRango resumen,
            LocalDate desde,
            LocalDate hasta,
            CellStyle estiloTitulo,
            CellStyle estiloSubtitulo,
            CellStyle estiloEncabezado,
            CellStyle estiloFecha,
            CellStyle estiloMoneda,
            CellStyle estiloResumenEtiqueta,
            CellStyle estiloResumenValor) {

        Sheet hoja = libro.createSheet("Resumen");

        // Título
        Row titulo = hoja.createRow(0);
        titulo.setHeightInPoints(28);

        Cell celdaTitulo = titulo.createCell(0);

        celdaTitulo.setCellValue(
                "REPORTE DE ESTADÍSTICAS - BIBLIOTECA"
        );

        celdaTitulo.setCellStyle(estiloTitulo);

        hoja.addMergedRegion(
                new org.apache.poi.ss.util.CellRangeAddress(
                        0, 0, 0, 3
                )
        );

        // Subtítulo
        Row subtitulo = hoja.createRow(1);

        Cell celdaSubtitulo = subtitulo.createCell(0);

        celdaSubtitulo.setCellValue(
                "Resumen del periodo seleccionado"
        );

        celdaSubtitulo.setCellStyle(estiloSubtitulo);

        hoja.addMergedRegion(
                new org.apache.poi.ss.util.CellRangeAddress(
                        1, 1, 0, 3
                )
        );

        // Fechas
        Row fechas = hoja.createRow(3);

        Cell desdeLabel = fechas.createCell(0);
        desdeLabel.setCellValue("Desde");
        desdeLabel.setCellStyle(estiloEncabezado);

        Cell desdeValor = fechas.createCell(1);
        desdeValor.setCellValue(
                desde != null
                        ? desde.format(FORMATO_FECHA)
                        : ""
        );
        desdeValor.setCellStyle(estiloFecha);

        Cell hastaLabel = fechas.createCell(2);
        hastaLabel.setCellValue("Hasta");
        hastaLabel.setCellStyle(estiloEncabezado);

        Cell hastaValor = fechas.createCell(3);
        hastaValor.setCellValue(
                hasta != null
                        ? hasta.format(FORMATO_FECHA)
                        : ""
        );
        hastaValor.setCellStyle(estiloFecha);

        // Encabezado de resumen
        Row encabezado = hoja.createRow(5);

        encabezado.createCell(0)
                .setCellValue("Indicador");

        encabezado.createCell(1)
                .setCellValue("Cantidad");

        encabezado.getCell(0)
                .setCellStyle(estiloEncabezado);

        encabezado.getCell(1)
                .setCellStyle(estiloEncabezado);

        // Datos
        Object[][] datos = {
                {"Accesos", resumen.getAccesos()},
                {"Usuarios distintos", resumen.getUsuariosDistintos()},
                {"Búsquedas", resumen.getBusquedas()},
                {"Préstamos", resumen.getPrestamos()},
                {"Devoluciones", resumen.getDevoluciones()},
                {"Multas generadas", resumen.getMultasGeneradas()}
        };

        int fila = 6;

        for (Object[] dato : datos) {

            Row row = hoja.createRow(fila++);

            Cell etiqueta = row.createCell(0);
            etiqueta.setCellValue((String) dato[0]);
            etiqueta.setCellStyle(estiloResumenEtiqueta);

            Cell valor = row.createCell(1);
            valor.setCellValue(
                    ((Number) dato[1]).doubleValue()
            );
            valor.setCellStyle(estiloResumenValor);
        }

        // Monto cobrado
        Row monto = hoja.createRow(fila);

        Cell etiquetaMonto = monto.createCell(0);
        etiquetaMonto.setCellValue("Monto cobrado");
        etiquetaMonto.setCellStyle(estiloResumenEtiqueta);

        BigDecimal valorMonto =
                resumen.getMontoCobrado();

        Cell celdaMonto = monto.createCell(1);

        celdaMonto.setCellValue(
                valorMonto != null
                        ? valorMonto.doubleValue()
                        : 0
        );

        celdaMonto.setCellStyle(estiloMoneda);

        // Ajustar columnas
        hoja.setColumnWidth(0, 28 * 256);
        hoja.setColumnWidth(1, 18 * 256);
        hoja.setColumnWidth(2, 18 * 256);
        hoja.setColumnWidth(3, 18 * 256);

        hoja.createFreezePane(0, 5);
    }

    // ================================================================
    // SERIES
    // ================================================================

    private void crearSerie(
            Workbook libro,
            String nombreHoja,
            List<PuntoSerie> datos,
            String encabezadoPeriodo,
            String encabezadoValor,
            String encabezadoSecundario,
            CellStyle estiloTitulo,
            CellStyle estiloEncabezado,
            CellStyle estiloCelda,
            CellStyle estiloFecha,
            CellStyle estiloNumero) {

        Sheet hoja = libro.createSheet(nombreHoja);

        // Título
        Row titulo = hoja.createRow(0);
        titulo.setHeightInPoints(26);

        Cell celdaTitulo = titulo.createCell(0);

        celdaTitulo.setCellValue(
                nombreHoja.toUpperCase()
        );

        celdaTitulo.setCellStyle(estiloTitulo);

        hoja.addMergedRegion(
                new org.apache.poi.ss.util.CellRangeAddress(
                        0, 0, 0, 2
                )
        );

        // Encabezados
        Row encabezados = hoja.createRow(2);

        encabezados.createCell(0)
                .setCellValue(encabezadoPeriodo);

        encabezados.createCell(1)
                .setCellValue(encabezadoValor);

        encabezados.createCell(2)
                .setCellValue(encabezadoSecundario);

        for (int i = 0; i < 3; i++) {
            encabezados.getCell(i)
                    .setCellStyle(estiloEncabezado);
        }

        // Datos
        int fila = 3;

        for (PuntoSerie punto : datos) {

            Row row = hoja.createRow(fila++);

            Cell periodo = row.createCell(0);

            periodo.setCellValue(
                    punto.getPeriodo() != null
                            ? punto.getPeriodo()
                            .format(FORMATO_FECHA)
                            : ""
            );

            periodo.setCellStyle(estiloFecha);

            Cell valor = row.createCell(1);

            valor.setCellValue(
                    punto.getValor()
            );

            valor.setCellStyle(estiloNumero);

            Cell secundario = row.createCell(2);

            secundario.setCellValue(
                    punto.getValorSecundario()
            );

            secundario.setCellStyle(estiloNumero);
        }

        // Filtro
        if (!datos.isEmpty()) {

            hoja.setAutoFilter(
                    new org.apache.poi.ss.util.CellRangeAddress(
                            2,
                            fila - 1,
                            0,
                            2
                    )
            );
        }

        hoja.createFreezePane(0, 3);

        hoja.setColumnWidth(0, 20 * 256);
        hoja.setColumnWidth(1, 18 * 256);
        hoja.setColumnWidth(2, 22 * 256);
    }

    // ================================================================
    // CONTEOS
    // ================================================================

    private void crearConteos(
            Workbook libro,
            String nombreHoja,
            List<Conteo> datos,
            String encabezadoEtiqueta,
            CellStyle estiloTitulo,
            CellStyle estiloEncabezado,
            CellStyle estiloCelda,
            CellStyle estiloNumero) {

        Sheet hoja = libro.createSheet(nombreHoja);

        // Título
        Row titulo = hoja.createRow(0);
        titulo.setHeightInPoints(26);

        Cell celdaTitulo = titulo.createCell(0);

        celdaTitulo.setCellValue(
                nombreHoja.toUpperCase()
        );

        celdaTitulo.setCellStyle(estiloTitulo);

        hoja.addMergedRegion(
                new org.apache.poi.ss.util.CellRangeAddress(
                        0, 0, 0, 2
                )
        );

        // Encabezados
        Row encabezados = hoja.createRow(2);

        encabezados.createCell(0)
                .setCellValue(encabezadoEtiqueta);

        encabezados.createCell(1)
                .setCellValue("Cantidad");

        encabezados.createCell(2)
                .setCellValue("Detalle");

        for (int i = 0; i < 3; i++) {

            encabezados.getCell(i)
                    .setCellStyle(estiloEncabezado);
        }

        // Datos
        int fila = 3;

        for (Conteo conteo : datos) {

            Row row = hoja.createRow(fila++);

            Cell etiqueta = row.createCell(0);

            etiqueta.setCellValue(
                    conteo.getEtiqueta() != null
                            ? conteo.getEtiqueta()
                            : ""
            );

            etiqueta.setCellStyle(estiloCelda);

            Cell cantidad = row.createCell(1);

            cantidad.setCellValue(
                    conteo.getCantidad()
            );

            cantidad.setCellStyle(estiloNumero);

            Cell detalle = row.createCell(2);

            detalle.setCellValue(
                    conteo.getDetalle() != null
                            ? conteo.getDetalle()
                            : ""
            );

            detalle.setCellStyle(estiloCelda);
        }

        // Filtros
        if (!datos.isEmpty()) {

            hoja.setAutoFilter(
                    new org.apache.poi.ss.util.CellRangeAddress(
                            2,
                            fila - 1,
                            0,
                            2
                    )
            );
        }

        // Congelar encabezados
        hoja.createFreezePane(0, 3);

        // Tamaño de columnas
        hoja.setColumnWidth(0, 36 * 256);
        hoja.setColumnWidth(1, 16 * 256);
        hoja.setColumnWidth(2, 50 * 256);
    }

    // ================================================================
    // ESTILOS
    // ================================================================

    private CellStyle crearEstiloTitulo(
            Workbook libro) {

        CellStyle estilo = libro.createCellStyle();

        Font fuente = libro.createFont();

        fuente.setBold(true);
        fuente.setFontHeightInPoints((short) 16);
        fuente.setColor(
                IndexedColors.WHITE.getIndex()
        );

        estilo.setFont(fuente);

        estilo.setFillForegroundColor(
                IndexedColors.DARK_GREEN.getIndex()
        );

        estilo.setFillPattern(
                FillPatternType.SOLID_FOREGROUND
        );

        estilo.setAlignment(
                HorizontalAlignment.CENTER
        );

        estilo.setVerticalAlignment(
                VerticalAlignment.CENTER
        );

        return estilo;
    }

    private CellStyle crearEstiloSubtitulo(
            Workbook libro) {

        CellStyle estilo = libro.createCellStyle();

        Font fuente = libro.createFont();

        fuente.setItalic(true);
        fuente.setFontHeightInPoints((short) 11);

        estilo.setFont(fuente);

        estilo.setAlignment(
                HorizontalAlignment.CENTER
        );

        return estilo;
    }

    private CellStyle crearEstiloEncabezado(
            Workbook libro) {

        CellStyle estilo = libro.createCellStyle();

        Font fuente = libro.createFont();

        fuente.setBold(true);
        fuente.setColor(
                IndexedColors.WHITE.getIndex()
        );

        estilo.setFont(fuente);

        estilo.setFillForegroundColor(
                IndexedColors.GREEN.getIndex()
        );

        estilo.setFillPattern(
                FillPatternType.SOLID_FOREGROUND
        );

        estilo.setAlignment(
                HorizontalAlignment.CENTER
        );

        estilo.setVerticalAlignment(
                VerticalAlignment.CENTER
        );

        estilo.setBorderTop(
                BorderStyle.THIN
        );

        estilo.setBorderBottom(
                BorderStyle.THIN
        );

        estilo.setBorderLeft(
                BorderStyle.THIN
        );

        estilo.setBorderRight(
                BorderStyle.THIN
        );

        return estilo;
    }

    private CellStyle crearEstiloCelda(
            Workbook libro) {

        CellStyle estilo = libro.createCellStyle();

        estilo.setBorderBottom(
                BorderStyle.THIN
        );

        estilo.setBorderBottom(
                BorderStyle.THIN
        );

        estilo.setVerticalAlignment(
                VerticalAlignment.CENTER
        );

        return estilo;
    }

    private CellStyle crearEstiloNumero(
            Workbook libro) {

        CellStyle estilo = crearEstiloCelda(libro);

        estilo.setAlignment(
                HorizontalAlignment.CENTER
        );

        return estilo;
    }

    private CellStyle crearEstiloFecha(
            Workbook libro) {

        CellStyle estilo = crearEstiloCelda(libro);

        estilo.setDataFormat(
                libro.createDataFormat()
                        .getFormat("dd/mm/yyyy")
        );

        estilo.setAlignment(
                HorizontalAlignment.CENTER
        );

        return estilo;
    }

    private CellStyle crearEstiloMoneda(
            Workbook libro) {

        CellStyle estilo = crearEstiloCelda(libro);

        estilo.setDataFormat(
                libro.createDataFormat()
                        .getFormat("$#,##0.00")
        );

        return estilo;
    }

    private CellStyle crearEstiloResumenEtiqueta(
            Workbook libro) {

        CellStyle estilo = crearEstiloCelda(libro);

        Font fuente = libro.createFont();
        fuente.setBold(true);

        estilo.setFont(fuente);

        return estilo;
    }

    private CellStyle crearEstiloResumenValor(
            Workbook libro) {

        CellStyle estilo = crearEstiloCelda(libro);

        Font fuente = libro.createFont();
        fuente.setBold(true);
        fuente.setFontHeightInPoints((short) 12);

        estilo.setFont(fuente);

        estilo.setAlignment(
                HorizontalAlignment.CENTER
        );

        return estilo;
    }
}