package elf4j.impl.core.util;

import elf4j.Level;

public class InternalLogger {
    private InternalLogger() {
    }

    public static void log(Level level, String message) {
        System.err.println("ELF4J status " + level + ": " + message);
    }
}
