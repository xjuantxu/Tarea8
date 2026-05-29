package biblioteca.vista.controllers;

import biblioteca.modelo.dominio.*;
import biblioteca.modelo.negocio.Dialogos;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

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
        cmbCategoria.setItems(FXCollections.observableArrayList(Categoria.values()));
        cmbCategoria.getSelectionModel().selectFirst();
        cmbTipo.setItems(FXCollections.observableArrayList("Libro", "Audiolibro"));
        cmbTipo.getSelectionModel().selectFirst();
        actualizarCamposAudiolibro();
        cmbTipo.valueProperty().addListener((observable, anterior, seleccionado) -> actualizarCamposAudiolibro());
        listaAutores.setItems(FXCollections.observableArrayList(autores));
        listaAutores.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        btnDeleteAutor.setDisable(true);
        btnDeleteAutor.disableProperty().bind(
                listaAutores.getSelectionModel().selectedItemProperty().isNull()
        );
    }

    @FXML
    private void onAddAutor() {

        if (autores.size() >= 3) {
            Dialogos.mostrarDialogoError("Error","No se pueden añadir más de 3 autores");
            return;
        }

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

        listaAutores.setItems(
                FXCollections.observableArrayList(autores)
        );

        txtNombreAutor.clear();
        txtApellidosAutor.clear();
        txtNacionalidadAutor.clear();
    }

    private void actualizarCamposAudiolibro() {
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

        txtISBN.setText(libro.getISBN());
        txtISBN.setDisable(true);
        txtTitulo.setText(libro.getTitulo());
        txtAnio.setText(String.valueOf(libro.getAnio()));
        cmbCategoria.setValue(libro.getCategoria());

        if (libro instanceof Audiolibro audiolibro) {
            cmbTipo.setValue("Audiolibro");
            txtFormato.setText(audiolibro.getFormato());
            txtDuracion.setText(String.valueOf(audiolibro.getDuracion().getSeconds()));
        } else {
            cmbTipo.setValue("Libro");
        }

        autores.clear();
        for (Autor autor : libro.getAutores()) {
            if (autor != null) {
                autores.add(autor);
            }
        }
        listaAutores.setItems(FXCollections.observableArrayList(autores));
    }
    public Libro getLibro() {

        String isbn = txtISBN.getText();
        String titulo = txtTitulo.getText();
        int anio = Integer.parseInt(txtAnio.getText());
        Categoria categoria = cmbCategoria.getValue();

        Libro libro;

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

        for (Autor a : autores) {
            libro.addAutor(a);
        }

        return libro;
    }

    @FXML
    private void onDeleteAutor(ActionEvent actionEvent) {
        Autor autorSeleccionado = listaAutores.getSelectionModel().getSelectedItem();

        if (autorSeleccionado == null) {
            return;
        }

        autores.remove(autorSeleccionado);
        listaAutores.getItems().remove(autorSeleccionado);
        listaAutores.getSelectionModel().clearSelection();
    }
}