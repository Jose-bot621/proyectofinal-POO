package pe.edu.upeu.sysventas.utils;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import pe.edu.upeu.sysventas.components.StageManager;
import pe.edu.upeu.sysventas.config.AppContext;

import java.io.IOException;

/**
 * Cambia la pantalla de la ventana principal (Stage) cargando un FXML.
 * Todas las pantallas nuevas (rol, login, tienda) pasan por aquí.
 */
public final class Navegador {

    private Navegador() {
    }

    public static void cambiarVista(Stage stage, String fxml, String titulo, boolean ocuparPantalla) {
        try {
            FXMLLoader loader = new FXMLLoader(Navegador.class.getResource(fxml));
            loader.setControllerFactory(AppContext.getInstance()::getBean);
            Parent root = loader.load();

            Scene scene = new Scene(root);
            scene.getStylesheets().add(Navegador.class.getResource("/css/style.css").toExternalForm());

            stage.setMaximized(false);
            stage.setResizable(true);
            stage.setTitle(titulo);
            stage.setScene(scene);

            if (ocuparPantalla) {
                Rectangle2D r = Screen.getPrimary().getVisualBounds();
                stage.setX(r.getMinX());
                stage.setY(r.getMinY());
                stage.setWidth(r.getWidth());
                stage.setHeight(r.getHeight());
            } else {
                stage.sizeToScene();
                stage.centerOnScreen();
            }
            StageManager.setPrimaryStage(stage);
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException("No se pudo abrir la vista " + fxml, e);
        }
    }
}
