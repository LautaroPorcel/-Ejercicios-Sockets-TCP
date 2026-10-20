import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class ClientHandler implements Runnable {
    private final Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        String quien = socket.getInetAddress() + ":" + socket.getPort();
        System.out.println("[" + Thread.currentThread().getName() + "] atendiendo a " + quien);
        try (Socket s = socket;
             BufferedReader in = new BufferedReader(
                     new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(
                     new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8), true)) {

            String mensaje;
            while ((mensaje = in.readLine()) != null) {
                if ("CHAO".equalsIgnoreCase(mensaje.trim())) break;
                out.println("ECO: " + mensaje);
            }
        } catch (IOException e) {
            System.err.println("Cliente " + quien + " desconectado: " + e.getMessage());
        }
        System.out.println("[" + Thread.currentThread().getName() + "] terminó con " + quien);
    }
}
