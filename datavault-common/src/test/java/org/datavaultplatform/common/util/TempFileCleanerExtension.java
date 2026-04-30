package org.datavaultplatform.common.util;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.time.Instant;

@Slf4j
@Order(1) // High priority: ensures it runs first in beforeAll and last in afterAll
public class TempFileCleanerExtension implements BeforeAllCallback, AfterAllCallback {

    @Override
    public void beforeAll(ExtensionContext context) {
        // Only record the time for top-level test classes
        if (context.getRequiredTestClass().getEnclosingClass() == null) {
            // Record the time right before the test class starts
            // We use the specific test class context 
            // to prevent collisions when running test classes in parallel
            context.getStore(ExtensionContext.Namespace.create(getClass(), context.getRequiredTestClass()))
                   .put("testStartTime", Instant.now());
        }
    }

    @Override
    public void afterAll(ExtensionContext context) {
        // Only perform cleanup for top-level test classes
        if (context.getRequiredTestClass().getEnclosingClass() == null) {
            Instant startTime = context.getStore(ExtensionContext.Namespace.create(getClass(), context.getRequiredTestClass()))
                                       .get("testStartTime", Instant.class);
            if (startTime != null) {
                TempFileCleaner.cleanTempTestFiles(startTime);
            } else {
                log.error("No test start time found for test class {}", context.getRequiredTestClass().getName());
            }
        }
    }
}
