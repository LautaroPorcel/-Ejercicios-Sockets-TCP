import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class MultiClientServer {
    private static final int PUERTO = 8080;
    private static final ExecutorService pool = Executors.newFixedThreadPool(10);
    private static final RoomManager salas = new RoomManager();

    public static void main(String[] args) throws IOException {
        try (ServerSocket server = new ServerSocket(PUERTO)) {
            System.out.println("JSMS con salas escuchando en " + PUERTO);
            while (true) {
                pool.execute(new ClientHandler(server.accept(), salas));
            }
        }
    }
}
