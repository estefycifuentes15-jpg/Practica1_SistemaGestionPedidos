package cafeteria;

import java.util.ArrayList;
import java.util.Scanner;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Main {

    private static Scanner entrada = new Scanner(System.in);
    private static GestorArchivos gestor = new GestorArchivos();
    private static ArrayList<Producto> productos =
            gestor.cargarProductos();

    public static void main(String[] args) {

        int opcion;

        do {
            System.out.println("\n..... EL BUEN SABOR .....");
            System.out.println("1. Registrar producto");
            System.out.println("2. Listar productos");
            System.out.println("3. Reponer stock");
            System.out.println("4. Registrar pedido");
            System.out.println("5. Reporte de ventas");
            System.out.println("6. Guardar y salir");
            System.out.print("Seleccione una opción: ");

            opcion = leerEntero();

            switch (opcion) {
                case 1:
                    registrarProducto();
                    break;

                case 2:
                    listarProductos();
                    break;

                case 3:
                    reponerStock();
                    break;

                case 4:
                    registrarPedido();
                    break;

                case 5:
                    gestor.mostrarReporteVentas();
                    break;

                case 6:
                    gestor.guardarProductos(productos);
                    System.out.println("Datos guardados. Hasta pronto.");
                    break;

                default:
                    System.out.println("Opción incorrecta.");
            }

        } while (opcion != 6);

        entrada.close();
    }

    private static int leerEntero() {
        while (true) {
            try {
                return Integer.parseInt(entrada.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.print("Ingrese un número entero válido: ");
            }
        }
    }

    private static double leerDouble() {
        while (true) {
            try {
                double numero = Double.parseDouble(
                    entrada.nextLine().trim().replace(',', '.')
                );

                if (Double.isFinite(numero)) {
                    return numero;
                }
            } catch (NumberFormatException e) {
                // Se vuelve a solicitar el valor.
            }

            System.out.print("Ingrese un número válido: ");
        }
    }

    private static Producto buscarProducto(int codigo) {
        for (Producto producto : productos) {
            if (producto.getCodigo() == codigo) {
                return producto;
            }
        }
        return null;
    }

    private static void registrarProducto() {
        System.out.println("\n--- REGISTRAR PRODUCTO ---");

        int codigo = 1;

        for (Producto producto : productos) {
            if (producto.getCodigo() >= codigo) {
                codigo = producto.getCodigo() + 1;
            }
        }

        System.out.println("Código asignado: " + codigo);
        System.out.print("Nombre del producto: ");
        String nombre = entrada.nextLine().trim();

        if (nombre.isEmpty() || nombre.contains("|")) {
            System.out.println("Nombre vacío o con símbolo | no permitido.");
            return;
        }

        for (Producto producto : productos) {
            if (producto.getNombre().equalsIgnoreCase(nombre)) {
                System.out.println("Ese producto ya está registrado.");
                return;
            }
        }

        System.out.print("Precio: $");
        double precio = leerDouble();

        if (precio <= 0) {
            System.out.println("El precio debe ser mayor que cero.");
            return;
        }

        System.out.print("Stock inicial: ");
        int stock = leerEntero();

        if (stock < 0) {
            System.out.println("El stock no puede ser negativo.");
            return;
        }

        productos.add(new Producto(codigo, nombre, precio, stock));
        gestor.guardarProductos(productos);

        System.out.println("Producto registrado correctamente.");
    }

    private static void listarProductos() {
        System.out.println("\n--- CATÁLOGO DE PRODUCTOS ---");

        if (productos.isEmpty()) {
            System.out.println("No hay productos registrados.");
            return;
        }

        for (Producto producto : productos) {
            System.out.println(producto);
        }
    }

    private static void reponerStock() {
        listarProductos();

        if (productos.isEmpty()) {
            return;
        }

        System.out.print("Código del producto: ");
        int codigo = leerEntero();

        Producto producto = buscarProducto(codigo);

        if (producto == null) {
            System.out.println("Producto no encontrado.");
            return;
        }

        System.out.print("Cantidad que desea reponer: ");
        int cantidad = leerEntero();

        if (cantidad <= 0) {
            System.out.println("La cantidad debe ser mayor que cero.");
            return;
        }

        producto.reponerStock(cantidad);
        gestor.guardarProductos(productos);

        System.out.println("Stock actualizado.");
        System.out.println(producto);
    }

    private static void registrarPedido() {
        if (productos.isEmpty()) {
            System.out.println("Primero registre productos.");
            return;
        }

        int codigoPedido = (int)
                (System.currentTimeMillis() % Integer.MAX_VALUE);

        String fecha = LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        );

        Pedido pedido = new Pedido(codigoPedido, fecha);

        System.out.println("\n--- REGISTRAR PEDIDO ---");
        listarProductos();

        System.out.println(
            "Ingrese el código 0 cuando termine de agregar productos."
        );

        while (true) {
            System.out.print("Código del producto (0 para terminar): ");
            int codigo = leerEntero();

            if (codigo == 0) {
                break;
            }

            Producto producto = buscarProducto(codigo);

            if (producto == null) {
                System.out.println("Producto no encontrado.");
                continue;
            }

            if (producto.getStock() <= 0) {
                System.out.println("Producto agotado.");
                continue;
            }

            System.out.print("Cantidad: ");
            int cantidad = leerEntero();

            if (cantidad <= 0) {
                System.out.println("Cantidad no válida.");
                continue;
            }

            if (cantidad > producto.getStock()) {
                System.out.println(
                    "Stock insuficiente. Disponible: "
                    + producto.getStock()
                );
                continue;
            }

            DetallePedido detalle =
                    new DetallePedido(producto, cantidad);

            producto.vender(cantidad);
            pedido.agregarDetalle(detalle);

            System.out.println("Producto agregado al pedido.");
        }

        if (pedido.getDetalles().isEmpty()) {
            System.out.println("Pedido cancelado: no hay productos.");
            return;
        }

        gestor.registrarVenta(pedido);
        gestor.guardarProductos(productos);

        System.out.println("\n===== FACTURA =====");
        System.out.println("Pedido: " + pedido.getCodigo());
        System.out.println("Fecha: " + pedido.getFecha());

        for (DetallePedido detalle : pedido.getDetalles()) {
            System.out.printf(
                "%s | %d x $%.2f = $%.2f%n",
                detalle.getProducto().getNombre(),
                detalle.getCantidad(),
                detalle.getPrecioUnitario(),
                detalle.calcularSubtotal()
            );
        }

        System.out.printf(
            "TOTAL A PAGAR: $%.2f%n",
            pedido.calcularTotal()
        );

        System.out.println("Pedido registrado.");
    }
}