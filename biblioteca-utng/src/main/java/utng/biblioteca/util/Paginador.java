package utng.biblioteca.util;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;

import java.util.ArrayList;
import java.util.List;

/**
 * Pagina una TableView sin cambiar cómo se consultan los datos.
 *
 * El DAO sigue devolviendo la lista completa; este paginador solo decide qué
 * porción se muestra. Para los volúmenes de una biblioteca escolar eso es
 * suficiente y evita complicar cada consulta con OFFSET/FETCH.
 *
 * Uso típico desde un controlador:
 * <pre>
 *   paginador = new Paginador&lt;&gt;(tblMateriales, cmbPorPagina,
 *                                lblPagina, btnAnterior, btnSiguiente);
 *   paginador.setDatos(listaCompleta);
 * </pre>
 *
 * @param <T> tipo de fila de la tabla
 */
public class Paginador<T> {

    /** Opciones de "cuántos registros por página" que se ofrecen. */
    private static final Integer[] TAMANOS = {10, 25, 50, 100};

    private final TableView<T> tabla;
    private final ComboBox<Integer> cmbTamano;
    private final Label lblEstado;
    private final Button btnAnterior;
    private final Button btnSiguiente;

    private final List<T> datos = new ArrayList<>();
    private final ObservableList<T> pagina = FXCollections.observableArrayList();

    private int paginaActual;

    public Paginador(TableView<T> tabla,
                     ComboBox<Integer> cmbTamano,
                     Label lblEstado,
                     Button btnAnterior,
                     Button btnSiguiente) {

        this.tabla = tabla;
        this.cmbTamano = cmbTamano;
        this.lblEstado = lblEstado;
        this.btnAnterior = btnAnterior;
        this.btnSiguiente = btnSiguiente;

        tabla.setItems(pagina);

        cmbTamano.getItems().setAll(TAMANOS);
        cmbTamano.getSelectionModel().selectFirst();
        cmbTamano.setOnAction(e -> {
            paginaActual = 0;
            refrescar();
        });

        btnAnterior.setOnAction(e -> irA(paginaActual - 1));
        btnSiguiente.setOnAction(e -> irA(paginaActual + 1));

        // Las flechas no llevan texto para no robar ancho a la tabla.
        btnAnterior.setTooltip(new Tooltip("Página anterior"));
        btnSiguiente.setTooltip(new Tooltip("Página siguiente"));
    }

    /** Reemplaza los datos y vuelve a la primera página. */
    public void setDatos(List<T> nuevos) {
        datos.clear();
        if (nuevos != null) {
            datos.addAll(nuevos);
        }
        paginaActual = 0;
        refrescar();
    }

    /**
     * Recarga los datos conservando la página actual.
     * Se usa después de guardar, para no devolver al usuario al inicio.
     */
    public void actualizarDatos(List<T> nuevos) {
        int pagina = paginaActual;
        setDatos(nuevos);
        irA(pagina);
    }

    public List<T> getDatos() {
        return List.copyOf(datos);
    }

    public int getTotal() {
        return datos.size();
    }

    private int tamanoPagina() {
        Integer valor = cmbTamano.getValue();
        return (valor == null || valor <= 0) ? TAMANOS[0] : valor;
    }

    private int totalPaginas() {
        if (datos.isEmpty()) {
            return 1;
        }
        return (int) Math.ceil(datos.size() / (double) tamanoPagina());
    }

    private void irA(int destino) {
        int ultima = totalPaginas() - 1;
        paginaActual = Math.max(0, Math.min(destino, ultima));
        refrescar();
    }

    /**
     * Selecciona la fila indicada aunque esté en otra página: primero salta a
     * la página que la contiene. Se usa al volver de guardar un registro.
     */
    public void seleccionar(T elemento) {
        if (elemento == null) {
            return;
        }
        int indice = datos.indexOf(elemento);
        if (indice < 0) {
            return;
        }

        irA(indice / tamanoPagina());
        tabla.getSelectionModel().select(elemento);
        tabla.scrollTo(elemento);
    }

    private void refrescar() {
        int tamano = tamanoPagina();
        int ultima = totalPaginas() - 1;

        if (paginaActual > ultima) {
            paginaActual = Math.max(0, ultima);
        }

        int desde = paginaActual * tamano;
        int hasta = Math.min(desde + tamano, datos.size());

        pagina.setAll(desde >= hasta ? List.of() : datos.subList(desde, hasta));

        if (datos.isEmpty()) {
            lblEstado.setText("Sin registros");
        } else {
            lblEstado.setText((desde + 1) + "-" + hasta + " de " + datos.size()
                            + "  ·  pág. " + (paginaActual + 1) + "/" + totalPaginas());
        }

        btnAnterior.setDisable(paginaActual <= 0);
        btnSiguiente.setDisable(paginaActual >= ultima);
    }
}
