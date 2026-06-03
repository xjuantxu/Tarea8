package biblioteca.vista.controllers;

import biblioteca.controlador.Controlador;
import biblioteca.modelo.Modelo;
import biblioteca.modelo.dominio.Audiolibro;
import biblioteca.modelo.dominio.Prestamo;
import biblioteca.modelo.negocio.Dialogos;
import biblioteca.modelo.negocio.recursos.LocalizadorRecursos;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

// Controlador de la pantalla donde se muestran los prestamos.
public class PrestamosController {

    public Button btnNuevo;
    public Button btnDevolver;
    public TextField txtBuscar;
    public RadioButton radioTodos;
    public RadioButton radioLibros;
    public RadioButton radioAudiolibros;
    public MenuItem cxtDevolver;
    public TableView<Prestamo> tablaPrestamos;
    public TableColumn<Prestamo, String> colID;
    public TableColumn<Prestamo, String> colNombre;
    public TableColumn<Prestamo, String> colISBN;
    public TableColumn<Prestamo, String> colTitulo;
    public TableColumn<Prestamo, String> colFechaInicio;
    public TableColumn<Prestamo, String> colFechaFin;
    public MenuItem menuDevolver;
    public MenuItem menuVolver;
    public Button btnVolver;
    private Controlador controlador;
    private List<Prestamo> prestamos;

    public void setControlador(Controlador controlador) {
        if (controlador == null) {
            return;
        }

        // Recibe el controlador principal y carga los prestamos.
        this.controlador = controlador;
        cargarPrestamos();
    }

