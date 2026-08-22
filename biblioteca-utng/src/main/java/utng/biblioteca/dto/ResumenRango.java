package utng.biblioteca.dto;

import java.math.BigDecimal;

/** Números del encabezado del módulo de reportes, para el rango elegido. */
public class ResumenRango {

    private int accesos;
    private int usuariosDistintos;
    private int busquedas;
    private int prestamos;
    private int devoluciones;
    private int multasGeneradas;
    private BigDecimal montoCobrado = BigDecimal.ZERO;

    public int getAccesos() { return accesos; }
    public void setAccesos(int v) { this.accesos = v; }

    public int getUsuariosDistintos() { return usuariosDistintos; }
    public void setUsuariosDistintos(int v) { this.usuariosDistintos = v; }

    public int getBusquedas() { return busquedas; }
    public void setBusquedas(int v) { this.busquedas = v; }

    public int getPrestamos() { return prestamos; }
    public void setPrestamos(int v) { this.prestamos = v; }

    public int getDevoluciones() { return devoluciones; }
    public void setDevoluciones(int v) { this.devoluciones = v; }

    public int getMultasGeneradas() { return multasGeneradas; }
    public void setMultasGeneradas(int v) { this.multasGeneradas = v; }

    public BigDecimal getMontoCobrado() { return montoCobrado; }
    public void setMontoCobrado(BigDecimal v) { this.montoCobrado = v; }
}
