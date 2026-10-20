import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class ChatClient {
    public static void main(String[] args) throws IOException {
        String host = args.length > 0 ? args[0] : "localhost";
        int puerto = args.length > 1 ? Integer.parseInt(args[1]) : 8080;

        try (Socket socket = new Socket(host, puerto);
             BufferedReader in = new BufferedReader(
                     new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(
                     new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
             BufferedReader consola = new BufferedReader(new InputStreamReader(System.in))) {

            // Hilo dedicado a escuchar al servidor mientras el principal lee el teclado
            Thread receptor = new Thread(() -> {
                try {
                    String linea;
                    while ((linea = in.readLine()) != null) System.out.println(linea);
                } catch (IOException e) {
                    // socket cerrado: es la forma normal de terminar
                }
                System.out.println("[Conexión cerrada]");
                System.exit(0);
            }, "receptor");
            receptor.setDaemon(true);
            receptor.start();

            String linea;
            while ((linea = consola.readLine()) != null) {
                out.println(linea);
                if ("QUIT".equalsIgnoreCase(linea.trim())) break;
            }
        }
    }
}
