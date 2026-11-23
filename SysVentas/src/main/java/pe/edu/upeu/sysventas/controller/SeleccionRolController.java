package pe.edu.upeu.sysventas.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.stage.Stage;
import pe.edu.upeu.sysventas.dto.SessionManager;
import pe.edu.upeu.sysventas.enums.RolIngreso;
import pe.edu.upeu.sysventas.utils.Navegador;

/** Ventana inicial: la persona elige si entra como COMPRADOR o como VENDEDOR. */
public class SeleccionRolController {

    @FXML
    public void ingresarComoComprador(ActionEvent e) {
        SessionManager.getInstance().setRolIngreso(RolIngreso.COMPRADOR);
        // El comprador no necesita login: va directo al catálogo.
        Navegador.cambiarVista(ventana(e), "/view/main_tienda.fxml", "Moda Store - Comprador", true);
    }

    @FXML
    public void ingresarComoVendedor(ActionEvent e) {
        SessionManager.getInstance().setRolIngreso(RolIngreso.VENDEDOR);
        // El vendedor debe autenticarse con usuario y clave.
        Navegador.cambiarVista(ventana(e), "/view/login.fxml", "Moda Store - Acceso Vendedor", false);
    }

    @FXML
    public void salir() {
        Platform.exit(); // SysVentas.stop() cierra el pool de la base de datos
    }

    private Stage ventana(ActionEvent e) {
        return (Stage) ((Node) e.getSource()).getScene().getWindow();
    }
}
