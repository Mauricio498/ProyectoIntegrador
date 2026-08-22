package utng.biblioteca.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Multa {
    private int idMulta;
    private DetallePrestamo detallePrestamo;
    private LocalDateTime fechaGeneracion;
    private int diasRetraso;
    private BigDecimal tarifaDiaria;
    private BigDecimal monto;
    private String motivo;
    private String estado;

    // Constructor vacío
    public Multa() {
    }

    // Constructor para crear Multa nueva
    public Multa(DetallePrestamo detallePrestamo, int diasRetraso, 
                 BigDecimal tarifaDiaria, BigDecimal monto, String motivo) {
        this.detallePrestamo = detallePrestamo;
        this.diasRetraso = diasRetraso;
        this.tarifaDiaria = tarifaDiaria;
        this.monto = monto;
        this.motivo = motivo;
        this.fechaGeneracion = LocalDateTime.now();
        this.estado = "Pendiente";
    }

    // Constructor con ID
    public Multa(int idMulta, DetallePrestamo detallePrestamo, LocalDateTime fechaGeneracion,
                 int diasRetraso, BigDecimal tarifaDiaria, BigDecimal monto, 
                 String motivo, String estado) {
        this.idMulta = idMulta;
        this.detallePrestamo = detallePrestamo;
        this.fechaGeneracion = fechaGeneracion;
        this.diasRetraso = diasRetraso;
        this.tarifaDiaria = tarifaDiaria;
        this.monto = monto;
        this.motivo = motivo;
        this.estado = estado;
    }

    // Getters y Setters
    public int getIdMulta() {
        return idMulta;
    }

    public void setIdMulta(int idMulta) {
        this.idMulta = idMulta;
    }

    public DetallePrestamo getDetallePrestamo() {
        return detallePrestamo;
    }

    public void setDetallePrestamo(DetallePrestamo detallePrestamo) {
        this.detallePrestamo = detallePrestamo;
    }

    public LocalDateTime getFechaGeneracion() {
        return fechaGeneracion;
    }

    public void setFechaGeneracion(LocalDateTime fechaGeneracion) {
        this.fechaGeneracion = fechaGeneracion;
    }

    public int getDiasRetraso() {
        return diasRetraso;
    }

    public void setDiasRetraso(int diasRetraso) {
        this.diasRetraso = diasRetraso;
    }

    public BigDecimal getTarifaDiaria() {
        return tarifaDiaria;
    }

    public void setTarifaDiaria(BigDecimal tarifaDiaria) {
        this.tarifaDiaria = tarifaDiaria;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    // Métodos auxiliares
    public boolean isPendiente() {
        return "Pendiente".equalsIgnoreCase(estado);
    }

    public boolean isPagada() {
        return "Pagada".equalsIgnoreCase(estado);
    }

    public boolean isCondonada() {
        return "Condonada".equalsIgnoreCase(estado);
    }
}
