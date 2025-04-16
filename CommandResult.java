package ir.ac.kntu;

public class CommandResult {
    private final PrintType type;
    private final Object value;

    public CommandResult(PrintType type, Object value) {
        this.type = type;
        this.value = value;
    }

    public PrintType getType() {
        return type;
    }

    public Object getValue() {
        return value;
    }
}
