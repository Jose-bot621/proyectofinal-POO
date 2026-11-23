package pe.edu.upeu.sysventas.dto;

import pe.edu.upeu.sysventas.model.Producto;

import java.util.Locale;

/** Línea del carrito del comprador (producto + cantidad). Vive solo en memoria. */
public class ItemTienda {
    private final Producto producto;
    private int cantidad;

    public ItemTienda(Producto producto, int cantidad) {
        this.producto = producto;
        this.cantidad = cantidad;
    }

    public Producto getProducto() { return producto; }

    public int getCantidad() { return cantidad; }

    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public double getSubtotal() { return producto.getPu() * cantidad; }

    @Override
    public String toString() {
        return String.format(Locale.US, "%s (%s, %s)  x%d  =  S/ %.2f",
                producto.getNombre(),
                producto.getTalla() == null ? "-" : producto.getTalla(),
                producto.getColor() == null ? "-" : producto.getColor(),
                cantidad, getSubtotal());
    }
}
