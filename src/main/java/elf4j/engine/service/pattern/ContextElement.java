package elf4j.engine.service.pattern;

import elf4j.engine.service.LogEvent;
import java.util.NoSuchElementException;
import lombok.NonNull;
import org.slf4j.MDC;

public class ContextElement implements PatternElement {
    final String key;

    public ContextElement(String key) {
        this.key = key;
    }

    @Override
    public boolean includeCallerDetail() {
        return false;
    }

    public static @NonNull ContextElement from(String patternSegment) {
        return new ContextElement(PatternElements.getPatternElementDisplayOption(patternSegment)
                .orElseThrow(() -> new NoSuchElementException("No key configured in 'context' pattern element")));
    }
    /**
     * @param logEvent entire log content data source to render
     * @param target logging text aggregator of the final log message
     */
    @Override
    public void render(LogEvent logEvent, @NonNull StringBuilder target) {
        String value = MDC.get(key);
        target.append(value == null ? "" : value);
    }
}
