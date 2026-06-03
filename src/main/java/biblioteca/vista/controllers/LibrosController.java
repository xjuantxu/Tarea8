package biblioteca.vista.controllers;

import biblioteca.controlador.Controlador;
import biblioteca.modelo.Modelo;
import biblioteca.modelo.dominio.Libro;
import biblioteca.modelo.negocio.Dialogos;
import biblioteca.modelo.negocio.recursos.LocalizadorRecursos;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.SimpleStringProperty;
import biblioteca.modelo.dominio.Audiolibro;

import java.io.IOException;
import java.util.List;

// Controlador de la pantalla donde se muestran los libros.
public class LibrosController {

    public MenuItem cxtEliminar;
    public MenuItem menuVolver;
    public MenuItem menuEliminar;
    private Controlador controlador;
    private List<Libro> libros;

    @FXML
    private TableView<Libro> tablaLibros;


    @FXML
    private TableColumn<Libro, String> colISBN;
    @FXML
    private TableColumn<Libro, String> colTitulo;

    @FXML
    private TableColumn<Libro, String> colAutores;

    @FXML
    private TableColumn<Libro, Integer> colAnio;

    @FXML
    private TableColumn<Libro, String> colCategoria;

    @FXML
    private TableColumn<Libro, String> colFormato;

    @FXML
    private TableColumn<Libro, String> colDuracion;

    @FXML
    private RadioButton radioTodos;

    @FXML
    private RadioButton radioLibros;

    @FXML
    private RadioButton radioAudiolibros;

    @FXML
    private TextField txtBuscar;

