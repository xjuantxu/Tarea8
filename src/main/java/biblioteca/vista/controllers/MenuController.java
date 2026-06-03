package biblioteca.vista.controllers;

import biblioteca.controlador.Controlador;
import biblioteca.modelo.negocio.Dialogos;
import biblioteca.modelo.negocio.recursos.LocalizadorRecursos;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;

import java.io.IOException;

// Controlador del menu principal.
public class MenuController {

    public MenuItem menuAcercaDe;
    private Controlador controlador;

    public void setControlador(Controlador controlador) {
        // Recibe el controlador principal para usar la misma conexion.
        this.controlador = controlador;
    }

    private void cambiarVista(ActionEvent event, String fxml) {
        try {
            // Carga la vista que se indique desde el menu.
            FXMLLoader loader = new FXMLLoader(LocalizadorRecursos.class.getResource(fxml));

            Scene scene = new Scene(loader.load());

            // Se pasa el controlador principal a la pantalla que se abre.
            Object controller = loader.getController();

            if (controller instanceof LibrosController lc && controlador != null) {
                lc.setControlador(controlador);
            } else if (controller instanceof UsuariosController uc && controlador != null) {
                uc.setControlador(controlador);
            } else if (controller instanceof PrestamosController pc && controlador != null) {
                pc.setControlador(controlador);
            }

            // Cogemos la ventana actual y cambiamos su escena.
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource())
                    .getScene()
                    .getWindow();

            stage.setScene(scene);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onLibrosClick(ActionEvent event) {
        // Abre la pantalla de libros.
        cambiarVista(event, "/biblioteca/libros-view.fxml");
    }

    public void onUsuariosClick(ActionEvent event) {
        // Abre la pantalla de usuarios.
        cambiarVista(event, "/biblioteca/usuarios-view.fxml");
    }

    public void onPrestamosClick(ActionEvent event) {
        // Abre la pantalla de prestamos.
        cambiarVista(event, "/biblioteca/prestamos-view.fxml");
    }

    public void onSalirClick(ActionEvent event) {
        // Se obtiene la ventana para poder cerrarla despues.
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource())
                .getScene()
                .getWindow();

        // Antes de salir se pide confirmacion.
        boolean confirmarSalida = Dialogos.mostrarDialogoConfirmacion("Confirmar salida", "¿Desea salir de la aplicación?", stage);

        if (!confirmarSalida) {
            return;
        }

        try {
            // Si hay conexion abierta, se cierra.
            if (controlador != null) {
                controlador.terminar();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Dialogos.mostrarDialogoInformacion("Información", "Conexión cerrada. Cerrando aplicación.");
        stage.close();
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
