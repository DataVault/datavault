package org.datavaultplatform.common.storage.impl;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class OsCommandLineBufferingPrefixGeneratorTest {

    final OsCommandLineBufferingPrefixGenerator generator = Mockito.spy(new OsCommandLineBufferingPrefixGenerator());

    private static final List<String> SCRIPT_COMMANDS = List.of("script", "-q", "/dev/null");
    private static final List<String> STD_BUF_COMMANDS  = List.of("stdBuf", "-oL");
    
    @Nested
    class OperatingSystemTests {
        @Test
        @EnabledOnOs(OS.MAC)
        void testMacOs() {
            List<String> generated = generator.generate();
            assertThat(generated).isEqualTo(SCRIPT_COMMANDS);
        }

        @Test
        @EnabledOnOs(OS.LINUX)
        void testLinux() {
            List<String> generated = generator.generate();
            if (OsCommandLineBufferingPrefixGenerator.isCommandOnPath(OsCommandLineBufferingPrefixGenerator.COMMAND_STDBUF)) {
                assertThat(generated).isEqualTo(STD_BUF_COMMANDS);
            } else if (OsCommandLineBufferingPrefixGenerator.isCommandOnPath(OsCommandLineBufferingPrefixGenerator.COMMAND_SCRIPT)) {
                assertThat(generated).isEqualTo(SCRIPT_COMMANDS);
            } else {
                assertThat(generated).isEmpty();
            }
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
            Mockito.doReturn(true).when(generator).isOnPath(OsCommandLineBufferingPrefixGenerator.COMMAND_SCRIPT);
            Mockito.doReturn(true).when(generator).isMacOs();
            Mockito.lenient().doReturn(false).when(generator).isLinux();
            List<String> generated = generator.generate();
            Mockito.verify(generator).isMacOs();
            Mockito.verify(generator).isOnPath(OsCommandLineBufferingPrefixGenerator.COMMAND_SCRIPT);
            assertThat(generated).isEqualTo(SCRIPT_COMMANDS);
        }

        @Test
        void testLinuxStdBuf() {
            Mockito.doReturn(true).when(generator).isOnPath(OsCommandLineBufferingPrefixGenerator.COMMAND_STDBUF);
            Mockito.doReturn(true).when(generator).isLinux();
            Mockito.lenient().doReturn(false).when(generator).isMacOs();
            List<String> generated = generator.generate();
            Mockito.verify(generator).isLinux();
            Mockito.verify(generator).isOnPath(OsCommandLineBufferingPrefixGenerator.COMMAND_STDBUF);
            assertThat(generated).isEqualTo(STD_BUF_COMMANDS);
        }

        @Test
        void testLinuxScript() {
            Mockito.doAnswer( invocation -> {
                String program = invocation.getArgument(0);
                if (OsCommandLineBufferingPrefixGenerator.COMMAND_SCRIPT.equals(program)) {
                    return true;
                } else {
                    return false;
                }
            }).when(generator).isOnPath(anyString());
            Mockito.doReturn(false).when(generator).isOnPath(OsCommandLineBufferingPrefixGenerator.COMMAND_STDBUF);
            Mockito.doReturn(true).when(generator).isLinux();
            Mockito.lenient().doReturn(false).when(generator).isMacOs();
            List<String> generated = generator.generate();
            Mockito.verify(generator).isLinux();
            ArgumentCaptor<String> argCommand = ArgumentCaptor.forClass(String.class);
            Mockito.verify(generator, times(2)).isOnPath(argCommand.capture());
            assertThat(argCommand.getAllValues()).containsExactlyInAnyOrder(OsCommandLineBufferingPrefixGenerator.COMMAND_STDBUF, OsCommandLineBufferingPrefixGenerator.COMMAND_SCRIPT);
            assertThat(generated).isEqualTo(SCRIPT_COMMANDS);
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