package org.datavaultplatform.common.storage.impl;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class OsCommandLineBufferingPrefixGeneratorTest {

    final OsCommandLineBufferingPrefixGenerator generator = Mockito.spy(new OsCommandLineBufferingPrefixGenerator());

    @Nested
    class OperatingSystemTests {
        @Test
        @EnabledOnOs(OS.MAC)
        void testMacOs() {
            List<String> generated = generator.generate();
            assertThat(generated).isEqualTo(List.of("script", "-q", "/dev/null"));
        }

        @Test
        @Disabled
        //@EnabledOnOs(OS.LINUX)
        void testLinux() {
            List<String> generated = generator.generate();
            assertThat(generated).isEqualTo(List.of("stdBuf", "-oL"));
        }

        @Test
        @EnabledOnOs(OS.WINDOWS)
        void testWindows() {
            List<String> generated = generator.generate();
            assertThat(generated).isEmpty();
        }
    }

    @Nested
    class MockedOperatingSystemTests {
        @Test
        void testMacOs() {
            Mockito.doReturn(true).when(generator).isOnPath(OsCommandLineBufferingPrefixGenerator.MACOS_SCRIPT);
            Mockito.doReturn(true).when(generator).isMacOs();
            Mockito.lenient().doReturn(false).when(generator).isLinux();
            List<String> generated = generator.generate();
            Mockito.verify(generator).isMacOs();
            Mockito.verify(generator).isOnPath(OsCommandLineBufferingPrefixGenerator.MACOS_SCRIPT);
            assertThat(generated).isEqualTo(List.of("script", "-q", "/dev/null"));
        }

        @Test
        void testLinux() {
            Mockito.doReturn(true).when(generator).isOnPath(OsCommandLineBufferingPrefixGenerator.LINUX_STDBUF);
            Mockito.doReturn(true).when(generator).isLinux();
            Mockito.lenient().doReturn(false).when(generator).isMacOs();
            List<String> generated = generator.generate();
            Mockito.verify(generator).isLinux();
            Mockito.verify(generator).isOnPath(OsCommandLineBufferingPrefixGenerator.LINUX_STDBUF);
            assertThat(generated).isEqualTo(List.of("stdBuf", "-oL"));
        }

        @Test
        void testWindows() {
            Mockito.doReturn(false).when(generator).isLinux();
            Mockito.doReturn(false).when(generator).isMacOs();
            List<String> generated = generator.generate();
            Mockito.verify(generator).isLinux();
            Mockito.verify(generator).isMacOs();
            assertThat(generated).isEmpty();
        }
    }
}