package utng.biblioteca.dto;

public class ResultadoImportacion {

    private final int filasProcesadas;
    private final int materialesCreados;
    private final int materialesReutilizados;
    private final int ejemplaresCreados;

    public ResultadoImportacion(
            int filasProcesadas,
            int materialesCreados,
            int materialesReutilizados,
            int ejemplaresCreados) {

        this.filasProcesadas = filasProcesadas;
        this.materialesCreados = materialesCreados;
        this.materialesReutilizados = materialesReutilizados;
        this.ejemplaresCreados = ejemplaresCreados;
    }

    public int getFilasProcesadas() {
        return filasProcesadas;
    }

    public int getMaterialesCreados() {
        return materialesCreados;
    }

    public int getMaterialesReutilizados() {
        return materialesReutilizados;
    }

    public int getEjemplaresCreados() {
        return ejemplaresCreados;
    }
}