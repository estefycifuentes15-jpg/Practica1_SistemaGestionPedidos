package cafeteria;

import java.io.File;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class GestorArchivos {

    private File carpeta;
    private File archivoProductos;
    private File archivoVentas;

    public GestorArchivos() {
        carpeta = new File("datos");
        carpeta.mkdirs();

        archivoProductos = new File(carpeta, "productos.txt");
        archivoVentas = new File(carpeta, "ventas.txt");
    }

    public ArrayList<Producto> cargarProductos() {
        ArrayList<Producto> productos = new ArrayList<Producto>();

        if (!archivoProductos.exists()) {
            return productos;
        }

        try (BufferedReader lector =
                new BufferedReader(new FileReader(archivoProductos))) {

            String linea;

            while ((linea = lector.readLine()) != null) {
                String[] datos = linea.split("\\|", -1);

                if (datos.length == 4) {
                    try {
                        int codigo = Integer.parseInt(datos[0]);
                        String nombre = datos[1];
                        double precio = Double.parseDouble(datos[2]);
                        int stock = Integer.parseInt(datos[3]);

                        productos.add(
                            new Producto(codigo, nombre, precio, stock)
                        );
                    } catch (NumberFormatException e) {
                        System.out.println(
                            "Se omitió un registro de producto incorrecto."
                        );
                    }
                }
            }

        } catch (IOException e) {
            System.out.println("Error al leer productos: " + e.getMessage());
        }

        return productos;
    }

    public void guardarProductos(ArrayList<Producto> productos) {
        try (FileWriter escritor =
                new FileWriter(archivoProductos, false)) {

            for (Producto producto : productos) {
                escritor.write(
                    producto.getCodigo() + "|"
                    + producto.getNombre() + "|"
                    + producto.getPrecio() + "|"
                    + producto.getStock() + "\n"
                );
            }

        } catch (IOException e) {
            System.out.println("Error al guardar productos: "
                    + e.getMessage());
        }
    }

    public void registrarVenta(Pedido pedido) {
        try (FileWriter escritor =
                new FileWriter(archivoVentas, true)) {

            for (DetallePedido detalle : pedido.getDetalles()) {
                escritor.write(
                    pedido.getFecha() + "|"
                    + pedido.getCodigo() + "|"
                    + detalle.getProducto().getCodigo() + "|"
                    + detalle.getProducto().getNombre() + "|"
                    + detalle.getCantidad() + "|"
                    + detalle.getPrecioUnitario() + "|"
                    + detalle.calcularSubtotal() + "\n"
                );
            }

        } catch (IOException e) {
            System.out.println("Error al registrar la venta: "
                    + e.getMessage());
        }
    }

    public void mostrarReporteVentas() {
        if (!archivoVentas.exists()) {
            System.out.println("Todavía no hay ventas registradas.");
            return;
        }

        double totalVentas = 0;
        int cantidadLineas = 0;

        Set<String> pedidos = new HashSet<String>();
        Map<String, Integer> cantidades =
                new HashMap<String, Integer>();

        try (BufferedReader lector =
                new BufferedReader(new FileReader(archivoVentas))) {

            String linea;

            while ((linea = lector.readLine()) != null) {
                String[] datos = linea.split("\\|", -1);

                if (datos.length == 7) {
                    try {
                        String codigoPedido = datos[1];
                        String nombreProducto = datos[3];
                        int cantidad = Integer.parseInt(datos[4]);
                        double subtotal = Double.parseDouble(datos[6]);

                        pedidos.add(codigoPedido);
                        totalVentas += subtotal;
                        cantidadLineas++;

                        int anterior = cantidades.containsKey(nombreProducto)
                                ? cantidades.get(nombreProducto) : 0;

                        cantidades.put(nombreProducto, anterior + cantidad);

                    } catch (NumberFormatException e) {
                        System.out.println(
                            "Se omitió una línea de venta incorrecta."
                        );
                    }
                }
            }

        } catch (IOException e) {
            System.out.println("Error al leer ventas: " + e.getMessage());
            return;
        }

        System.out.println("\n===== REPORTE DE VENTAS =====");
        System.out.println("Pedidos registrados: " + pedidos.size());
        System.out.println("Líneas de venta: " + cantidadLineas);
        System.out.printf("Total vendido: $%.2f%n", totalVentas);

        System.out.println("\nProductos vendidos:");

        if (cantidades.isEmpty()) {
            System.out.println("No hay productos vendidos.");
        } else {
            for (Map.Entry<String, Integer> entrada
                    : cantidades.entrySet()) {
                System.out.println(
                    entrada.getKey() + ": "
                    + entrada.getValue() + " unidades"
                );
            }
        }
    }
}