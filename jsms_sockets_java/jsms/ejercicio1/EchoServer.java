import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class EchoServer {
    public static void main(String[] args) throws IOException {
        int puerto = 8080;
        try (ServerSocket serverSocket = new ServerSocket(puerto)) {
            System.out.println("Servidor iniciado en puerto " + puerto);
            try (Socket clientSocket = serverSocket.accept(); // bloqueante
                 BufferedReader in = new BufferedReader(
                         new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
                 PrintWriter out = new PrintWriter(
                         new OutputStreamWriter(clientSocket.getOutputStream(), StandardCharsets.UTF_8), true)) {

                System.out.println("Cliente conectado desde " + clientSocket.getInetAddress());
                String mensaje;
                while ((mensaje = in.readLine()) != null) { // null = el cliente cerró
                    if ("CHAO".equalsIgnoreCase(mensaje)) break;
                    out.println("ECO: " + mensaje);
                }
            }
            System.out.println("Cliente desconectado, servidor termina.");
        }
    }
}
