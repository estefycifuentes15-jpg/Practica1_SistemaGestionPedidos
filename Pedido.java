package cafeteria;

import java.util.ArrayList;

public class Pedido {

    private int codigo;
    private String fecha;
    private ArrayList<DetallePedido> detalles;

    public Pedido(int codigo, String fecha) {
        this.codigo = codigo;
        this.fecha = fecha;
        this.detalles = new ArrayList<DetallePedido>();
    }

    public int getCodigo() {
        return codigo;
    }

    public String getFecha() {
        return fecha;
    }

    public ArrayList<DetallePedido> getDetalles() {
        return detalles;
    }

    public void agregarDetalle(DetallePedido detalle) {
        detalles.add(detalle);
    }

    public double calcularTotal() {
        double total = 0;

        for (DetallePedido detalle : detalles) {
            total += detalle.calcularSubtotal();
        }

        return total;
    }
}