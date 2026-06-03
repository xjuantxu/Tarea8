package biblioteca.vista.controllers;

import biblioteca.controlador.Controlador;
import biblioteca.fichero.GestorBackup;
import biblioteca.modelo.negocio.Dialogos;
import biblioteca.modelo.negocio.recursos.LocalizadorRecursos;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.MenuItem;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;

// Controlador del menu principal.
public class MenuController {

    public MenuItem menuAcercaDe;
    public MenuItem menuCargarCopia;
    public MenuItem menuHacerCopia;
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

    public void onHacerCopia() {
        // Obtenemos la ventana actual para abrir el selector encima de ella.
        Stage stage = obtenerStageMenu();

        // Se usa FileChooser porque pide un archivo, aunque solo se utiliza la carpeta elegida.
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar ubicacion de la copia de seguridad");
        fileChooser.setInitialFileName("backup.xml"); //Nombre por defecto para poder elegir ubicación
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Archivos XML", "*.xml")
        );

        File ficheroSeleccionado = fileChooser.showSaveDialog(stage);

        // Si el usuario cancela, no se realiza ninguna copia.
        if (ficheroSeleccionado == null) {
            return;
        }

        // La copia crea varios XML, por eso se toma el directorio del archivo seleccionado.
        File directorio = ficheroSeleccionado.getParentFile();
        if (directorio == null) {
            Dialogos.mostrarDialogoError("Error", "No se ha podido obtener el directorio de la copia.");
            return;
        }

        // Antes de crear los ficheros se pide confirmacion.
        boolean confirmado = Dialogos.mostrarDialogoConfirmacion(
                "Confirmar copia",
                "Se guardara la copia de seguridad en:\n" + directorio.getAbsolutePath() + "\n\n¿Desea continuar?",
                stage
        );

        if (!confirmado) {
            return;
        }

        try {
            // Se configura el directorio y el gestor crea los XML de backup.
            GestorBackup.setDirectorio(directorio.getAbsolutePath());
            GestorBackup.hacerCopiaSeguridad();

            Dialogos.mostrarDialogoInformacion(
                    "Copia realizada",
                    "La copia de seguridad se ha creado correctamente."
            );

        } catch (Exception e) {
            // Si hay cualquier error al escribir los XML, se avisa al usuario.
            Dialogos.mostrarDialogoError(
                    "Error",
                    "No se pudo realizar la copia de seguridad: " + e.getMessage()
            );
        }
    }

    public void OnCargarCopia() {
        // Obtenemos la ventana actual para abrir el selector encima de ella.
        Stage stage = obtenerStageMenu();

        // Se elige uno de los XML de la copia y despues se usa su carpeta.
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar copia de seguridad");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Archivos XML", "*.xml")
        );

        File ficheroSeleccionado = fileChooser.showOpenDialog(stage);

        // Si el usuario cancela, no se carga nada.
        if (ficheroSeleccionado == null) {
            return;
        }

        // La copia esta formada por varios XML dentro del mismo directorio.
        File directorio = ficheroSeleccionado.getParentFile();
        if (directorio == null) {
            Dialogos.mostrarDialogoError("Error", "No se ha podido obtener el directorio de la copia.");
            return;
        }

        try {
            // Se configura el directorio para que el gestor sepa donde buscar los XML.
            GestorBackup.setDirectorio(directorio.getAbsolutePath());

            if (!GestorBackup.existenFicherosBackup()) {
                Dialogos.mostrarDialogoError(
                        "Error",
                        "La carpeta seleccionada no contiene todos los ficheros de backup necesarios."
                );
                return;
            }

            // Antes de restaurar se avisa de que se sustituiran los datos actuales.
            boolean confirmado = Dialogos.mostrarDialogoConfirmacion(
                    "Confirmar restauracion",
                    "Se cargara la copia de seguridad desde:\n"
                            + directorio.getAbsolutePath()
                            + "\n\nLos datos actuales se borraran antes de restaurar la copia.\n¿Desea continuar?",
                    stage
            );

            if (!confirmado) {
                return;
            }

            GestorBackup.cargarCopiaSeguridad();

            Dialogos.mostrarDialogoInformacion(
                    "Copia cargada",
                    "La copia de seguridad se ha cargado correctamente."
            );

        } catch (Exception e) {
            // Si hay cualquier error al leer los XML, se avisa al usuario.
            Dialogos.mostrarDialogoError(
                    "Error",
                    "No se pudo cargar la copia de seguridad: " + e.getMessage()
            );
        }
    }

    private Stage obtenerStageMenu() {
        // Desde el MenuItem se puede acceder al popup del menu y a su ventana.
        if (menuHacerCopia != null && menuHacerCopia.getParentPopup() != null) {
            Window window = menuHacerCopia.getParentPopup().getOwnerWindow();
            if (window instanceof Stage stage) {
                return stage;
            }
        }

        if (menuCargarCopia != null && menuCargarCopia.getParentPopup() != null) {
            Window window = menuCargarCopia.getParentPopup().getOwnerWindow();
            if (window instanceof Stage stage) {
                return stage;
            }
        }

        // Si no se encuentra la ventana, los dialogos se abriran sin propietario.
        return null;
    }
}
