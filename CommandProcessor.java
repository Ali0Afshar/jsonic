package ir.ac.kntu;

import java.util.HashMap;
import java.util.Map;

public class CommandProcessor {
    private final Map<String, CommandHandler> handlers = new HashMap<>();

    public CommandProcessor() {
        handlers.put("create", new CreateCommandHandler());
        handlers.put("insert", new InsertCommandHandler());
        handlers.put("update", new UpdateCommandHandler());
        handlers.put("search", new SearchCommandHandler());
        handlers.put("delete", new DeleteCommandHandler());
    }

    public CommandResult processCommand(InputData inputData) {
        CommandHandler handler = handlers.get(inputData.command);
        if (handler != null) {
            return handler.handle(inputData);
        } 
        else {
            throw new IllegalArgumentException("Unknown command: " + inputData.command);
        }
    }
}
