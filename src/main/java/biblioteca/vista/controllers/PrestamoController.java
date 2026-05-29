package biblioteca.vista.controllers;

import biblioteca.controlador.Controlador;
import biblioteca.modelo.Modelo;
import biblioteca.modelo.dominio.Direccion;
import biblioteca.modelo.dominio.Libro;
import biblioteca.modelo.dominio.Usuario;
import biblioteca.modelo.negocio.Dialogos;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class PrestamoController {
    public Label lblID;
    public Label lblNombre;
    public Label lblEmail;
    public Label lblVia;
    public Label lblNumero;
    public Label lblCP;
    public Label lblLocalidad;
    public Label lblISBN;
    public Label lblTitulo;
    public Label lblCategoria;
    public TextField txtID;
    public Button btnBuscarID;
    public TextField txtISBN;
    public Button btnBuscarISBN;
    private Controlador controlador;

    public void setControlador(Controlador controlador) {
        if (controlador == null) {
            return;
        }

        this.controlador = controlador;
    }

    @FXML
    public void initialize() {
        if (controlador == null) {
            Modelo modelo = new Modelo();
            controlador = new Controlador(modelo);
            controlador.comenzar();
        }

        limpiarLabels();
    }

    private void limpiarLabels() {
        lblID.setText("");
        lblNombre.setText("");
        lblEmail.setText("");
        lblVia.setText("");
        lblNumero.setText("");
        lblCP.setText("");
        lblLocalidad.setText("");
        lblISBN.setText("");
        lblTitulo.setText("");
        lblCategoria.setText("");
    }

    public void OnBuscarISBN(ActionEvent actionEvent) {
        try {
            String isbn = txtISBN.getText();

            if (isbn == null || isbn.isBlank()) {
                Dialogos.mostrarDialogoAdvertencia("Aviso", "Introduce un ISBN para buscar.");
                return;
            }

            Libro libro = controlador.buscar(new Libro(isbn.trim()));

            if (libro == null) {
                limpiarLabelsLibro();
                Dialogos.mostrarDialogoAdvertencia("Aviso", "No existe un libro con ese ISBN.");
                return;
            }

            cargarDatosLibro(libro);

        } catch (IllegalArgumentException e) {
            Dialogos.mostrarDialogoError("ISBN inválido", e.getMessage());
        } catch (Exception e) {
            Dialogos.mostrarDialogoError("Error al buscar libro", e.getMessage());
        }
    }

    public void OnBuscarID(ActionEvent actionEvent) {
        try {
            String dni = txtID.getText();

            if (dni == null || dni.isBlank()) {
                Dialogos.mostrarDialogoAdvertencia("Aviso", "Introduce un DNI para buscar.");
                return;
            }

            Usuario usuario = controlador.buscar(new Usuario(dni.trim().toUpperCase()));

            if (usuario == null) {
                limpiarLabelsUsuario();
                Dialogos.mostrarDialogoAdvertencia("Aviso", "No existe un usuario con ese DNI.");
                return;
            }

            cargarDatosUsuario(usuario);

        } catch (IllegalArgumentException e) {
            Dialogos.mostrarDialogoError("DNI inválido", e.getMessage());
        } catch (Exception e) {
            Dialogos.mostrarDialogoError("Error al buscar usuario", e.getMessage());
        }
    }

    private void cargarDatosUsuario(Usuario usuario) {
        lblID.setText(valorSeguro(usuario.getDni()));
        lblNombre.setText(valorSeguro(usuario.getNombre()));
        lblEmail.setText(valorSeguro(usuario.getEmail()));

        Direccion direccion = usuario.getDireccion();
        if (direccion == null) {
            lblVia.setText("");
            lblNumero.setText("");
            lblCP.setText("");
            lblLocalidad.setText("");
            return;
        }

        lblVia.setText(valorSeguro(direccion.getVia()));
        lblNumero.setText(valorSeguro(direccion.getNumero()));
        lblCP.setText(valorSeguro(direccion.getCp()));
        lblLocalidad.setText(valorSeguro(direccion.getLocalidad()));
    }

    private void limpiarLabelsUsuario() {
        lblID.setText("");
        lblNombre.setText("");
        lblEmail.setText("");
        lblVia.setText("");
        lblNumero.setText("");
        lblCP.setText("");
        lblLocalidad.setText("");
    }

    private void cargarDatosLibro(Libro libro) {
        lblISBN.setText(valorSeguro(libro.getISBN()));
        lblTitulo.setText(valorSeguro(libro.getTitulo()));
        lblCategoria.setText(libro.getCategoria() == null ? "" : libro.getCategoria().name());
    }

    private void limpiarLabelsLibro() {
        lblISBN.setText("");
        lblTitulo.setText("");
        lblCategoria.setText("");
    }

    //Metodo que se asegura que no devuelva null, sino que muestre el texto vacío
    private String valorSeguro(String valor) {
        return valor == null ? "" : valor;
    }

    public Usuario getUsuarioPrestamo() {
        String dni = txtID.getText();

        if (dni == null || dni.isBlank()) {
            throw new IllegalArgumentException("Introduce un DNI para el préstamo.");
        }

        Usuario usuario = controlador.buscar(new Usuario(dni.trim().toUpperCase()));

        if (usuario == null) {
            throw new IllegalArgumentException("No existe un usuario con ese DNI.");
        }

        return usuario;
    }

    public Libro getLibroPrestamo() {
        String isbn = txtISBN.getText();

        if (isbn == null || isbn.isBlank()) {
            throw new IllegalArgumentException("Introduce un ISBN para el préstamo.");
        }

        Libro libro = controlador.buscar(new Libro(isbn.trim()));

        if (libro == null) {
            throw new IllegalArgumentException("No existe un libro con ese ISBN.");
        }

        return libro;
    }
}
