package biblioteca;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApp.class.getResource("conectar-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        stage.setTitle("GestBiblio v0.1");
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResource("/img/logo.png")).toExternalForm()));
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }
}
