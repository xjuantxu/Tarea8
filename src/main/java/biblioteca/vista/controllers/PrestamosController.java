package biblioteca.vista.controllers;

import biblioteca.controlador.Controlador;
import biblioteca.modelo.Modelo;
import biblioteca.modelo.dominio.Audiolibro;
import biblioteca.modelo.dominio.Prestamo;
import biblioteca.modelo.negocio.Dialogos;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

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

        this.controlador = controlador;
        cargarPrestamos();
    }

    @FXML
    public void initialize() {
        colID.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUsuario().getDni()));
        colNombre.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUsuario().getNombre()));
        colISBN.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getLibro().getISBN()));
        colTitulo.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getLibro().getTitulo()));
        colFechaInicio.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getInicio())));
        colFechaFin.setCellValueFactory(cellData -> {
            if (cellData.getValue().getFin() == null) {
                return new SimpleStringProperty("");
            }

            return new SimpleStringProperty(String.valueOf(cellData.getValue().getFin()));
        });

        if (controlador == null) {
            Modelo modelo = new Modelo();
            controlador = new Controlador(modelo);
            controlador.comenzar();
        }

        ToggleGroup grupoFiltro = new ToggleGroup();
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
        prestamos = controlador.listadoPrestamos();
        aplicarFiltro();
    }

    private void aplicarFiltro() {
        if (prestamos == null) {
            tablaPrestamos.setItems(FXCollections.observableArrayList());
            return;
        }

        List<Prestamo> prestamosFiltrados = prestamos.stream()
                .filter(this::cumpleFiltroSeleccionado)
                .filter(this::cumpleBusqueda)
                .toList();

        tablaPrestamos.setItems(FXCollections.observableArrayList(prestamosFiltrados));
    }

    private boolean cumpleFiltroSeleccionado(Prestamo prestamo) {
        if (radioAudiolibros.isSelected()) {
            return prestamo.getLibro() instanceof Audiolibro;
        }

        if (radioLibros.isSelected()) {
            return !(prestamo.getLibro() instanceof Audiolibro);
        }

        return true;
    }

    private boolean cumpleBusqueda(Prestamo prestamo) {
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

    public void onNuevoClick(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/biblioteca/prestamo-view.fxml")
            );

            Dialog<Boolean> dialog = new Dialog<>();
            dialog.setTitle("Nuevo Préstamo");
            dialog.getDialogPane().setContent(loader.load());

            PrestamoController prestamoController = loader.getController();
            prestamoController.setControlador(controlador);

            ButtonType btnPrestar = new ButtonType("Prestar", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(btnPrestar, ButtonType.CANCEL);

            dialog.setResultConverter(buttonType -> buttonType == btnPrestar);

            dialog.showAndWait().ifPresent(crear -> {
                if (!crear) {
                    return;
                }

                try {
                    boolean confirmado = Dialogos.mostrarDialogoConfirmacion(
                            "Confirmar préstamo",
                            "¿Desea realizar el préstamo?",
                            null
                    );

                    if (!confirmado) {
                        return;
                    }

                    boolean creado = controlador.prestar(
                            prestamoController.getLibroPrestamo(),
                            prestamoController.getUsuarioPrestamo(),
                            LocalDate.now()
                    );

                    if (creado) {
                        cargarPrestamos();
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

    public void onDevolverClick(ActionEvent actionEvent) {
        Prestamo prestamoSeleccionado = tablaPrestamos.getSelectionModel().getSelectedItem();

        if (prestamoSeleccionado == null) {
            Dialogos.mostrarDialogoAdvertencia("Aviso", "Debe seleccionar un préstamo para devolver.");
            return;
        }

        if (prestamoSeleccionado.getFin() != null) {
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
            boolean devuelto = controlador.devolver(
                    prestamoSeleccionado.getLibro(),
                    prestamoSeleccionado.getUsuario(),
                    LocalDate.now()
            );

            if (devuelto) {
                Dialogos.mostrarDialogoInformacion(
                        "Devolución registrada",
                        "El préstamo se ha marcado como devuelto correctamente."
                );
            } else {
                Dialogos.mostrarDialogoAdvertencia("Aviso", "No se pudo registrar la devolución.");
            }
        } catch (Exception e) {
            Dialogos.mostrarDialogoError("Error al devolver préstamo", e.getMessage());
        } finally {
            cargarPrestamos();
        }
    }

    @FXML
    private void onVolverClick(javafx.event.ActionEvent event) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/biblioteca/menu-view.fxml")
            );

            javafx.scene.Scene scene = new javafx.scene.Scene(loader.load());

            Object controller = loader.getController();
            if (controller instanceof MenuController mc) {
                mc.setControlador(controlador);
            }

            javafx.stage.Stage stage = (javafx.stage.Stage) tablaPrestamos.getScene().getWindow();

            stage.setScene(scene);

        } catch (Exception e) {
            Dialogos.mostrarDialogoError("Error al crear", e.getMessage());
        }
    }

    public void AcercaDeShow(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/biblioteca/acercade-view.fxml")
            );

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
