package biblioteca.vista.controllers;

import biblioteca.controlador.Controlador;
import biblioteca.modelo.Modelo;
import biblioteca.modelo.negocio.Dialogos;
import biblioteca.modelo.negocio.recursos.LocalizadorRecursos;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;

// Controlador de la pantalla de conexion a la base de datos.
public class ConexionController {

    private Controlador controlador;

    @FXML
    public void initialize() {
        // Se prepara el controlador principal antes de conectar.
        Modelo modelo = new Modelo();
        controlador = new Controlador(modelo);
    }

    @FXML
    protected void onConectarButtonClick(ActionEvent event) {
        try {
            // Si la conexion funciona, pasamos al menu principal.
            controlador.comenzar();
            Dialogos.mostrarDialogoInformacion("Información", "Conexión creada correctamente.");

            FXMLLoader loader = new FXMLLoader(LocalizadorRecursos.class.getResource("/biblioteca/menu-view.fxml"));

            Scene scene = new Scene(loader.load());

            // Obtenemos el controlador del menu para pasarle la conexion ya creada.
            Object controller = loader.getController();
            if (controller instanceof MenuController mc) {
                mc.setControlador(controlador);
            }

            // Cogemos la ventana actual y cambiamos su escena por la del menu.
            Stage stage = (Stage) ((Node) event.getSource())
                    .getScene()
                    .getWindow();

            stage.setScene(scene);

        } catch (Exception e) {
            Dialogos.mostrarDialogoError("Error.", e.getMessage());
        }
    }
}
