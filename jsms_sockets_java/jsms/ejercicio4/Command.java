public class Command {
    public enum Tipo { CONNECT, JOIN, MSG, QUIT, INVALIDO }

    private final Tipo tipo;
    private final String argumento; // en INVALIDO guarda el motivo del error

    public Command(Tipo tipo, String argumento) {
        this.tipo = tipo;
        this.argumento = argumento;
    }

    public static Command invalido(String motivo) {
        return new Command(Tipo.INVALIDO, motivo);
    }

    public Tipo getTipo() { return tipo; }
    public String getArgumento() { return argumento; }

    @Override
    public String toString() { return tipo + "(" + argumento + ")"; }
}
