package utng.biblioteca.service;

import utng.biblioteca.dao.impl.MultaDaoImpl;
import utng.biblioteca.dao.interfaces.MultaDao;
import utng.biblioteca.dto.Resultado;
import utng.biblioteca.model.Multa;
import utng.biblioteca.session.Sesion;

import java.math.BigDecimal;
import java.util.List;

/** Consulta de multas y registro de pagos. */
public class MultaService extends ServicioBase {

    private final MultaDao multaDao;

    public MultaService() {
        this(new MultaDaoImpl());
    }

    public MultaService(MultaDao multaDao) {
        this.multaDao = multaDao;
    }

    /** Bandeja del administrador. */
    public List<String[]> listarParaGestion(boolean soloPendientes) {
        return consultar("listar multas", () -> multaDao.listarConDetalle(null, soloPendientes));
    }

    /** Multas del usuario en sesion. */
    public List<Multa> misMultas(boolean soloPendientes) {
        Integer id = Sesion.getIdUsuario();
        if (id == null) {
            return List.of();
        }
        return consultar("listar mis multas", () -> multaDao.listar(id, soloPendientes));
    }

    public Resultado registrarPago(int idMulta, BigDecimal monto) {
        if (!Sesion.esPersonalBiblioteca()) {
            return Resultado.error("Solo el personal de biblioteca registra pagos.");
        }
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            return Resultado.error("El monto debe ser mayor a cero.");
        }
        return operar("registrar el pago",
                () -> multaDao.registrarPago(idMulta, Sesion.getIdUsuario(), monto));
    }

    /** Condonar una multa queda reservado al superadministrador. */
    public Resultado condonar(int idMulta) {
        if (!Sesion.esSuperAdministrador()) {
            return Resultado.error("Solo el superadministrador puede condonar multas.");
        }
        return operar("condonar la multa", () -> {
            multaDao.condonar(idMulta);
            return Resultado.ok("Multa condonada.");
        });
    }

    public BigDecimal adeudoDe(int idUsuario) {
        return valor("consultar adeudo", () -> multaDao.adeudoDe(idUsuario), () -> BigDecimal.ZERO);
    }
}
