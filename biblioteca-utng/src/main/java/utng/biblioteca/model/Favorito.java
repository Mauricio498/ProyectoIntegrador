package utng.biblioteca.model;

import java.time.LocalDateTime;

/**
 * Favorito representa la relación entre un Usuario y un Material que marcó como favorito.
 * Relación muchos a muchos: un usuario puede tener múltiples favoritos 
 * y un material puede ser favorito de múltiples usuarios.
 */
public class Favorito {
    private Usuario usuario;
    private Material material;
    private LocalDateTime fechaAgregado;

    // Constructor vacío
    public Favorito() {
    }

    // Constructor
    public Favorito(Usuario usuario, Material material) {
        this.usuario = usuario;
        this.material = material;
        this.fechaAgregado = LocalDateTime.now();
    }

    // Constructor con fecha
    public Favorito(Usuario usuario, Material material, LocalDateTime fechaAgregado) {
        this.usuario = usuario;
        this.material = material;
        this.fechaAgregado = fechaAgregado;
    }

    // Getters y Setters
    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public LocalDateTime getFechaAgregado() {
        return fechaAgregado;
    }

    public void setFechaAgregado(LocalDateTime fechaAgregado) {
        this.fechaAgregado = fechaAgregado;
    }
}
