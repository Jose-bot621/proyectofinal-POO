package pe.edu.upeu.sysventas.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import pe.edu.upeu.sysventas.components.Toast;
import pe.edu.upeu.sysventas.dto.ComboBoxOption;
import pe.edu.upeu.sysventas.dto.ItemTienda;
import pe.edu.upeu.sysventas.dto.SessionManager;
import pe.edu.upeu.sysventas.dto.comprobante.Comprobante;
import pe.edu.upeu.sysventas.enums.TipoDocumento;
import pe.edu.upeu.sysventas.exception.ModelNotFoundException;
import pe.edu.upeu.sysventas.model.Cliente;
import pe.edu.upeu.sysventas.model.Producto;
import pe.edu.upeu.sysventas.model.Venta;
import pe.edu.upeu.sysventas.model.VentaDetalle;
import pe.edu.upeu.sysventas.service.*;
import pe.edu.upeu.sysventas.utils.Navegador;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Pantalla del COMPRADOR: catálogo de prendas (tarjetas de colores), carrito y compra.
 * Al confirmar: valida stock, registra/recupera el cliente, guarda Venta + VentaDetalle
 * y descuenta el stock de cada producto.
 */
public class TiendaController {

    /** Usuario interno que figura como "vendedor" de las compras hechas por el comprador (ver datos_tienda.sql). */
    private static final long ID_USUARIO_TIENDA = 12L;

    private static final String[] PALETA = {
            "linear-gradient(to bottom right, #ff9a9e, #fad0c4)",
            "linear-gradient(to bottom right, #a18cd1, #fbc2eb)",
            "linear-gradient(to bottom right, #84fab0, #8fd3f4)",
            "linear-gradient(to bottom right, #ffecd2, #fcb69f)",
            "linear-gradient(to bottom right, #fddb92, #d1fdff)",
            "linear-gradient(to bottom right, #fbc2eb, #a6c1ee)"
    };

    private final IProductoService productoService;
    private final ICategoriaService categoriaService;
    private final IClienteService clienteService;
    private final IVentaService ventaService;
    private final IVentaDetalleService detalleService;
    private final IUsuarioService usuarioService;

    @FXML private BorderPane raiz;
    @FXML private FlowPane flowProductos;
    @FXML private ComboBox<ComboBoxOption> cbxCategoria;
    @FXML private TextField txtBuscar, txtDni, txtNombre;
    @FXML private ListView<ItemTienda> lvCarrito;
    @FXML private Label lblTotal;
    @FXML private Button btnComprar;

    private final ObservableList<ItemTienda> carrito = FXCollections.observableArrayList();

    public TiendaController(IProductoService productoService,
                            ICategoriaService categoriaService,
                            IClienteService clienteService,
                            IVentaService ventaService,
                            IVentaDetalleService detalleService,
                            IUsuarioService usuarioService) {
        this.productoService = productoService;
        this.categoriaService = categoriaService;
        this.clienteService = clienteService;
        this.ventaService = ventaService;
        this.detalleService = detalleService;
        this.usuarioService = usuarioService;
    }

    // ------------------------------------------------------------------
    // INICIO
    // ------------------------------------------------------------------
    @FXML
    public void initialize() {
        carrito.clear();
        lvCarrito.setItems(carrito);
        txtDni.clear();
        txtNombre.clear();
        // El DNI solo admite hasta 8 dígitos
        txtDni.textProperty().addListener((obs, anterior, nuevo) -> {
            if (!nuevo.matches("\\d{0,8}")) {
                txtDni.setText(anterior);
            }
        });

        cbxCategoria.getItems().clear();
        cbxCategoria.getItems().add(new ComboBoxOption("0", "Todas las categorías"));
        cbxCategoria.getItems().addAll(categoriaService.lisCategoria());
        cbxCategoria.getSelectionModel().selectFirst();
        cbxCategoria.setOnAction(e -> cargarCatalogo());
        txtBuscar.textProperty().addListener((obs, anterior, nuevo) -> cargarCatalogo());

        cargarCatalogo();
        actualizarResumen();
    }