    @FXML
    public void initialize() {
        // Se configuran las columnas de la tabla.
        colID.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUsuario().getDni()));
        colNombre.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUsuario().getNombre()));
        colISBN.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getLibro().getISBN()));
        colTitulo.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getLibro().getTitulo()));
        colFechaInicio.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getInicio())));
        colFechaFin.setCellValueFactory(cellData -> {
            // Si el prestamo no esta devuelto, no se muestra fecha fin.
            if (cellData.getValue().getFin() == null) {
                return new SimpleStringProperty("");
            }

            return new SimpleStringProperty(String.valueOf(cellData.getValue().getFin()));
        });

        if (controlador == null) {
            // Si no recibe controlador, crea uno propio.
            Modelo modelo = new Modelo();
            controlador = new Controlador(modelo);
            controlador.comenzar();
        }

        ToggleGroup grupoFiltro = new ToggleGroup();

        // Solo puede estar seleccionado un filtro cada vez.
        radioTodos.setToggleGroup(grupoFiltro);
        radioLibros.setToggleGroup(grupoFiltro);
        radioAudiolibros.setToggleGroup(grupoFiltro);
        radioTodos.setSelected(true);

        grupoFiltro.selectedToggleProperty().addListener((observable, anterior, seleccionado) -> {
            if (seleccionado == null) {
                anterior.setSelected(true);
                return;
            }

            aplicarFiltro();
        });

        txtBuscar.textProperty().addListener((observable, anterior, texto) -> aplicarFiltro());

        cargarPrestamos();
    }

    private void cargarPrestamos() {
        // Se cargan los prestamos desde el controlador y se aplica el filtro actual.
        prestamos = controlador.listadoPrestamos();
        aplicarFiltro();
    }

    private void aplicarFiltro() {
        if (prestamos == null) {
            tablaPrestamos.setItems(FXCollections.observableArrayList());
            return;
        }

        // Primero filtra por tipo de libro y luego por el texto de busqueda.
        List<Prestamo> prestamosFiltrados = prestamos.stream()
                .filter(this::cumpleFiltroSeleccionado)
                .filter(this::cumpleBusqueda)
                .toList();

        tablaPrestamos.setItems(FXCollections.observableArrayList(prestamosFiltrados));
        tablaPrestamos.refresh();
    }

    private boolean cumpleFiltroSeleccionado(Prestamo prestamo) {
        // Comprueba si el prestamo cumple el filtro seleccionado.
        if (radioAudiolibros.isSelected()) {
            return prestamo.getLibro() instanceof Audiolibro;
        }

        if (radioLibros.isSelected()) {
            return !(prestamo.getLibro() instanceof Audiolibro);
        }

        return true;
    }

    private boolean cumpleBusqueda(Prestamo prestamo) {
        // Comprueba si el texto buscado aparece en algun dato del prestamo.
        String texto = txtBuscar.getText();

        if (texto == null || texto.isBlank()) {
            return true;
        }

        String busqueda = texto.toLowerCase();

        return contiene(prestamo.getUsuario().getDni(), busqueda)
                || contiene(prestamo.getUsuario().getNombre(), busqueda)
                || contiene(prestamo.getLibro().getISBN(), busqueda)
                || contiene(prestamo.getLibro().getTitulo(), busqueda)
                || contiene(String.valueOf(prestamo.getInicio()), busqueda)
                || contiene(String.valueOf(prestamo.getFin()), busqueda);
    }

    private boolean contiene(String valor, String busqueda) {
        return valor != null && valor.toLowerCase().contains(busqueda);
    }

    public void onNuevoClick() {
        try {
            // Se abre el formulario para crear un nuevo prestamo.
            FXMLLoader loader = new FXMLLoader(LocalizadorRecursos.class.getResource("/biblioteca/prestamo-view.fxml"));

            Dialog<Boolean> dialog = new Dialog<>();
            dialog.setTitle("Nuevo Préstamo");
            dialog.getDialogPane().setContent(loader.load());

            PrestamoController prestamoController = loader.getController();
            // Se pasa el mismo controlador al formulario.
            prestamoController.setControlador(controlador);

            ButtonType btnPrestar = new ButtonType("Prestar", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(btnPrestar, ButtonType.CANCEL);

            dialog.setResultConverter(buttonType -> buttonType == btnPrestar);

            dialog.showAndWait().ifPresent(crear -> {
                if (!crear) {
                    return;
                }

                try {
                    // Antes de crear el prestamo se pide confirmacion.
                    boolean confirmado = Dialogos.mostrarDialogoConfirmacion(
                            "Confirmar préstamo",
                            "¿Desea realizar el préstamo?",
                            null
                    );

                    if (!confirmado) {
                        return;
                    }

                    // Se recogen el libro y el usuario del formulario.
                    var libro = prestamoController.getLibroPrestamo();
                    var usuario = prestamoController.getUsuarioPrestamo();
                    LocalDate fechaInicio = LocalDate.now();

                    boolean creado = controlador.prestar(libro, usuario, fechaInicio);

                    if (creado) {
                        cargarPrestamos();
                        seleccionarPrestamo(usuario.getDni(), libro.getISBN(), fechaInicio);
                        Dialogos.mostrarDialogoInformacion(
                                "Préstamo realizado",
                                "El préstamo se ha realizado correctamente."
                        );
                    } else {
                        Dialogos.mostrarDialogoAdvertencia("Aviso", "No se pudo crear el préstamo.");
                    }
                } catch (Exception e) {
                    Dialogos.mostrarDialogoError("Error al crear préstamo", e.getMessage());
                }
            });

        } catch (Exception e) {
            Dialogos.mostrarDialogoError("Error al crear", e.getMessage());
        }
    }

    public void onDevolverClick() {
        // Se devuelve el prestamo seleccionado.
        Prestamo prestamoSeleccionado = tablaPrestamos.getSelectionModel().getSelectedItem();

        if (prestamoSeleccionado == null) {
            Dialogos.mostrarDialogoAdvertencia("Aviso", "Debe seleccionar un préstamo para devolver.");
            return;
        }

        if (prestamoSeleccionado.getFin() != null) {
            // No se puede devolver dos veces el mismo prestamo.
            Dialogos.mostrarDialogoAdvertencia("Aviso", "El préstamo seleccionado ya está devuelto.");
            return;
        }

        boolean confirmado = Dialogos.mostrarDialogoConfirmacion(
                "Confirmar devolución",
                "¿Desea registrar la devolución del préstamo?",
                null
        );

        if (!confirmado) {
            return;
        }

        try {
            // Se registra la devolucion con la fecha actual.
            boolean devuelto = controlador.devolver(
                    prestamoSeleccionado.getLibro(),
                    prestamoSeleccionado.getUsuario(),
                    LocalDate.now()
            );

            if (devuelto) {
                cargarPrestamos();
                tablaPrestamos.getSelectionModel().clearSelection();
                Dialogos.mostrarDialogoInformacion(
                        "Devolución registrada",
                        "El préstamo se ha marcado como devuelto correctamente."
                );
            } else {
                Dialogos.mostrarDialogoAdvertencia("Aviso", "No se pudo registrar la devolución.");
            }
        } catch (Exception e) {
            Dialogos.mostrarDialogoError("Error al devolver préstamo", e.getMessage());
        }
    }

    private void seleccionarPrestamo(String dni, String isbn, LocalDate fechaInicio) {
        tablaPrestamos.getSelectionModel().clearSelection();

        for (Prestamo prestamo : tablaPrestamos.getItems()) {
            if (prestamo.getUsuario().getDni().equals(dni)
                    && prestamo.getLibro().getISBN().equals(isbn)
                    && prestamo.getInicio().equals(fechaInicio)) {
                int indice = tablaPrestamos.getItems().indexOf(prestamo);
                tablaPrestamos.getSelectionModel().select(prestamo);
                tablaPrestamos.getFocusModel().focus(indice);
                tablaPrestamos.scrollTo(indice);
                tablaPrestamos.refresh();
                return;
            }
        }

        tablaPrestamos.refresh();
    }

    @FXML
    private void onVolverClick() {
        try {
            // Volvemos al menu principal manteniendo el mismo controlador.
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(LocalizadorRecursos.class.getResource("/biblioteca/menu-view.fxml"));

            javafx.scene.Scene scene = new javafx.scene.Scene(loader.load());

            // Obtenemos el controlador del menu para pasarle la conexion.
            Object controller = loader.getController();
            if (controller instanceof MenuController mc) {
                mc.setControlador(controlador);
            }

            // Cogemos la ventana actual y cambiamos su escena por la del menu.
            javafx.stage.Stage stage = (javafx.stage.Stage) tablaPrestamos.getScene().getWindow();

            stage.setScene(scene);

        } catch (Exception e) {
            Dialogos.mostrarDialogoError("Error al crear", e.getMessage());
        }
    }

    public void AcercaDeShow() {
        try {
            // Muestra la ventana de informacion de la aplicacion.
            FXMLLoader loader = new FXMLLoader(LocalizadorRecursos.class.getResource("/biblioteca/acercade-view.fxml"));

            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Acerca de");
            dialog.getDialogPane().setContent(loader.load());
            dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
            dialog.showAndWait();

        } catch (IOException e) {
            Dialogos.mostrarDialogoError("Error", "No se pudo cargar la ventana Acerca de.");
        }
    }
}
