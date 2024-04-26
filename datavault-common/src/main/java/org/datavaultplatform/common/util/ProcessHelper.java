package org.datavaultplatform.common.util;


import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.lang.reflect.Field;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
public class ProcessHelper {

    public static final Duration DEFAULT_DURATION = Duration.ofHours(1);

    private final String[] commands;
    private final String desc;
    private final Duration maxDuration;

    public ProcessHelper(String desc, String... commands) {
        this(desc, DEFAULT_DURATION, commands);
    }

    public ProcessHelper(String desc, Duration maxDuration, String... commands) {
        this.desc = desc;
        this.commands = commands;
        this.maxDuration = maxDuration;
        log.info("Process desc[{}]commands[{}]", desc, String.join(" ", commands));
    }

    public ProcessInfo execute() throws Exception {
        ProcessBuilder pb = new ProcessBuilder(this.commands);
        Process process = pb.start();
        Integer pid = getPid(process);
        log.info("Process desc[{}]pid[{}] STARTED", desc, pid);
        long start = System.currentTimeMillis();
        boolean exitedNormally = process.waitFor(maxDuration.getSeconds(), TimeUnit.SECONDS);
        boolean timedOut = !exitedNormally;
        if (timedOut) {
            process.destroyForcibly();
        }
        long diff = System.currentTimeMillis() - start;
        return new ProcessInfo(desc, Duration.ofMillis(diff), process, pid, timedOut);
    }

    // TODO - we don't need to do this with Java17+
    private Integer getPid(Process process) {
        try {
            Field pidField = process.getClass().getDeclaredField("pid");
            pidField.setAccessible(true);
            return (Integer) pidField.get(process);
        } catch (Exception ex) {
            log.trace("problem getting Process PID", ex);
            return -1;
        }
    }

    @Getter
    public static class ProcessInfo {
        private final Process process;
        private final boolean timedOut;
        private final int exitValue;
        private final List<String> errorMessages;
        private final List<String> outputMessages;
        private final Duration duration;

        public ProcessInfo(String desc, Duration duration, Process process, Integer pid, boolean timedOut) {
            this.process = process;
            this.timedOut = timedOut;
            this.errorMessages = getLines(process.getErrorStream());
            this.outputMessages = getLines(process.getInputStream());
            this.duration = duration;
            if (process.isAlive()) {
                log.warn("Process desc[{}]pid[{}] is still alive", desc, pid);
                int tempExit;
                try {
                    tempExit = process.destroyForcibly().exitValue();
                } catch (Exception ex) {
                    log.error("problem destroying process task[{}]pid[{}]", desc, pid, ex);
                    tempExit = -1;
                }
                this.exitValue = tempExit;
            } else {
                this.exitValue = process.exitValue();
            }
            log.info("Process desc[{}]pid[{}] EXITED[{}] duration[{}]", desc, pid, exitValue, duration);
        }

        private List<String> getLines(InputStream inputStream) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                return reader.lines()
                        .filter(Objects::nonNull)
                        .filter(not(String::isEmpty))
                        .map(String::trim)
                        .collect(Collectors.toList());
            } catch (Exception ex) {
                log.warn("unexpected error", ex);
                return Collections.emptyList();
            }
        }

        public boolean wasSuccess() {
            return this.exitValue == 0;
        }
        public boolean wasFailure() {
            return ! wasSuccess();
        }
        //this is part of java 17+
        private <T> Predicate<T> not(Predicate<T> predicate) {
            return (T value) -> !predicate.test(value);
        }
    }
    
}