    // ------------------------------------------------------------------
    // CATÁLOGO
    // ------------------------------------------------------------------
    private void cargarCatalogo() {
        flowProductos.getChildren().clear();
        String filtro = txtBuscar.getText() == null ? "" : txtBuscar.getText().trim().toLowerCase();
        ComboBoxOption categoria = cbxCategoria.getSelectionModel().getSelectedItem();
        String idCategoria = categoria == null ? "0" : categoria.getKey();

        int mostrados = 0;
        for (Producto p : productoService.findAll()) {
            if (!"0".equals(idCategoria)
                    && !idCategoria.equals(String.valueOf(p.getIdCategoria().getIdCategoria()))) {
                continue;
            }
            if (!filtro.isEmpty() && !p.getNombre().toLowerCase().contains(filtro)) {
                continue;
            }
            flowProductos.getChildren().add(crearTarjeta(p));
            mostrados++;
        }
        if (mostrados == 0) {
            Label vacio = new Label("😕 No hay prendas para mostrar");
            vacio.getStyleClass().add("mensaje-vacio");
            flowProductos.getChildren().add(vacio);
        }
    }

    private VBox crearTarjeta(Producto p) {
        int stock = (int) Math.floor(p.getStock() == null ? 0 : p.getStock());

        Label emoji = new Label(emojiDe(p));
        emoji.getStyleClass().add("tarjeta-emoji");
        StackPane cabecera = new StackPane(emoji);
        cabecera.setPrefHeight(110);
        cabecera.setStyle("-fx-background-color: " + colorDe(p) + "; -fx-background-radius: 16 16 0 0;");

        Label nombre = new Label(p.getNombre());
        nombre.getStyleClass().add("tarjeta-nombre");
        nombre.setWrapText(true);

        Label categoriaMarca = new Label(p.getIdCategoria().getNombre() + "  ·  " + p.getIdMarca().getNombre());
        categoriaMarca.getStyleClass().add("tarjeta-detalle");

        Label detalle = new Label("Talla: " + valor(p.getTalla()) + "   |   Color: " + valor(p.getColor()));
        detalle.getStyleClass().add("tarjeta-chip");

        Label precio = new Label(String.format(Locale.US, "S/ %.2f", p.getPu()));
        precio.getStyleClass().add("tarjeta-precio");

        Label lblStock = new Label();
        VBox cuerpo = new VBox(6, nombre, categoriaMarca, detalle, precio, lblStock);
        cuerpo.setPadding(new Insets(12));
        cuerpo.setAlignment(Pos.CENTER);

        if (stock <= 0) {
            lblStock.setText("⛔ Agotado");
            lblStock.getStyleClass().add("stock-agotado");
        } else {
            lblStock.setText(stock <= 5 ? "🔥 ¡Últimas " + stock + " unidades!" : "✅ Disponible: " + stock);
            lblStock.getStyleClass().add(stock <= 5 ? "stock-poco" : "stock-ok");

            Spinner<Integer> spCantidad = new Spinner<>(1, stock, 1);
            spCantidad.setPrefWidth(85);
            Button btnAgregar = new Button("🛒 Agregar");
            btnAgregar.getStyleClass().add("btn-agregar");
            btnAgregar.setOnAction(e -> agregarAlCarrito(p, spCantidad.getValue()));
            HBox acciones = new HBox(8, spCantidad, btnAgregar);
            acciones.setAlignment(Pos.CENTER);
            cuerpo.getChildren().add(acciones);
        }

        VBox tarjeta = new VBox(cabecera, cuerpo);
        tarjeta.setPrefWidth(235);
        tarjeta.getStyleClass().add("tarjeta-producto");
        return tarjeta;
    }

