import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class EchoClient {
    public static void main(String[] args) throws IOException {
        try (Socket socket = new Socket("localhost", 8080);
             BufferedReader in = new BufferedReader(
                     new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(
                     new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
             BufferedReader consola = new BufferedReader(new InputStreamReader(System.in))) {

            System.out.println("Conectado. Escribí mensajes (CHAO para salir).");
            String linea;
            while ((linea = consola.readLine()) != null) {
                out.println(linea);
                if ("CHAO".equalsIgnoreCase(linea)) break;
                String respuesta = in.readLine();
                if (respuesta == null) {
                    System.out.println("El servidor cerró la conexión.");
                    break;
                }
                System.out.println(respuesta);
            }
        }
    }
}
