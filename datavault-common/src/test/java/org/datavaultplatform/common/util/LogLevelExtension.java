package org.datavaultplatform.common.util;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * A JUnit 5 extension to temporarily change the logging level of specific loggers for a test class.
 * It restores the original logging levels after all tests in the class have run.
 */
public class LogLevelExtension implements BeforeAllCallback, ExtensionContext.Store.CloseableResource {

    private static final String LOG_LEVEL_STORE_KEY = "logLevels";
    private final Map<String, Level> loggersToChange; // Loggers whose levels we want to change
    private Map<String, Level> originalLevels; // Store original levels here

    /**
     * Constructor to specify which loggers to modify and their desired temporary levels.
     *
     * @param loggerName The name of the logger to modify (e.g., "org.datavaultplatform.worker").
     * @param newLevel The temporary logging level (e.g., Level.OFF, Level.DEBUG).
     */
    public LogLevelExtension(String loggerName, Level newLevel) {
        this.loggersToChange = new HashMap<>();
        this.loggersToChange.put(loggerName, newLevel);
    }

    /**
     * Constructor to specify multiple loggers to modify.
     *
     * @param loggersToChange A map where keys are logger names and values are their desired temporary levels.
     */
    public LogLevelExtension(Map<String, Level> loggersToChange) {
        this.loggersToChange = new HashMap<>(loggersToChange);
    }

    @Override
    public void beforeAll(ExtensionContext context) {
        // Initialize the map to store original levels for this instance
        this.originalLevels = new HashMap<>();

        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

        for (Map.Entry<String, Level> entry : loggersToChange.entrySet()) {
            Logger logger = loggerContext.getLogger(entry.getKey());
            this.originalLevels.put(entry.getKey(), logger.getLevel()); // Store original level in the instance field
            logger.setLevel(entry.getValue()); // Set new level
        }
        // Store 'this' instance in the global store, so its close() method is called later by JUnit.
        // The unique ID of the context is a good key to ensure uniqueness per test class.
        context.getStore(ExtensionContext.Namespace.GLOBAL).put(context.getUniqueId(), this);
    }

    @Override
    public void close() {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

        // Access originalLevels directly from the instance field
        for (Map.Entry<String, Level> entry : this.originalLevels.entrySet()) {
            Logger logger = loggerContext.getLogger(entry.getKey());
            logger.setLevel(entry.getValue()); // Restore original level
        }
    }
}