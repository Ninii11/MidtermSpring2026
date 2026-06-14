package codes;

import java.io.IOException;
import java.util.logging.*;
public final class LoggingSetup {

    private LoggingSetup() {}

    public static void configure() {
        Logger root = Logger.getLogger("");

        for (Handler h : root.getHandlers()) root.removeHandler(h);

        try {
            FileHandler fileHandler = new FileHandler("uno.log", true);
            fileHandler.setLevel(Level.INFO);
            fileHandler.setFormatter(new SimpleFormatter() {
                private static final String FORMAT = "[%1$tF %1$tT] [%2$s] %3$s%n";
                @Override
                public synchronized String format(LogRecord record) {
                    return String.format(FORMAT,
                            record.getMillis(),
                            record.getLevel().getName(),
                            record.getMessage());
                }
            });
            root.addHandler(fileHandler);
        } catch (IOException e) {
            System.err.println("Warning: could not open uno.log for writing: " + e.getMessage());
        }
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.WARNING);
        consoleHandler.setFormatter(new SimpleFormatter() {
            @Override
            public synchronized String format(LogRecord record) {
                return "[LOG:" + record.getLevel() + "] " + record.getMessage() + System.lineSeparator();
            }
        });
        root.addHandler(consoleHandler);

        root.setLevel(Level.INFO);
    }
}