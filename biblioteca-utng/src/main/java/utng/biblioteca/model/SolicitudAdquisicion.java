package utng.biblioteca.model;

import java.time.LocalDateTime;

public class SolicitudAdquisicion {
    private int idSolicitud;
    private Usuario usuario;
    private String titulo;
    private String autor;
    private String editorial;
    private String isbn;
    private String descripcion;
    private LocalDateTime fechaSolicitud;
    private String estado;
    private String respuesta;
    private Usuario administrador;
    private LocalDateTime fechaRespuesta;

    // Constructor vacío
    public SolicitudAdquisicion() {
    }

    // Constructor para crear SolicitudAdquisicion nueva
    public SolicitudAdquisicion(Usuario usuario, String titulo, String autor, 
                               String editorial, String isbn, String descripcion) {
        this.usuario = usuario;
        this.titulo = titulo;
        this.autor = autor;
        this.editorial = editorial;
        this.isbn = isbn;
        this.descripcion = descripcion;
        this.fechaSolicitud = LocalDateTime.now();
        this.estado = "Pendiente";
    }

    // Constructor con ID
    public SolicitudAdquisicion(int idSolicitud, Usuario usuario, String titulo, 
                               String autor, String editorial, String isbn, String descripcion,
                               LocalDateTime fechaSolicitud, String estado, String respuesta,
                               Usuario administrador, LocalDateTime fechaRespuesta) {
        this.idSolicitud = idSolicitud;
        this.usuario = usuario;
        this.titulo = titulo;
        this.autor = autor;
        this.editorial = editorial;
        this.isbn = isbn;
        this.descripcion = descripcion;
        this.fechaSolicitud = fechaSolicitud;
        this.estado = estado;
        this.respuesta = respuesta;
        this.administrador = administrador;
        this.fechaRespuesta = fechaRespuesta;
    }

    // Getters y Setters
    public int getIdSolicitud() {
        return idSolicitud;
    }

    public void setIdSolicitud(int idSolicitud) {
        this.idSolicitud = idSolicitud;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public String getEditorial() {
        return editorial;
    }

    public void setEditorial(String editorial) {
        this.editorial = editorial;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public LocalDateTime getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(LocalDateTime fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getRespuesta() {
        return respuesta;
    }

    public void setRespuesta(String respuesta) {
        this.respuesta = respuesta;
    }

    public Usuario getAdministrador() {
        return administrador;
    }

    public void setAdministrador(Usuario administrador) {
        this.administrador = administrador;
    }

    public LocalDateTime getFechaRespuesta() {
        return fechaRespuesta;
    }

    public void setFechaRespuesta(LocalDateTime fechaRespuesta) {
        this.fechaRespuesta = fechaRespuesta;
    }

    // Métodos auxiliares
    public boolean isPendiente() {
        return "Pendiente".equalsIgnoreCase(estado);
    }

    public boolean isAprobada() {
        return "Aprobada".equalsIgnoreCase(estado);
    }
}
