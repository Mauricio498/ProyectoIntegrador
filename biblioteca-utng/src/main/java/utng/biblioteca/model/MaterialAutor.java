package utng.biblioteca.model;

/**
 * Relación muchos a muchos entre Material y Autor.
 * Un material puede tener múltiples autores y un autor puede escribir múltiples materiales.
 */
public class MaterialAutor {
    private Material material;
    private Autor autor;

    // Constructor vacío
    public MaterialAutor() {
    }

    // Constructor
    public MaterialAutor(Material material, Autor autor) {
        this.material = material;
        this.autor = autor;
    }

    // Getters y Setters
    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public Autor getAutor() {
        return autor;
    }

    public void setAutor(Autor autor) {
        this.autor = autor;
    }
}
