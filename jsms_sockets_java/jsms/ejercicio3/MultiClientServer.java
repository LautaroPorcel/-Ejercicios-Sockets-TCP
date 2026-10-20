import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class MultiClientServer {
    private static final int PUERTO = 8080;
    private static final ExecutorService pool = Executors.newFixedThreadPool(10);

    public static void main(String[] args) throws IOException {
        try (ServerSocket server = new ServerSocket(PUERTO)) {
            System.out.println("JSMS (protocolo) escuchando en " + PUERTO);
            while (true) {
                pool.execute(new ClientHandler(server.accept()));
            }
        }
    }
}
