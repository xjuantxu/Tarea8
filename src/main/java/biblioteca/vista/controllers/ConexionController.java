package biblioteca.vista.controllers;

import biblioteca.controlador.Controlador;
import biblioteca.modelo.Modelo;
import biblioteca.modelo.negocio.Dialogos;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.stage.Stage;
import java.io.IOException;
import javafx.scene.control.Alert;

public class ConexionController {

    private Controlador controlador;

    @FXML
    public void initialize() {
        Modelo modelo = new Modelo();
        controlador = new Controlador(modelo);
    }

    @FXML
    protected void onConectarButtonClick(ActionEvent event) {
        try {
            controlador.comenzar();
            Dialogos.mostrarDialogoInformacion("Información", "Conexión creada correctamente.");
            cambiarVista(event, "menu-view.fxml");

        } catch (Exception e) {
            Dialogos.mostrarDialogoError("Error.",e.getMessage());
        }
    }


    private void cambiarVista(ActionEvent event, String fxml) {

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/biblioteca/" + fxml)
            );

            Scene scene = new Scene(loader.load());

            Object controller = loader.getController();
            if (controller instanceof MenuController mc) {
                mc.setControlador(controlador);
            }

            Stage stage = (Stage) ((Node) event.getSource())
                    .getScene()
                    .getWindow();

            stage.setScene(scene);

        } catch (IOException e) {
            e.printStackTrace();
            Dialogos.mostrarDialogoError("Error.","No se pudo cargar la vista");
        }
    }
}
