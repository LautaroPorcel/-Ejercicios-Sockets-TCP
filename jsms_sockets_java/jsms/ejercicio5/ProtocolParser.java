public class ProtocolParser {

    /** Devuelve null si la línea está vacía (se ignora). Nunca lanza excepciones por sintaxis mala. */
    public Command parsear(String lineaBruta) {
        if (lineaBruta == null || lineaBruta.trim().isEmpty()) return null;

        String[] partes = lineaBruta.trim().split("\\s+", 2);
        String nombre = partes[0].toUpperCase();
        String arg = partes.length > 1 ? partes[1].trim() : "";

        switch (nombre) {
            case "CONNECT":
                if (arg.isEmpty() || arg.contains(" "))
                    return Command.invalido("Sintaxis: CONNECT <username> (sin espacios)");
                return new Command(Command.Tipo.CONNECT, arg);
            case "JOIN":
                if (arg.isEmpty() || arg.contains(" "))
                    return Command.invalido("Sintaxis: JOIN <nombre_sala> (sin espacios)");
                return new Command(Command.Tipo.JOIN, arg);
            case "MSG":
                if (arg.isEmpty())
                    return Command.invalido("Sintaxis: MSG <texto>");
                return new Command(Command.Tipo.MSG, arg);
            case "QUIT":
                return new Command(Command.Tipo.QUIT, "");
            default:
                return Command.invalido("Comando desconocido: " + nombre);
        }
    }
}
