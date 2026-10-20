import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class RoomManager {
    // NombreSala -> miembros. CopyOnWriteArrayList porque se hacen muchos más broadcasts que joins/leaves.
    private final Map<String, List<ClientHandler>> salas = new ConcurrentHashMap<>();

    public void unirseASala(String nombreSala, ClientHandler cliente) {
        // compute es atómico por clave: no se pierde el cliente si otro hilo borra la sala vacía a la vez
        salas.compute(nombreSala, (k, lista) -> {
            if (lista == null) lista = new CopyOnWriteArrayList<>();
            lista.add(cliente);
            return lista;
        });
    }

    public void salirDeSala(String nombreSala, ClientHandler cliente) {
        if (nombreSala == null) return;
        salas.computeIfPresent(nombreSala, (k, lista) -> {
            lista.remove(cliente);
            return lista.isEmpty() ? null : lista; // null borra la sala vacía
        });
    }

    public void broadcast(String nombreSala, String mensaje, ClientHandler emisor) {
        List<ClientHandler> miembros = salas.get(nombreSala);
        if (miembros != null) {
            for (ClientHandler miembro : miembros) {
                if (miembro != emisor) { // evita el eco al emisor
                    miembro.enviarMensajeDirecto(mensaje);
                }
            }
        }
    }
}
