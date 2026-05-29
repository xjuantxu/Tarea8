package biblioteca.vista.controllers;

import biblioteca.modelo.dominio.Direccion;
import biblioteca.modelo.dominio.Usuario;
import javafx.scene.control.TextField;

public class UsuarioController {
    public TextField txtID;
    public TextField txtNombre;
    public TextField txtEmail;
    public TextField txtLocalidad;
    public TextField txtCP;
    public TextField txtVia;
    public TextField txtNumero;

    public void setUsuario(Usuario usuario) {
        if (usuario == null) {
            return;
        }

        txtID.setText(usuario.getDni());
        txtID.setDisable(true);
        txtNombre.setText(usuario.getNombre());
        txtEmail.setText(usuario.getEmail());

        Direccion direccion = usuario.getDireccion();
        if (direccion != null) {
            txtVia.setText(direccion.getVia());
            txtNumero.setText(direccion.getNumero());
            txtCP.setText(direccion.getCp());
            txtLocalidad.setText(direccion.getLocalidad());
        }
    }

    public Usuario getUsuario() {
        Usuario usuario = new Usuario(
                txtID.getText(),
                txtNombre.getText()
        );

        usuario.setEmail(txtEmail.getText());
        usuario.setDireccion(new Direccion(
                txtVia.getText(),
                txtNumero.getText(),
                txtCP.getText(),
                txtLocalidad.getText()
        ));

        return usuario;
    }
}
