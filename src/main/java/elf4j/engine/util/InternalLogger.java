package elf4j.engine.util;

import elf4j.Level;

/**
 *
 */
public class InternalLogger {
    private InternalLogger() {
    }

    /**
     * @param level   log severity level
     * @param message to message
     */
    public static void log(Level level, String message) {
        System.err.println("ELF4J status " + level + ": " + message);
    }
}
