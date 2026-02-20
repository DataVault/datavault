package org.datavaultplatform.common.util;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;


import java.io.File;
import java.nio.file.Files;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

@EnabledOnOs({OS.LINUX, OS.MAC})
@DisabledInsideDocker //docker image where we run unit tests on Jenkins does not have 'logger' command
class ProcessHelperTest {

    @TempDir
    File tempDir;

    @Test
    void testProcessWithListCommand() throws Exception {
        File f1 = new File(tempDir, "file1.txt");
        File f2 = new File(tempDir, "file2.txt");
        File f3 = new File(tempDir, "file3.txt");

        createFile(f1);
        createFile(f2);
        createFile(f3);
        checkSuccess("test", matchList("file1.txt", "file2.txt", "file3.txt"), "ls", tempDir.toString());
    }

    void createFile(File file) throws Exception {
        Files.createFile(file.toPath());
    }

    void checkSuccess(String desc,
                      Matcher<List<String>> expectedOutputs,
                      String... commands) throws Exception {
        ProcessHelper processHelper = new ProcessHelper(desc, commands);
        ProcessInfo info = processHelper.execute();
        System.out.printf("output %s%n", info.outputMessages());

        assertThat(expectedOutputs.matches(info.outputMessages())).isTrue();
        assertThat(info.exitValue()).isZero();
        assertThat(info.wasSuccess()).isTrue();
    }

    private Matcher<List<String>> matchList(String... items) {
        List<String> list = Arrays.asList(items);
        Matcher<List<String>> matcher = Matchers.equalTo(list);
        return matcher;
    }

    @Test
    void testProcessWithPWD() throws Exception {
        String pwd = System.getProperty("user.dir");
        checkSuccess("test", matchList(pwd), "pwd");
    }

    @Test
    void testProcessWithError() throws Exception {
        Matcher<List<String>> errorMatcher = new TypeSafeMatcher<>() {

            @Override
            public void describeTo(Description description) {
            }

            @Override
            protected boolean matchesSafely(List<String> values) {
                return String.join("", values).contains("bob");
            }
        };
        //TODO gotta be a better way to get something to stderr during test
        checkSuccess("test", errorMatcher, "logger", "-s", "bob");
    }

    @Nested
    class TimedTests {

        @Test
        void testDuration() throws Exception {
            ProcessHelper processHelper = new ProcessHelper("test", "sleep", "5");
            ProcessInfo info = processHelper.execute();
            System.out.printf("output %s%n", info.outputMessages());

            assertThat(info.exitValue()).isZero();
            assertThat(info.wasSuccess()).isTrue();
            assertThat(info.duration()).isGreaterThanOrEqualTo(Duration.ofSeconds(5));
        }

        @Test
        void testTimeout() {
            String regex = "OS process desc\\[(.*?)]pid\\[(\\d+)]TimedOutAfter\\[(PT\\d+S)]forcedToShutdown\\[false]";
            long start = System.nanoTime();
            TimeoutException timeout = assertThrows(TimeoutException.class, () -> {
                ProcessHelper processHelper = new ProcessHelper("test", Duration.ofSeconds(2), "sleep", "3");
                processHelper.execute();
            });
            Duration diff = Duration.ofNanos(System.nanoTime() - start);
            assertThat(timeout.getMessage()).matches(regex);
            assertThat(diff)
                    .isBetween(Duration.ofMillis(2000), Duration.ofMillis(2500));
        }

    }

}