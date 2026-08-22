package utng.biblioteca.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import utng.biblioteca.dto.RegistroImportacion;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class ImportadorExcel {

    private static final String TIPO_LIBRO = "Libro";
    private static final String TIPO_TESIS = "Tesis";
    private static final String TIPO_CONSULTA = "Consulta";
    private static final String TIPO_OTROS = "Otros";

    /**
     * Lee un archivo Excel y convierte sus filas en registros preparados.
     *
     * IMPORTANTE:
     * Este método todavía NO inserta nada en SQL Server.
     */
    public List<RegistroImportacion> leer(File archivo)
            throws IOException {

        List<RegistroImportacion> registros =
                new ArrayList<>();

        try (FileInputStream entrada =
                     new FileInputStream(archivo);
             Workbook libro =
                     new XSSFWorkbook(entrada)) {

            Sheet hoja = libro.getSheetAt(0);

            if (hoja.getPhysicalNumberOfRows() <= 1) {
                return registros;
            }

            // Validamos que sea el formato esperado
            validarEncabezados(hoja.getRow(0));

            DataFormatter formatter =
                    new DataFormatter();

            for (int i = 1;
                 i <= hoja.getLastRowNum();
                 i++) {

                Row fila = hoja.getRow(i);

                if (fila == null || filaVacia(fila)) {
                    continue;
                }

                RegistroImportacion registro =
                        convertirFila(fila, formatter);

                registro.setFilaExcel(i + 1);

                registros.add(registro);
            }
        }

        return registros;
    }

    /**
     * Convierte una fila del Excel en un RegistroImportacion.
     */
    private RegistroImportacion convertirFila(
            Row fila,
            DataFormatter formatter) {

        RegistroImportacion registro =
                new RegistroImportacion();

        registro.setIdBiblioteca(
                leerEntero(fila.getCell(0), formatter)
        );

        registro.setNumeroAdquisicion(
                limpiar(
                        formatter.formatCellValue(
                                fila.getCell(1)
                        )
                )
        );

        registro.setCodigoEjemplar(null);

        registro.setTitulo(
                limpiar(
                        formatter.formatCellValue(
                                fila.getCell(3)
                        )
                )
        );

        registro.setAutor(
                limpiar(
                        formatter.formatCellValue(
                                fila.getCell(4)
                        )
                )
        );

        registro.setEditorial(
                limpiar(
                        formatter.formatCellValue(
                                fila.getCell(5)
                        )
                )
        );

        registro.setIsbn(
                limpiar(
                        formatter.formatCellValue(
                                fila.getCell(6)
                        )
                )
        );

        registro.setClasificacion(
                limpiar(
                        formatter.formatCellValue(
                                fila.getCell(7)
                        )
                )
        );

        registro.setAnioPublicacion(
                leerAnio(fila.getCell(8), formatter)
        );

        registro.setFechaIngreso(
                leerFecha(fila.getCell(9))
        );

        registro.setTipoDetectado(
                determinarTipo(
                        registro.getNumeroAdquisicion(),
                        registro.getEditorial(),
                        registro.getClasificacion()
                )
        );

        return registro;
    }

    /**
     * Determina uno de los cuatro tipos permitidos.
     */
    private String determinarTipo(
            String numeroAdquisicion,
            String editorial,
            String clasificacion) {

        String codigo =
                limpiar(numeroAdquisicion)
                        .toUpperCase();

        String edit =
                limpiar(editorial)
                        .toUpperCase();

        String clas = limpiar(clasificacion).toUpperCase();

        // ------------------------------------------------------------
        // TESIS
        // ------------------------------------------------------------

        if (esCodigoTesis(codigo)
                || (edit.contains("UTNG")
                    && !codigo.startsWith("UC")
                    && !codigo.startsWith("CDUC"))) {

            return TIPO_TESIS;
        }

        // ------------------------------------------------------------
        // LIBROS
        // ------------------------------------------------------------

        if (codigo.startsWith("UC")
                || codigo.startsWith("CDUC")) {

            return TIPO_LIBRO;
        }

        // ------------------------------------------------------------
        // CONSULTA
        // ------------------------------------------------------------

        if (codigo.startsWith("INEGI")) {

            return TIPO_CONSULTA;
        }

        // ------------------------------------------------------------
        // OTROS
        // ------------------------------------------------------------

        return TIPO_OTROS;
    }

    /**
     * Prefijos que aparecen en el archivo y corresponden a trabajos
     * académicos de la colección.
     */
    private boolean esCodigoTesis(String codigo) {

        String[] prefijos = {
                "ING",
                "ITP",
                "PI",
                "DN",
                "ACH",
                "ARH",
                "LGCH",
                "IM",
                "ITI",
                "DSM",
                "DD",
                "CO",
                "IF",
                "LC",
                "LINM",
                "IDG",
                "IRI",
                "ER",
                "T",
                "PP",
                "MC",
                "IRD"
        };

        for (String prefijo : prefijos) {
            if (codigo.startsWith(prefijo)) {
                return true;
            }
        }

        return false;
    }

    private void validarEncabezados(Row encabezados) {

        if (encabezados == null) {
            throw new IllegalArgumentException(
                    "El archivo Excel no contiene encabezados."
            );
        }

        String[] esperados = {
                "idbiblioteca",
                "numadqui",
                "ficha_no",
                "titulo",
                "autor",
                "editorial",
                "isbn",
                "clasificacion",
                "fecha",
                "fechaingreso"
        };

        DataFormatter formatter =
                new DataFormatter();

        for (int i = 0;
             i < esperados.length;
             i++) {

            String actual =
                    limpiar(
                            formatter.formatCellValue(
                                    encabezados.getCell(i)
                            )
                    ).toLowerCase();

            if (!esperados[i].equals(actual)) {

                throw new IllegalArgumentException(
                        "El archivo no tiene el formato esperado. "
                        + "Se esperaba '" + esperados[i]
                        + "' en la columna " + (i + 1)
                );
            }
        }
    }

    private boolean filaVacia(Row fila) {

        for (int i = 0; i < 10; i++) {

            Cell celda = fila.getCell(i);

            if (celda != null
                    && !celda.toString().trim().isEmpty()) {

                return false;
            }
        }

        return true;
    }

    private int leerEntero(
            Cell celda,
            DataFormatter formatter) {

        if (celda == null) {
            return 0;
        }

        String valor =
                limpiar(formatter.formatCellValue(celda));

        if (valor.isEmpty()) {
            return 0;
        }

        try {
            return Integer.parseInt(valor);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private Integer leerAnio(
            Cell celda,
            DataFormatter formatter) {

        if (celda == null) {
            return null;
        }

        String valor =
                limpiar(formatter.formatCellValue(celda));

        if (valor.isEmpty()) {
            return null;
        }

        try {

            int anio =
                    (int) Double.parseDouble(valor);

            if (anio >= 1000 && anio <= 2100) {
                return anio;
            }

        } catch (NumberFormatException ignored) {
        }

        return null;
    }

    private LocalDate leerFecha(Cell celda) {

        if (celda == null) {
            return null;
        }

        if (celda.getCellType()
                == CellType.NUMERIC
                && DateUtil.isCellDateFormatted(celda)) {

            return celda.getDateCellValue()
                    .toInstant()
                    .atZone(
                            ZoneId.systemDefault()
                    )
                    .toLocalDate();
        }

        if (celda.getCellType()
                == CellType.STRING) {

            String texto =
                    limpiar(celda.getStringCellValue());

            if (texto.isEmpty()) {
                return null;
            }

            String[] formatos = {
                    "dd/MM/yyyy",
                    "d/M/yyyy",
                    "yyyy-MM-dd"
            };

            for (String formato : formatos) {

                try {
                    return LocalDate.parse(
                            texto,
                            java.time.format.DateTimeFormatter
                                    .ofPattern(formato)
                    );

                } catch (Exception ignored) {
                }
            }
        }

        return null;
    }

    private String limpiar(String valor) {

        if (valor == null) {
            return "";
        }

        return valor
                .trim()
                .replaceAll("\\s+", " ");
    }
}