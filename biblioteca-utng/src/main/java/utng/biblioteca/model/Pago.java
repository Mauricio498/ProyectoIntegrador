package utng.biblioteca.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Pago {
    private int idPago;
    private Multa multa;
    private Usuario administrador;
    private BigDecimal monto;
    private LocalDateTime fechaPago;

    // Constructor vacío
    public Pago() {
    }

    // Constructor para crear Pago nuevo
    public Pago(Multa multa, BigDecimal monto) {
        this.multa = multa;
        this.monto = monto;
        this.fechaPago = LocalDateTime.now();
    }

    // Constructor con ID y administrador
    public Pago(int idPago, Multa multa, Usuario administrador, 
                BigDecimal monto, LocalDateTime fechaPago) {
        this.idPago = idPago;
        this.multa = multa;
        this.administrador = administrador;
        this.monto = monto;
        this.fechaPago = fechaPago;
    }

    // Getters y Setters
    public int getIdPago() {
        return idPago;
    }

    public void setIdPago(int idPago) {
        this.idPago = idPago;
    }

    public Multa getMulta() {
        return multa;
    }

    public void setMulta(Multa multa) {
        this.multa = multa;
    }

    public Usuario getAdministrador() {
        return administrador;
    }

    public void setAdministrador(Usuario administrador) {
        this.administrador = administrador;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public LocalDateTime getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(LocalDateTime fechaPago) {
        this.fechaPago = fechaPago;
    }
}