    private String emojiDe(Producto p) {
        String c = p.getIdCategoria().getNombre().toLowerCase();
        if (c.contains("polo") || c.contains("camis")) return "👕";
        if (c.contains("pantal") || c.contains("jean")) return "👖";
        if (c.contains("casaca") || c.contains("abrigo")) return "🧥";
        if (c.contains("vestido") || c.contains("falda")) return "👗";
        if (c.contains("zapat") || c.contains("calzado")) return "👟";
        return "🛍️";
    }

    private String colorDe(Producto p) {
        return PALETA[(int) (p.getIdCategoria().getIdCategoria() % PALETA.length)];
    }

    private String valor(String texto) {
        return texto == null || texto.isBlank() ? "-" : texto;
    }

    // ------------------------------------------------------------------
    // CARRITO
    // ------------------------------------------------------------------
    private void agregarAlCarrito(Producto p, int cantidad) {
        ItemTienda existente = null;
        for (ItemTienda it : carrito) {
            if (it.getProducto().getIdProducto().equals(p.getIdProducto())) {
                existente = it;
                break;
            }
        }
        int stock = (int) Math.floor(p.getStock());
        int nuevaCantidad = (existente == null ? 0 : existente.getCantidad()) + cantidad;
        if (nuevaCantidad > stock) {
            mensaje("Solo hay " + stock + " unidades de " + p.getNombre(), "#ffb3ba");
            return;
        }
        if (existente == null) {
            carrito.add(new ItemTienda(p, cantidad));
        } else {
            existente.setCantidad(nuevaCantidad);
            lvCarrito.refresh();
        }
        actualizarResumen();
        mensaje("🛒 " + p.getNombre() + " agregado al carrito", "#baffc9");
    }

