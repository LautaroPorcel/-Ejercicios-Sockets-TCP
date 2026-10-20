import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class MultiClientServer {
    private static final int PUERTO = 8080;
    private static final ExecutorService pool = Executors.newFixedThreadPool(10);
    private static final RoomManager salas = new RoomManager();

    public static void main(String[] args) throws IOException {
        ServerSocket server = new ServerSocket(PUERTO);

        // Se ejecuta con Ctrl+C (SIGINT) o System.exit
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nIniciando apagado controlado del servidor...");
            try { server.close(); } catch (IOException ignored) { } // hace salir al accept()
            ClientHandler.notificarApagado();                       // avisa a los clientes
            pool.shutdown();                                        // no acepta tareas nuevas
            try {
                if (!pool.awaitTermination(3, TimeUnit.SECONDS)) pool.shutdownNow();
            } catch (InterruptedException e) {
                pool.shutdownNow();
            }
            System.out.println("Servidor apagado.");
        }, "shutdown-hook"));

        System.out.println("JSMS completo escuchando en " + PUERTO);
        try {
            while (!server.isClosed()) {
                Socket cliente = server.accept();
                pool.execute(new ClientHandler(cliente, salas));
            }
        } catch (IOException e) {
            if (!server.isClosed()) System.err.println("Error en el bucle principal: " + e.getMessage());
        }
    }
}
