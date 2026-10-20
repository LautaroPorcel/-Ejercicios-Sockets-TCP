import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ClientHandler implements Runnable {
    private static final Set<String> USUARIOS = ConcurrentHashMap.newKeySet();
    /** Handlers vivos, para avisarles cuando el servidor se apaga. */
    private static final Set<ClientHandler> ACTIVOS = ConcurrentHashMap.newKeySet();
    private static final int TIMEOUT_MS = 30000;

    private final Socket socket;
    private final RoomManager salas;
    private final ProtocolParser parser = new ProtocolParser();
    private volatile PrintWriter out;
    private String usuario;
    private String sala;

    public ClientHandler(Socket socket, RoomManager salas) {
        this.socket = socket;
        this.salas = salas;
    }

    public void enviarMensajeDirecto(String mensaje) {
        PrintWriter w = out;
        if (w != null) w.println(mensaje);
    }

    /** Avisa al cliente y cierra el socket; el readLine() del hilo falla y se ejecuta limpiar(). */
    public void cerrarPorApagado() {
        enviarMensajeDirecto("SERVER El servidor se está apagando. Hasta pronto.");
        try { socket.close(); } catch (IOException ignored) { }
    }

    public static void notificarApagado() {
        for (ClientHandler h : ACTIVOS) h.cerrarPorApagado();
    }

    @Override
    public void run() {
        ACTIVOS.add(this);
        try (Socket s = socket;
             BufferedReader in = new BufferedReader(
                     new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter salida = new PrintWriter(
                     new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8), true)) {

            s.setSoTimeout(TIMEOUT_MS);   // si no llega nada en 30 s, readLine() lanza SocketTimeoutException
            s.setKeepAlive(true);
            this.out = salida;
            enviarMensajeDirecto("Bienvenido a JSMS. Usá CONNECT <usuario>");

            try {
                String linea;
                while ((linea = in.readLine()) != null) {
                    Command cmd = parser.parsear(linea);
                    if (cmd == null) continue;
                    if (!procesar(cmd)) break;
                }
            } catch (SocketTimeoutException e) {
                // se captura adentro para poder avisarle al cliente antes de que se cierren los streams
                enviarMensajeDirecto("TIMEOUT Cerrando por inactividad (" + (TIMEOUT_MS / 1000) + " s)");
                System.err.println("Timeout de " + (usuario != null ? usuario : "cliente anónimo"));
            }
        } catch (SocketException e) {
            System.err.println("Conexión caída de " + (usuario != null ? usuario : "cliente anónimo") + ": " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Error de E/S: " + e.getMessage());
        } finally {
            limpiar();
        }
    }

    private boolean procesar(Command cmd) {
        switch (cmd.getTipo()) {
            case CONNECT:
                if (usuario != null) {
                    enviarMensajeDirecto("400 ERROR Ya estás conectado como " + usuario);
                } else if (!USUARIOS.add(cmd.getArgumento())) {
                    enviarMensajeDirecto("401 ERROR Nombre de usuario en uso");
                } else {
                    usuario = cmd.getArgumento();
                    enviarMensajeDirecto("200 OK");
                }
                break;
            case JOIN:
                if (usuario == null) { enviarMensajeDirecto("400 ERROR Primero hacé CONNECT"); break; }
                if (sala != null) {
                    salas.broadcast(sala, "SYSTEM " + usuario + " salió de " + sala, this);
                    salas.salirDeSala(sala, this);
                }
                sala = cmd.getArgumento();
                salas.unirseASala(sala, this);
                enviarMensajeDirecto("JOINED " + sala);
                salas.broadcast(sala, "SYSTEM " + usuario + " se unió a " + sala, this);
                break;
            case MSG:
                if (usuario == null) { enviarMensajeDirecto("400 ERROR Primero hacé CONNECT"); break; }
                if (sala == null)    { enviarMensajeDirecto("400 ERROR Primero hacé JOIN"); break; }
                enviarMensajeDirecto("MSG " + usuario + ": " + cmd.getArgumento());
                salas.broadcast(sala, "BROADCAST [" + usuario + "]: " + cmd.getArgumento(), this);
                break;
            case QUIT:
                enviarMensajeDirecto("BYE");
                return false;
            default:
                enviarMensajeDirecto("400 ERROR " + cmd.getArgumento());
        }
        return true;
    }

    /** Se ejecuta siempre, sin importar cómo terminó la sesión (QUIT, timeout, caída, apagado). */
    private void limpiar() {
        ACTIVOS.remove(this);
        if (sala != null) {
            salas.salirDeSala(sala, this);
            salas.broadcast(sala, "SYSTEM " + usuario + " se desconectó", this);
        }
        if (usuario != null) USUARIOS.remove(usuario);
        try { socket.close(); } catch (IOException ignored) { }
    }
}
