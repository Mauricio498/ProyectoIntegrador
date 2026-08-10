package biblioteca.utng.util;

import biblioteca.utng.model.EstadoPrestamo;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;

/**
 * Utilidades para dar estilo moderno a las columnas de {@code TableView}
 * usadas en distintas pantallas (badges de estado, formato de fechas).
 */
public final class TableFactory {

    private TableFactory() {
    }

    /** Configura una columna para mostrar el estado del préstamo como un badge de color. */
    public static <S> void configurarColumnaBadge(TableColumn<S, EstadoPrestamo> columna) {
        columna.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(EstadoPrestamo estado, boolean empty) {
                super.updateItem(estado, empty);
                if (empty || estado == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }
                Label badge = new Label(estado.getEtiqueta());
                badge.getStyleClass().addAll("badge", estado.getCssClass());
                setGraphic(badge);
            }
        });
    }
}
