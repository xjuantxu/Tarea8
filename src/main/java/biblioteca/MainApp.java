package biblioteca;

import biblioteca.modelo.negocio.recursos.LocalizadorRecursos;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(LocalizadorRecursos.class.getResource("/biblioteca/conectar-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        stage.setTitle("GestBiblio v0.2");
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResource("/img/logo.png")).toExternalForm()));
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();

        // Fuerza que la ventana de conexion aparezca delante al abrir la aplicacion.
        Platform.runLater(() -> {
            stage.setAlwaysOnTop(true);
            stage.toFront();
            stage.requestFocus();
            stage.setAlwaysOnTop(false);
        });
    }
}
