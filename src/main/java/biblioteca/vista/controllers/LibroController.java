package biblioteca.vista.controllers;

import biblioteca.modelo.dominio.*;
import biblioteca.modelo.negocio.Dialogos;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

// Controlador del formulario de libro.
public class LibroController {

    @FXML
    public Button btnAddAutor;
    public Button btnDeleteAutor;
    @FXML
    private TextField txtISBN;
    @FXML
    private TextField txtTitulo;
    @FXML
    private TextField txtAnio;
    @FXML
    private ComboBox<Categoria> cmbCategoria;
    @FXML
    private ComboBox<String> cmbTipo;
    @FXML
    private TextField txtFormato;
    @FXML
    private TextField txtDuracion;

    @FXML
    private TextField txtNombreAutor;
    @FXML
    private TextField txtApellidosAutor;
    @FXML
    private TextField txtNacionalidadAutor;
    @FXML
    private ListView<Autor> listaAutores;

    private List<Autor> autores = new ArrayList<>();

    @FXML
    public void initialize() {
        // Se cargan las opciones iniciales de los combos.
        cmbCategoria.setItems(FXCollections.observableArrayList(Categoria.values()));
        cmbCategoria.getSelectionModel().selectFirst();
        cmbTipo.setItems(FXCollections.observableArrayList("Libro", "Audiolibro"));
        cmbTipo.getSelectionModel().selectFirst();

        // Se preparan los campos especiales de audiolibro.
        actualizarCamposAudiolibro();
        cmbTipo.valueProperty().addListener((observable, anterior, seleccionado) -> actualizarCamposAudiolibro());

        // Se prepara la lista de autores.
        listaAutores.setItems(FXCollections.observableArrayList(autores));
        listaAutores.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        btnDeleteAutor.setDisable(true);
        btnDeleteAutor.disableProperty().bind(
                listaAutores.getSelectionModel().selectedItemProperty().isNull()
        );
    }

    @FXML
    private void onAddAutor() {

        // Como maximo se permiten 3 autores.
        if (autores.size() >= 3) {
            Dialogos.mostrarDialogoError("Error","No se pueden añadir más de 3 autores");
            return;
        }

        // Los datos del autor son obligatorios.
        if (txtNombreAutor.getText().isBlank() ||
                txtApellidosAutor.getText().isBlank() ||
                txtNacionalidadAutor.getText().isBlank()) {

            Dialogos.mostrarDialogoError("Error","Todos los campos del autor son obligatorios");
            return;
        }

        Autor autor = new Autor(
                txtNombreAutor.getText(),
                txtApellidosAutor.getText(),
                txtNacionalidadAutor.getText()
        );

        autores.add(autor);
        listaAutores.getItems().add(autor);

        // Se actualiza la lista y se limpian los campos.
        listaAutores.setItems(
                FXCollections.observableArrayList(autores)
        );

        txtNombreAutor.clear();
        txtApellidosAutor.clear();
        txtNacionalidadAutor.clear();
    }

    private void actualizarCamposAudiolibro() {
        // Si es libro normal, no se usan formato ni duracion.
        boolean esLibro = "Libro".equals(cmbTipo.getValue());
        txtFormato.setDisable(esLibro);
        txtDuracion.setDisable(esLibro);

        if (esLibro) {
            txtFormato.clear();
            txtDuracion.clear();
        }
    }

    public void setLibro(Libro libro) {
        if (libro == null) {
            return;
        }

        // Rellena el formulario con los datos de un libro existente.
        txtISBN.setText(libro.getISBN());
        txtISBN.setDisable(true);
        txtTitulo.setText(libro.getTitulo());
        txtAnio.setText(String.valueOf(libro.getAnio()));
        cmbCategoria.setValue(libro.getCategoria());

        // Si es audiolibro tambien se rellenan sus campos propios.
        if (libro instanceof Audiolibro audiolibro) {
            cmbTipo.setValue("Audiolibro");
            txtFormato.setText(audiolibro.getFormato());
            txtDuracion.setText(String.valueOf(audiolibro.getDuracion().getSeconds()));
        } else {
            cmbTipo.setValue("Libro");
        }

        // Se cargan los autores del libro en la lista.
        autores.clear();
        for (Autor autor : libro.getAutores()) {
            if (autor != null) {
                autores.add(autor);
            }
        }
        listaAutores.setItems(FXCollections.observableArrayList(autores));
    }
    public Libro getLibro() {

        // Se recogen los datos escritos en el formulario.
        String isbn = txtISBN.getText();
        String titulo = txtTitulo.getText();
        int anio = Integer.parseInt(txtAnio.getText());
        Categoria categoria = cmbCategoria.getValue();

        Libro libro;

        // Segun el tipo seleccionado se crea un libro o un audiolibro.
        if ("Audiolibro".equals(cmbTipo.getValue())) {

            long segundos = Long.parseLong(txtDuracion.getText());

            libro = new Audiolibro(
                    isbn,
                    titulo,
                    anio,
                    categoria,
                    Duration.ofSeconds(segundos),
                    txtFormato.getText()
            );

        } else {
            libro = new Libro(isbn, titulo, anio, categoria);
        }

        // Se añaden los autores al libro creado.
        for (Autor a : autores) {
            libro.addAutor(a);
        }

        return libro;
    }

    @FXML
    private void onDeleteAutor() {
        // Se elimina el autor seleccionado en la lista.
        Autor autorSeleccionado = listaAutores.getSelectionModel().getSelectedItem();

        if (autorSeleccionado == null) {
            return;
        }

        autores.remove(autorSeleccionado);
        listaAutores.getItems().remove(autorSeleccionado);
        listaAutores.getSelectionModel().clearSelection();
    }
}
