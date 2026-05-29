package biblioteca.vista.controllers;

import biblioteca.controlador.Controlador;
import biblioteca.modelo.Modelo;
import biblioteca.modelo.dominio.Direccion;
import biblioteca.modelo.dominio.Usuario;
import biblioteca.modelo.negocio.Dialogos;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.util.List;

public class UsuariosController {

    @FXML
    public MenuItem menuEditar;
    @FXML
    public MenuItem menuEliminar;
    @FXML
    public MenuItem menuVolver;
    @FXML
    public MenuItem cxtEditar;
    @FXML
    public MenuItem cxtEliminar;
    @FXML
    public TableView<Usuario> tablaUsuarios;
    @FXML
    public TableColumn<Usuario, String> colID;
    @FXML
    public TableColumn<Usuario, String> colNombre;
    @FXML
    public TableColumn<Usuario, String> colEmail;
    @FXML
    public TableColumn<Usuario, String> colVia;
    @FXML
    public TableColumn<Usuario, String> colNumero;
    @FXML
    public TableColumn<Usuario, String> colCP;
    @FXML
    public TableColumn<Usuario, String> colLocalidad;
    @FXML
    public TextField txtBuscar;
    @FXML
    public Button btnNuevo;
    private Controlador controlador;
    private List<Usuario> usuarios;

    public void setControlador(Controlador controlador) {
        if (controlador == null) {
            return;
        }

        this.controlador = controlador;
        cargarUsuarios();
    }

    @FXML
    public void initialize() {
        colID.setCellValueFactory(new PropertyValueFactory<>("dni"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colVia.setCellValueFactory(cellData -> new SimpleStringProperty(getVia(cellData.getValue())));
        colNumero.setCellValueFactory(cellData -> new SimpleStringProperty(getNumero(cellData.getValue())));
        colCP.setCellValueFactory(cellData -> new SimpleStringProperty(getCp(cellData.getValue())));
        colLocalidad.setCellValueFactory(cellData -> new SimpleStringProperty(getLocalidad(cellData.getValue())));

        if (controlador == null) {
            Modelo modelo = new Modelo();
            controlador = new Controlador(modelo);
            controlador.comenzar();
        }

        txtBuscar.textProperty().addListener((observable, anterior, texto) -> aplicarFiltro());

        cargarUsuarios();
    }

    private void cargarUsuarios() {
        usuarios = controlador.listadoUsuarios();
        aplicarFiltro();
    }

    private void aplicarFiltro() {
        if (usuarios == null) {
            tablaUsuarios.setItems(FXCollections.observableArrayList());
            return;
        }

        List<Usuario> usuariosFiltrados = usuarios.stream()
                .filter(this::cumpleBusqueda)
                .toList();

        tablaUsuarios.setItems(FXCollections.observableArrayList(usuariosFiltrados));
    }

    private boolean cumpleBusqueda(Usuario usuario) {
        String texto = txtBuscar.getText();

        if (texto == null || texto.isBlank()) {
            return true;
        }

        String busqueda = texto.toLowerCase();

        return contiene(usuario.getDni(), busqueda)
                || contiene(usuario.getNombre(), busqueda)
                || contiene(usuario.getEmail(), busqueda)
                || contiene(getVia(usuario), busqueda)
                || contiene(getNumero(usuario), busqueda)
                || contiene(getCp(usuario), busqueda)
                || contiene(getLocalidad(usuario), busqueda);
    }

    private boolean contiene(String valor, String busqueda) {
        return valor != null && valor.toLowerCase().contains(busqueda);
    }

    private String getVia(Usuario usuario) {
        Direccion direccion = usuario.getDireccion();
        return direccion != null ? direccion.getVia() : "";
    }

    private String getNumero(Usuario usuario) {
        Direccion direccion = usuario.getDireccion();
        return direccion != null ? direccion.getNumero() : "";
    }

    private String getCp(Usuario usuario) {
        Direccion direccion = usuario.getDireccion();
        return direccion != null ? direccion.getCp() : "";
    }

    private String getLocalidad(Usuario usuario) {
        Direccion direccion = usuario.getDireccion();
        return direccion != null ? direccion.getLocalidad() : "";
    }


    public void onEditarClick(ActionEvent actionEvent) {
        Usuario usuarioSeleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();

        if (usuarioSeleccionado == null) {
            Dialogos.mostrarDialogoError("Error", "Debe seleccionar un usuario para editar.");
            return;
        }

        seleccionarUsuario(usuarioSeleccionado);

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/biblioteca/usuario-view.fxml")
            );

            Dialog<Usuario> dialog = new Dialog<>();
            dialog.setTitle("Editar Usuario");
            dialog.getDialogPane().setContent(loader.load());

            ButtonType btnOk = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(btnOk, ButtonType.CANCEL);

            UsuarioController controller = loader.getController();
            controller.setUsuario(usuarioSeleccionado);

            dialog.setResultConverter(button -> {
                if (button == btnOk) {
                    boolean confirmado = Dialogos.mostrarDialogoConfirmacion(
                            "Confirmar edicion",
                            "�Desea guardar los cambios del usuario?",
                            null
                    );

                    if (!confirmado) {
                        return null;
                    }

                    return controller.getUsuario();
                }
                return null;
            });

            dialog.showAndWait().ifPresent(usuario -> {
                try {
                    if (controlador.modificar(usuario)) {
                        cargarUsuarios();
                        mostrarUsuarioEnTabla(usuario);
                    } else {
                        Dialogos.mostrarDialogoAdvertencia("Aviso", "No se pudo modificar el usuario.");
                    }
                } catch (Exception e) {
                    Dialogos.mostrarDialogoError("Error al modificar", e.getMessage());
                }
            });

        } catch (Exception e) {
            Dialogos.mostrarDialogoError("Error al modificar", e.getMessage());
        }
    }

