package utng.biblioteca.dto;

import java.math.BigDecimal;

/**
 * Cifras del acervo en este momento: qué hay en la biblioteca, cómo está
 * repartido y qué necesita atención.
 *
 * A diferencia de {@link ResumenRango}, no depende de fechas: describe el
 * estado actual, no la actividad de un periodo.
 */
public class ResumenAcervo {

    private int materialesActivos;
    private int materialesBaja;
    private int ejemplaresVigentes;
    private int ejemplaresDisponibles;
    private int ejemplaresPrestados;
    private int ejemplaresReparacion;
    private int ejemplaresExtraviados;
    private int autores;
    private int editoriales;
    private int materialesSinEjemplar;
    private BigDecimal ejemplaresPorMaterial = BigDecimal.ZERO;

    public int getMaterialesActivos() { return materialesActivos; }
    public void setMaterialesActivos(int v) { this.materialesActivos = v; }

    public int getMaterialesBaja() { return materialesBaja; }
    public void setMaterialesBaja(int v) { this.materialesBaja = v; }

    public int getEjemplaresVigentes() { return ejemplaresVigentes; }
    public void setEjemplaresVigentes(int v) { this.ejemplaresVigentes = v; }

    public int getEjemplaresDisponibles() { return ejemplaresDisponibles; }
    public void setEjemplaresDisponibles(int v) { this.ejemplaresDisponibles = v; }

    public int getEjemplaresPrestados() { return ejemplaresPrestados; }
    public void setEjemplaresPrestados(int v) { this.ejemplaresPrestados = v; }

    public int getEjemplaresReparacion() { return ejemplaresReparacion; }
    public void setEjemplaresReparacion(int v) { this.ejemplaresReparacion = v; }

    public int getEjemplaresExtraviados() { return ejemplaresExtraviados; }
    public void setEjemplaresExtraviados(int v) { this.ejemplaresExtraviados = v; }

    public int getAutores() { return autores; }
    public void setAutores(int v) { this.autores = v; }

    public int getEditoriales() { return editoriales; }
    public void setEditoriales(int v) { this.editoriales = v; }

    public int getMaterialesSinEjemplar() { return materialesSinEjemplar; }
    public void setMaterialesSinEjemplar(int v) { this.materialesSinEjemplar = v; }

    public BigDecimal getEjemplaresPorMaterial() { return ejemplaresPorMaterial; }
    public void setEjemplaresPorMaterial(BigDecimal v) { this.ejemplaresPorMaterial = v; }
}
