package biblioteca.vista.controllers;

import biblioteca.controlador.Controlador;
import biblioteca.modelo.negocio.Dialogos;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;

import java.io.IOException;

public class MenuController {

    public MenuItem menuAcercaDe;
    private Controlador controlador;

    public void setControlador(Controlador controlador) {
        this.controlador = controlador;
    }

    private void cambiarVista(ActionEvent event, String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/biblioteca/" + fxml)
            );

            Scene scene = new Scene(loader.load());

            Object controller = loader.getController();

            if (controller instanceof LibrosController lc && controlador != null) {
                lc.setControlador(controlador);
            } else if (controller instanceof UsuariosController uc && controlador != null) {
                uc.setControlador(controlador);
            } else if (controller instanceof PrestamosController pc && controlador != null) {
                pc.setControlador(controlador);
            }

            Stage stage = (Stage) ((javafx.scene.Node) event.getSource())
                    .getScene()
                    .getWindow();

            stage.setScene(scene);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onLibrosClick(ActionEvent event) {
        cambiarVista(event, "libros-view.fxml");
    }

    public void onUsuariosClick(ActionEvent event) {
        cambiarVista(event, "usuarios-view.fxml");
    }

    public void onPrestamosClick(ActionEvent event) {
        cambiarVista(event, "prestamos-view.fxml");
    }

    public void onSalirClick(ActionEvent event) {
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource())
                .getScene()
                .getWindow();

        boolean confirmarSalida = Dialogos.mostrarDialogoConfirmacion("Confirmar salida", "¿Desea salir de la aplicación?", stage);

        if (!confirmarSalida) {
            return;
        }

        try {
            if (controlador != null) {
                controlador.terminar();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Dialogos.mostrarDialogoInformacion("Información", "Conexión cerrada. Cerrando aplicación.");
        stage.close();
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
