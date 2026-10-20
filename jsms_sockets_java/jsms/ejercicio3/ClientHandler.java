import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ClientHandler implements Runnable {
    // Nombres de usuario en uso (conjunto thread-safe)
    private static final Set<String> USUARIOS = ConcurrentHashMap.newKeySet();

    private final Socket socket;
    private final ProtocolParser parser = new ProtocolParser();
    private PrintWriter out;
    private String usuario;
    private String sala;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (Socket s = socket;
             BufferedReader in = new BufferedReader(
                     new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter salida = new PrintWriter(
                     new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8), true)) {

            this.out = salida;
            String linea;
            while ((linea = in.readLine()) != null) {
                Command cmd = parser.parsear(linea);
                if (cmd == null) continue;
                if (!procesar(cmd)) break;
            }
        } catch (IOException e) {
            System.err.println("Cliente desconectado: " + e.getMessage());
        } finally {
            if (usuario != null) USUARIOS.remove(usuario);
        }
    }

    /** @return false si hay que cerrar la sesión */
    private boolean procesar(Command cmd) {
        switch (cmd.getTipo()) {
            case CONNECT:
                if (usuario != null) {
                    out.println("400 ERROR Ya estás conectado como " + usuario);
                } else if (!USUARIOS.add(cmd.getArgumento())) {
                    out.println("401 ERROR Nombre de usuario en uso");
                } else {
                    usuario = cmd.getArgumento();
                    out.println("200 OK");
                }
                break;
            case JOIN:
                if (usuario == null) { out.println("400 ERROR Primero hacé CONNECT"); break; }
                sala = cmd.getArgumento();
                out.println("JOINED " + sala);
                break;
            case MSG:
                if (usuario == null) { out.println("400 ERROR Primero hacé CONNECT"); break; }
                if (sala == null)    { out.println("400 ERROR Primero hacé JOIN"); break; }
                out.println("MSG " + usuario + ": " + cmd.getArgumento()); // todavía sin broadcast
                break;
            case QUIT:
                out.println("BYE");
                return false;
            default:
                out.println("400 ERROR " + cmd.getArgumento());
        }
        return true;
    }
}
