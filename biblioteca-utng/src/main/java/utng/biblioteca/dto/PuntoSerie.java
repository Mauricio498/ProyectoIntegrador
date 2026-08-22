package utng.biblioteca.dto;

import java.time.LocalDate;

/**
 * Un punto de una serie de tiempo: la fecha en que inicia el periodo y hasta
 * dos valores medidos en él.
 *
 * Se usa igual para accesos (exitosos/fallidos) que para préstamos
 * (prestados/devueltos), por eso los valores se llaman de forma genérica.
 */
public class PuntoSerie {

    private LocalDate periodo;
    private int valor;
    private int valorSecundario;

    public PuntoSerie() {
    }

    public PuntoSerie(LocalDate periodo, int valor, int valorSecundario) {
        this.periodo = periodo;
        this.valor = valor;
        this.valorSecundario = valorSecundario;
    }

    public LocalDate getPeriodo() { return periodo; }
    public void setPeriodo(LocalDate v) { this.periodo = v; }

    public int getValor() { return valor; }
    public void setValor(int v) { this.valor = v; }

    public int getValorSecundario() { return valorSecundario; }
    public void setValorSecundario(int v) { this.valorSecundario = v; }
}
