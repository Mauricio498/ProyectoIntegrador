package utng.biblioteca.model;

import java.time.LocalDateTime;

/**
 * Busqueda registra las búsquedas realizadas en el sistema.
 * IMPORTANTE: idUsuario puede ser NULL para búsquedas realizadas como invitado.
 * No se debe crear un usuario ficticio; simplemente se deja NULL.
 */
public class Busqueda {
    private long idBusqueda;
    private Usuario usuario; // NULL si es búsqueda de invitado
    private String termino;
    private String filtros;
    private Integer cantidadResultados;
    private LocalDateTime fechaHora;

    // Constructor vacío
    public Busqueda() {
    }

    // Constructor para búsqueda autenticada
    public Busqueda(Usuario usuario, String termino, String filtros) {
        this.usuario = usuario;
        this.termino = termino;
        this.filtros = filtros;
        this.fechaHora = LocalDateTime.now();
    }

    // Constructor para búsqueda de invitado
    public Busqueda(String termino, String filtros) {
        this.usuario = null; // Invitado
        this.termino = termino;
        this.filtros = filtros;
        this.fechaHora = LocalDateTime.now();
    }

    // Constructor con ID
    public Busqueda(long idBusqueda, Usuario usuario, String termino, String filtros,
                    Integer cantidadResultados, LocalDateTime fechaHora) {
        this.idBusqueda = idBusqueda;
        this.usuario = usuario;
        this.termino = termino;
        this.filtros = filtros;
        this.cantidadResultados = cantidadResultados;
        this.fechaHora = fechaHora;
    }

    // Getters y Setters
    public long getIdBusqueda() {
        return idBusqueda;
    }

    public void setIdBusqueda(long idBusqueda) {
        this.idBusqueda = idBusqueda;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getTermino() {
        return termino;
    }

    public void setTermino(String termino) {
        this.termino = termino;
    }

    public String getFiltros() {
        return filtros;
    }

    public void setFiltros(String filtros) {
        this.filtros = filtros;
    }

    public Integer getCantidadResultados() {
        return cantidadResultados;
    }

    public void setCantidadResultados(Integer cantidadResultados) {
        this.cantidadResultados = cantidadResultados;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    // Métodos auxiliares
    public boolean esInvitado() {
        return usuario == null;
    }

    public boolean esAutenticado() {
        return usuario != null;
    }
}
