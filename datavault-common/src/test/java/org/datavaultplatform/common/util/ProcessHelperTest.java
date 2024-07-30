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
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@EnabledOnOs({OS.LINUX, OS.MAC})
@DisabledInsideDocker //docker image where we run unit tests on Jenkins does not have 'logger' command
class ProcessHelperTest {

    public static final int EXIT_STATUS_FROM_SIGKILL = 137; //128 + 9 (for sigkill)

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
        checkSuccess("test", matchList("file1.txt", "file2.txt", "file3.txt"), matchList(), "ls", tempDir.toString());
    }

    void createFile(File file) throws Exception {
        Files.createFile(file.toPath());
    }

    void checkSuccess(String desc,
                      Matcher<List<String>> expectedOutputs,
                      Matcher<List<String>> expectedErrors,
                      String... commands) throws Exception {
        ProcessHelper processHelper = new ProcessHelper(desc, commands);
        ProcessHelper.ProcessInfo info = processHelper.execute();
        System.out.printf("output %s%n", info.getOutputMessages());
        System.out.printf("error  %s%n", info.getErrorMessages());

        assertThat(expectedOutputs.matches(info.getOutputMessages())).isTrue();
        assertThat(expectedErrors.matches(info.getErrorMessages())).isTrue();
        assertThat(info.getExitValue()).isZero();
        assertThat(info.wasSuccess()).isTrue();
        assertThat(info.isTimedOut()).isFalse();
    }

    private Matcher<List<String>> matchList(String... items) {
        List<String> list = Arrays.asList(items);
        Matcher<List<String>> matcher = Matchers.equalTo(list);
        return matcher;
    }

    @Test
    void testProcessWithPWD() throws Exception {
        String pwd = System.getProperty("user.dir");
        Matcher<List<String>> matchErrors = Matchers.equalTo(Collections.emptyList());
        checkSuccess("test", matchList(pwd), matchList(), "pwd");
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
        checkSuccess("test", matchList(), errorMatcher, "logger", "-s", "bob");
    }

    @Nested
    class TimedTests {

        @Test
        void testDuration() throws Exception {
            ProcessHelper processHelper = new ProcessHelper("test", "sleep", "5");
            ProcessHelper.ProcessInfo info = processHelper.execute();
            System.out.printf("output %s%n", info.getOutputMessages());
            System.out.printf("error  %s%n", info.getErrorMessages());

            assertThat(info.getExitValue()).isZero();
            assertThat(info.wasSuccess()).isTrue();
            assertThat(info.isTimedOut()).isFalse();
            assertThat(info.getDuration()).isGreaterThanOrEqualTo(Duration.ofSeconds(5));
        }

        @Test
        void testTimeout() throws Exception {
            ProcessHelper processHelper = new ProcessHelper("test", Duration.ofSeconds(2), "sleep", "3");
            ProcessHelper.ProcessInfo info = processHelper.execute();
            System.out.printf("output %s%n", info.getOutputMessages());
            System.out.printf("error  %s%n", info.getErrorMessages());
            assertThat(info.getExitValue()).isEqualTo(EXIT_STATUS_FROM_SIGKILL);
            assertThat(info.wasSuccess()).isFalse();
            assertThat(info.isTimedOut()).isTrue();
            assertThat(info.getDuration()).isLessThan(Duration.ofSeconds(3));

        }

    }

}