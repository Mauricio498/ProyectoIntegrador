package utng.biblioteca.model;

public class DetalleAdquisicion {
    private int idDetalleAdquisicion;
    private Adquisicion adquisicion;
    private Material material;
    private int cantidad;

    // Constructor vacío
    public DetalleAdquisicion() {
    }

    // Constructor para crear DetalleAdquisicion nuevo
    public DetalleAdquisicion(Adquisicion adquisicion, Material material, int cantidad) {
        this.adquisicion = adquisicion;
        this.material = material;
        this.cantidad = cantidad;
    }

    // Constructor con ID
    public DetalleAdquisicion(int idDetalleAdquisicion, Adquisicion adquisicion, 
                             Material material, int cantidad) {
        this.idDetalleAdquisicion = idDetalleAdquisicion;
        this.adquisicion = adquisicion;
        this.material = material;
        this.cantidad = cantidad;
    }

    // Getters y Setters
    public int getIdDetalleAdquisicion() {
        return idDetalleAdquisicion;
    }

    public void setIdDetalleAdquisicion(int idDetalleAdquisicion) {
        this.idDetalleAdquisicion = idDetalleAdquisicion;
    }

    public Adquisicion getAdquisicion() {
        return adquisicion;
    }

    public void setAdquisicion(Adquisicion adquisicion) {
        this.adquisicion = adquisicion;
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }
}
