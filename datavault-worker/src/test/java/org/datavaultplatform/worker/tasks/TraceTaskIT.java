package org.datavaultplatform.worker.tasks;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import io.micrometer.tracing.Tracer;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.awaitility.Awaitility;
import org.datavaultplatform.common.util.TraceIdWrapper;
import org.datavaultplatform.worker.app.DataVaultWorkerInstanceApp;
import org.datavaultplatform.worker.rabbit.BaseRabbitIT;
import org.datavaultplatform.worker.test.AddTestProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.annotation.DirtiesContext;

import java.nio.charset.StandardCharsets;

@SpringBootTest(classes = {
        DataVaultWorkerInstanceApp.class,
        TraceTaskIT.TestConfig.class
})
@AddTestProperties
@DirtiesContext
@Slf4j
@Profile("database")
class TraceTaskIT extends BaseRabbitIT {

    final Resource traceMessage = new ClassPathResource("sampleMessages/sampleTraceMessage.json");
    @Autowired
    Tracer tracer;

    @BeforeEach
    @SneakyThrows
    void setupTraceTask() {
        setupTestTraceId(getTestTraceId());
    }

    @Test
    @SneakyThrows
    void testSendTraceMessage() {
        // Get the logger for the class that produces the log messages we want to capture
        ch.qos.logback.classic.Logger traceTaskLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Trace.class);

        // Create and start a new ListAppender
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();

        // Add the appender to the logger
        traceTaskLogger.addAppender(listAppender);

        TraceIdWrapper wrapper = new TraceIdWrapper("aaaabbbbccccddddaaaabbbbccccdddd", tracer);
        wrapper.runWithinWrapper(() -> {
            String message = FileUtils.readFileToString(this.traceMessage.getFile(), StandardCharsets.UTF_8);
            sendNormalMessage(message);

            Awaitility.await().until(() -> {
                boolean found = listAppender.list.stream()
                        .map(ILoggingEvent::getFormattedMessage)
                        .anyMatch(msg -> msg.contains("WorkerTraceId: aaaabbbbccccddddaaaabbbbccccdddd"));
                return found;
            });
            return null;
        });
        traceTaskLogger.detachAppender(listAppender);
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        Logger monitorLogger() {
            return log;
        }
    }
}