    @FXML
    public void quitarSeleccionado() {
        ItemTienda seleccionado = lvCarrito.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            carrito.remove(seleccionado);
            actualizarResumen();
        }
    }

    @FXML
    public void vaciarCarrito() {
        carrito.clear();
        actualizarResumen();
    }

    private void actualizarResumen() {
        double total = 0;
        for (ItemTienda it : carrito) {
            total += it.getSubtotal();
        }
        lblTotal.setText(String.format(Locale.US, "Total: S/ %.2f", total));
        btnComprar.setDisable(carrito.isEmpty());
    }

    // ------------------------------------------------------------------
    // COMPRA
    // ------------------------------------------------------------------
    @FXML
    public void confirmarCompra() {
        if (carrito.isEmpty()) {
            mensaje("Tu carrito está vacío", "#ffb3ba");
            return;
        }
        String dni = txtDni.getText().trim();
        String nombre = txtNombre.getText().trim();
        if (!dni.matches("\\d{8}")) {
            mensaje("Ingresa un DNI válido de 8 dígitos", "#ffb3ba");
            txtDni.requestFocus();
            return;
        }
        if (nombre.length() < 3) {
            mensaje("Ingresa tus nombres y apellidos", "#ffb3ba");
            txtNombre.requestFocus();
            return;
        }

        try {
            // 1) Revalidar stock con los datos actuales de la base de datos
            List<Producto> actuales = new ArrayList<>();
            for (ItemTienda it : carrito) {
                Producto actual = productoService.findById(it.getProducto().getIdProducto());
                if (actual.getStock() < it.getCantidad()) {
                    mensaje("Stock insuficiente de " + actual.getNombre()
                            + " (quedan " + (int) Math.floor(actual.getStock()) + ")", "#ffb3ba");
                    cargarCatalogo();
                    return;
                }
                actuales.add(actual);
            }

            // 2) Cliente (se crea si no existe)
            Cliente cliente = obtenerOCrearCliente(dni, nombre);

            // 3) Cabecera de la venta
            double total = 0;
            for (ItemTienda it : carrito) {
                total += it.getSubtotal();
            }
            total = redondear(total);
            double base = redondear(total / 1.18);
            double igv = redondear(total - base);

            Venta venta = Venta.builder()
                    .dniruc(cliente)
                    .idUsuario(usuarioService.findById(ID_USUARIO_TIENDA))
                    .precioBase(base)
                    .igv(igv)
                    .precioTotal(total)
                    .serie("B001")
                    .tipoDoc("Boleta")
                    .numDoc(String.format("%08d", System.currentTimeMillis() % 100000000L))
                    .fechaGener(LocalDate.now())
                    .build();
            Venta guardada = ventaService.save(venta);

            // 4) Detalle + descuento de stock
            List<VentaDetalle> detalles = new ArrayList<>();
            for (int i = 0; i < carrito.size(); i++) {
                ItemTienda it = carrito.get(i);
                Producto actual = actuales.get(i);
                VentaDetalle vd = VentaDetalle.builder()
                        .idVenta(guardada)
                        .idProducto(actual)
                        .cantidad((double) it.getCantidad())
                        .pu(actual.getPu())
                        .descuento(0.0)
                        .subtotal(redondear(actual.getPu() * it.getCantidad()))
                        .build();
                detalles.add(detalleService.save(vd));

                actual.setStock(actual.getStock() - it.getCantidad());
                productoService.update(actual.getIdProducto(), actual);
            }
            guardada.setDetalleVenta(detalles);

            // 5) Limpiar y refrescar
            carrito.clear();
            txtDni.clear();
            txtNombre.clear();
            actualizarResumen();
            cargarCatalogo();
            mensaje("🎉 ¡Compra realizada con éxito!", "#baffc9");

            mostrarComprobante(guardada);
        } catch (Exception ex) {
            ex.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error en la compra");
            alert.setHeaderText("No se pudo completar la compra");
            alert.setContentText(ex.getMessage());
            alert.showAndWait();
        }
    }

    private Cliente obtenerOCrearCliente(String dni, String nombre) {
        try {
            return clienteService.findById(dni);
        } catch (ModelNotFoundException e) {
            return clienteService.save(Cliente.builder()
                    .dniruc(dni)
                    .nombres(nombre)
                    .repLegal(nombre)
                    .direccion("Compra en tienda")
                    .tipoDocumento(TipoDocumento.DNI)
                    .build());
        }
    }

    private double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }

    /** Muestra la boleta con el mismo visor que usa el módulo de ventas. */
    private void mostrarComprobante(Venta venta) {
        try {
            Comprobante comprobante = ventaService.generarComprobante(venta);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/main_comprobante.fxml"));
            Parent visor = loader.load();
            MainComprobanteController controlador = loader.getController();
            controlador.pasarDatos(comprobante);

            if (visor instanceof Region) {
                ((Region) visor).setPrefSize(800, 600);
            }
            Alert alert = new Alert(Alert.AlertType.NONE);
            alert.setTitle("Tu boleta de compra");
            alert.setHeaderText(null);
            alert.getDialogPane().setContent(new StackPane(visor));
            alert.getButtonTypes().add(new ButtonType("Cerrar", ButtonBar.ButtonData.CANCEL_CLOSE));
            alert.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Compra registrada");
            alert.setHeaderText("¡Gracias por tu compra!");
            alert.setContentText(String.format(Locale.US, "Boleta %s-%s  |  Total: S/ %.2f",
                    venta.getSerie(), venta.getNumDoc(), venta.getPrecioTotal()));
            alert.showAndWait();
        }
    }

    // ------------------------------------------------------------------
    // NAVEGACIÓN / UTILIDADES
    // ------------------------------------------------------------------
    @FXML
    public void volverARoles(ActionEvent e) {
        SessionManager.getInstance().setRolIngreso(null);
        Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
        Navegador.cambiarVista(stage, "/view/seleccion_rol.fxml", "Moda Store", false);
    }

    private void mensaje(String texto, String colorFondo) {
        if (raiz.getScene() == null || raiz.getScene().getWindow() == null) {
            return;
        }
        Stage stage = (Stage) raiz.getScene().getWindow();
        Toast.showToast(stage, texto, colorFondo, 2200,
                stage.getX() + stage.getWidth() / 2 - 150, stage.getY() + 90);
    }
}