    @FXML
    public void initialize() {

        // Se crea el controlador principal y se inicia la conexion.
        Modelo modelo = new Modelo();
        controlador = new Controlador(modelo);
        controlador.comenzar();

        // Los audiolibros se muestran con otro color para distinguirlos.
        tablaLibros.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Libro libro, boolean empty) {
                super.updateItem(libro, empty);

                if (libro == null || empty) {
                    setStyle("");
                } else if (libro instanceof Audiolibro) {
                    setStyle("-fx-background-color: #e8f5e9;");
                } else {
                    setStyle("");
                }
            }
        });

        // Se configuran las columnas de la tabla.
        colISBN.setCellValueFactory(new PropertyValueFactory<>("ISBN"));
        colTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colAnio.setCellValueFactory(new PropertyValueFactory<>("anio"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colAutores.setCellValueFactory(cellData -> {
            Libro libro = cellData.getValue();

            StringBuilder autores = new StringBuilder();

            // Se juntan los autores en un solo texto para mostrarlos en la tabla.
            for (var autor : libro.getAutores()) {
                if (autor != null) {
                    if (!autores.isEmpty()) autores.append(", ");
                    autores.append(autor.getNombre()).append(" ").append(autor.getApellidos());
                }
            }

            return new javafx.beans.property.SimpleStringProperty(autores.toString());
        });
        colFormato.setCellValueFactory(cellData -> {
            Libro libro = cellData.getValue();

            if (libro instanceof Audiolibro audio) {
                return new SimpleStringProperty(audio.getFormato());
            }

            return new SimpleStringProperty("");
        });
        colDuracion.setCellValueFactory(cellData -> {
            Libro libro = cellData.getValue();

            if (libro instanceof Audiolibro audio) {

                // La duracion se muestra con formato horas:minutos:segundos.
                long segundos = audio.getDuracion().getSeconds();
                long h = segundos / 3600;
                long m = (segundos % 3600) / 60;
                long s = segundos % 60;

                String tiempo = String.format("%02d:%02d:%02d", h, m, s);

                return new SimpleStringProperty(tiempo);
            }

            return new SimpleStringProperty("");
        });

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

        cargarLibros();
    }

    public void setControlador(Controlador controlador) {
        if (controlador == null) {
            return;
        }

        this.controlador = controlador;
        cargarLibros();
    }

    private void cargarLibros() {
        // Se cargan los libros desde el controlador y se aplica el filtro actual.
        libros = controlador.listadoLibros();
        aplicarFiltro();
    }

    private void aplicarFiltro() {
        if (libros == null) {
            tablaLibros.setItems(FXCollections.observableArrayList());
            return;
        }

        // Primero filtra por tipo y luego por el texto de busqueda.
        List<Libro> librosFiltrados = libros.stream()
                .filter(this::cumpleFiltroSeleccionado)
                .filter(this::cumpleBusqueda)
                .toList();

        tablaLibros.setItems(FXCollections.observableArrayList(librosFiltrados));
    }

    private boolean cumpleFiltroSeleccionado(Libro libro) {
        // Comprueba si el libro cumple el filtro de tipo seleccionado.
        if (radioAudiolibros.isSelected()) {
            return libro instanceof Audiolibro;
        }

        if (radioLibros.isSelected()) {
            return !(libro instanceof Audiolibro);
        }

        return true;
    }

    private boolean cumpleBusqueda(Libro libro) {
        // Comprueba si el texto buscado aparece en algun dato del libro.
        String texto = txtBuscar.getText();

        if (texto == null || texto.isBlank()) {
            return true;
        }

        String busqueda = texto.toLowerCase();

        return contiene(libro.getISBN(), busqueda)
                || contiene(libro.getTitulo(), busqueda)
                || contiene(String.valueOf(libro.getAnio()), busqueda)
                || contiene(String.valueOf(libro.getCategoria()), busqueda)
                || contieneAutores(libro, busqueda)
                || contieneDatosAudiolibro(libro, busqueda);
    }

    private boolean contiene(String valor, String busqueda) {
        return valor != null && valor.toLowerCase().contains(busqueda);
    }

    private boolean contieneAutores(Libro libro, String busqueda) {
        // Busca tambien por nombre y apellidos de los autores.
        for (var autor : libro.getAutores()) {
            if (autor != null && contiene(autor.getNombre() + " " + autor.getApellidos(), busqueda)) {
                return true;
            }
        }

        return false;
    }

    private boolean contieneDatosAudiolibro(Libro libro, String busqueda) {
        // Si es audiolibro, tambien se puede buscar por formato o duracion.
        if (!(libro instanceof Audiolibro audio)) {
            return false;
        }

        return contiene(audio.getFormato(), busqueda)
                || contiene(String.valueOf(audio.getDuracion().getSeconds()), busqueda);
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
            javafx.stage.Stage stage = (javafx.stage.Stage) tablaLibros.getScene().getWindow();

            stage.setScene(scene);

        } catch (Exception e) {
            Dialogos.mostrarDialogoError("Error al crear", e.getMessage());
        }
    }

    @FXML
    private void onNuevoClick() {
        try {
            // Se abre el formulario para crear un nuevo libro.
            FXMLLoader loader = new FXMLLoader(LocalizadorRecursos.class.getResource("/biblioteca/libro-view.fxml"));

            Dialog<Libro> dialog = new Dialog<>();
            dialog.setTitle("Nuevo Libro");

            dialog.getDialogPane().setContent(loader.load());

            ButtonType btnOk = new ButtonType("Crear", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(btnOk, ButtonType.CANCEL);

            LibroController controller = loader.getController();

            dialog.setResultConverter(button -> {
                if (button == btnOk) {
                    // Antes de crear el libro se pide confirmacion.
                    boolean confirmado = Dialogos.mostrarDialogoConfirmacion(
                            "Confirmar alta",
                            "Desea crear el libro?",
                            null
                    );

                    if (!confirmado) {
                        return null;
                    }

                    return controller.getLibro();
                }
                return null;
            });

            dialog.showAndWait().ifPresent(libro -> {
                try {
                    // Si se crea correctamente, se recarga la tabla.
                    if (controlador.alta(libro)) {
                        cargarLibros();
                        mostrarLibroEnTabla(libro);
                    } else {
                        Dialogos.mostrarDialogoAdvertencia("Aviso", "No se pudo crear el libro.");
                    }
                } catch (Exception e) {
                    Dialogos.mostrarDialogoError("Error al crear", e.getMessage());
                }
            });

        } catch (Exception e) {
            Dialogos.mostrarDialogoError("Error al crear", e.getMessage());
        }
    }

    private void mostrarLibroEnTabla(Libro libro) {
        // Intenta dejar seleccionado el libro que se acaba de crear.
        OnSeleccionarLibro(libro);

        if (tablaLibros.getSelectionModel().isEmpty()) {
            radioTodos.setSelected(true);
            OnSeleccionarLibro(libro);
        }
    }

    private void OnSeleccionarLibro(Libro libro) {
        // Busca el libro por ISBN dentro de la tabla y lo selecciona.
        tablaLibros.getSelectionModel().clearSelection();

        for (Libro libroTabla : tablaLibros.getItems()) {
            if (libroTabla.getISBN().equals(libro.getISBN())) {
                int indice = tablaLibros.getItems().indexOf(libroTabla);
                tablaLibros.getSelectionModel().select(libroTabla);
                tablaLibros.getFocusModel().focus(indice);
                tablaLibros.scrollTo(indice);
                return;
            }
        }
    }

    public void onEliminarClick() {
        // Se elimina el libro seleccionado despues de confirmar.
        Libro libroSeleccionado = tablaLibros.getSelectionModel().getSelectedItem();

        if (libroSeleccionado == null) {
            Dialogos.mostrarDialogoError("Error", "Debe seleccionar un libro para eliminar.");
            return;
        }

        boolean confirmado = Dialogos.mostrarDialogoConfirmacion(
                "Confirmar eliminacion",
                "Desea eliminar el libro seleccionado?",
                null
        );

        if (!confirmado) {
            return;
        }

        try {
            if (controlador.baja(libroSeleccionado)) {
                cargarLibros();
            } else {
                Dialogos.mostrarDialogoAdvertencia("Aviso", "No se pudo eliminar el libro.");
            }
        } catch (Exception e) {
            Dialogos.mostrarDialogoError("Error al eliminar", e.getMessage());
        }
    }

    public void AcercaDeShow () {
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