    public void onEliminarClick(ActionEvent actionEvent) {
        Usuario usuarioSeleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();

        if (usuarioSeleccionado == null) {
            Dialogos.mostrarDialogoError("Error", "Debe seleccionar un usuario para eliminar.");
            return;
        }

        boolean confirmado = Dialogos.mostrarDialogoConfirmacion(
                "Confirmar eliminacion",
                "¿Desea eliminar el usuario seleccionado?",
                null
        );

        if (!confirmado) {
            return;
        }

        try {
            if (controlador.baja(usuarioSeleccionado)) {
                cargarUsuarios();
            } else {
                Dialogos.mostrarDialogoAdvertencia("Aviso", "No se pudo eliminar el usuario.");
            }
        } catch (Exception e) {
            Dialogos.mostrarDialogoError("Error al eliminar", e.getMessage());
        }
    }

    public void onNuevoClick(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/biblioteca/usuario-view.fxml")
            );

            Dialog<Usuario> dialog = new Dialog<>();
            dialog.setTitle("Nuevo Usuario");
            dialog.getDialogPane().setContent(loader.load());

            ButtonType btnOk = new ButtonType("Crear", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(btnOk, ButtonType.CANCEL);

            UsuarioController controller = loader.getController();

            dialog.setResultConverter(button -> {
                if (button == btnOk) {
                    boolean confirmado = Dialogos.mostrarDialogoConfirmacion(
                            "Confirmar alta",
                            "�Desea crear el usuario?",
                            null
                    );

                    if (!confirmado) {
                        return null;
                    }

                    return controller.getUsuario();
                }
                return null;
            });

            dialog.showAndWait().ifPresent(usuario -> {
                try {
                    if (controlador.alta(usuario)) {
                        cargarUsuarios();
                        mostrarUsuarioEnTabla(usuario);
                    } else {
                        Dialogos.mostrarDialogoAdvertencia("Aviso", "No se pudo crear el usuario.");
                    }
                } catch (Exception e) {
                    Dialogos.mostrarDialogoError("Error al crear", e.getMessage());
                }
            });

        } catch (Exception e) {
            Dialogos.mostrarDialogoError("Error al crear", e.getMessage());
        }
    }

    private void mostrarUsuarioEnTabla(Usuario usuario) {
        seleccionarUsuario(usuario);

        if (tablaUsuarios.getSelectionModel().isEmpty()) {
            txtBuscar.clear();
            seleccionarUsuario(usuario);
        }

        tablaUsuarios.refresh();
    }
    private void seleccionarUsuario(Usuario usuario) {
        tablaUsuarios.getSelectionModel().clearSelection();

        for (Usuario usuarioTabla : tablaUsuarios.getItems()) {
            if (usuarioTabla.getDni().equals(usuario.getDni())) {
                int indice = tablaUsuarios.getItems().indexOf(usuarioTabla);
                tablaUsuarios.getSelectionModel().select(usuarioTabla);
                tablaUsuarios.getFocusModel().focus(indice);
                tablaUsuarios.scrollTo(indice);
                return;
            }
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

            javafx.stage.Stage stage = (javafx.stage.Stage) tablaUsuarios.getScene().getWindow();

            stage.setScene(scene);

        } catch (Exception e) {
            Dialogos.mostrarDialogoError("Error al crear", e.getMessage());
        }
    }

    public void AcercaDeShow (ActionEvent event) {
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
