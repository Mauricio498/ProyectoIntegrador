package utng.biblioteca.model;

import java.time.LocalDateTime;

/**
 * Acceso registra los intentos de acceso al sistema (login/logout).
 * IMPORTANTE: idUsuario puede ser NULL para accesos de invitado o intentos fallidos.
 * No se debe crear un usuario ficticio; simplemente se deja NULL.
 */
public class Acceso {
    private long idAcceso;
    private Usuario usuario; // NULL si es invitado o login fallido
    private String tipoAcceso; // 'Login' o 'Logout'
    private String resultado; // 'Exitoso' o 'Fallido'
    private LocalDateTime fechaHora;

    // Constructor vacío
    public Acceso() {
    }

    // Constructor para acceso autenticado
    public Acceso(Usuario usuario, String tipoAcceso, String resultado) {
        this.usuario = usuario;
        this.tipoAcceso = tipoAcceso;
        this.resultado = resultado;
        this.fechaHora = LocalDateTime.now();
    }

    // Constructor para acceso de invitado
    public Acceso(String tipoAcceso, String resultado) {
        this.usuario = null; // Invitado
        this.tipoAcceso = tipoAcceso;
        this.resultado = resultado;
        this.fechaHora = LocalDateTime.now();
    }

    // Constructor con ID
    public Acceso(long idAcceso, Usuario usuario, String tipoAcceso, 
                  String resultado, LocalDateTime fechaHora) {
        this.idAcceso = idAcceso;
        this.usuario = usuario;
        this.tipoAcceso = tipoAcceso;
        this.resultado = resultado;
        this.fechaHora = fechaHora;
    }

    // Getters y Setters
    public long getIdAcceso() {
        return idAcceso;
    }

    public void setIdAcceso(long idAcceso) {
        this.idAcceso = idAcceso;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getTipoAcceso() {
        return tipoAcceso;
    }

    public void setTipoAcceso(String tipoAcceso) {
        this.tipoAcceso = tipoAcceso;
    }

    public String getResultado() {
        return resultado;
    }

    public void setResultado(String resultado) {
        this.resultado = resultado;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    // Métodos auxiliares
    public boolean esExitoso() {
        return "Exitoso".equalsIgnoreCase(resultado);
    }

    public boolean esFallido() {
        return "Fallido".equalsIgnoreCase(resultado);
    }

    public boolean esLogin() {
        return "Login".equalsIgnoreCase(tipoAcceso);
    }

    public boolean esLogout() {
        return "Logout".equalsIgnoreCase(tipoAcceso);
    }

    public boolean esInvitado() {
        return usuario == null;
    }

    public boolean esAutenticado() {
        return usuario != null;
    }
}
