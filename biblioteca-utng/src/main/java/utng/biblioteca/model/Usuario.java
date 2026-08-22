package utng.biblioteca.model;

import java.time.LocalDateTime;

public class Usuario {
    private int idUsuario;
    private String numeroControl;
    private String nombre;
    private String correo;
    private String telefono;
    private String usuario;
    private String passwordHash;
    private Rol rol;

    /** Área académica. Puede ser null: el campo es opcional. */
    private Area area;
    private boolean estado;
    private LocalDateTime fechaRegistro;

    // Constructor vacío
    public Usuario() {
    }

    // Constructor para crear Usuario nuevo
    public Usuario(String numeroControl, String nombre, String correo, 
                   String telefono, String usuario, String passwordHash, Rol rol) {
        this.numeroControl = numeroControl;
        this.nombre = nombre;
        this.correo = correo;
        this.telefono = telefono;
        this.usuario = usuario;
        this.passwordHash = passwordHash;
        this.rol = rol;
        this.estado = true;
        this.fechaRegistro = LocalDateTime.now();
    }

    // Constructor con ID
    public Usuario(int idUsuario, String numeroControl, String nombre, String correo,
                   String telefono, String usuario, String passwordHash, Rol rol, 
                   boolean estado, LocalDateTime fechaRegistro) {
        this.idUsuario = idUsuario;
        this.numeroControl = numeroControl;
        this.nombre = nombre;
        this.correo = correo;
        this.telefono = telefono;
        this.usuario = usuario;
        this.passwordHash = passwordHash;
        this.rol = rol;
        this.estado = estado;
        this.fechaRegistro = fechaRegistro;
    }

    // Getters y Setters
    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNumeroControl() {
        return numeroControl;
    }

    public void setNumeroControl(String numeroControl) {
        this.numeroControl = numeroControl;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public Area getArea() {
        return area;
    }

    public void setArea(Area area) {
        this.area = area;
    }

    /** Nombre del área, o cadena vacía si no tiene una asignada. */
    public String getNombreArea() {
        return area == null ? "" : area.getNombre();
    }

    public boolean isEstado() {
        return estado;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
}
