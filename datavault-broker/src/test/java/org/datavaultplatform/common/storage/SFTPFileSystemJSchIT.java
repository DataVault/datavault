package org.datavaultplatform.common.storage;

import org.datavaultplatform.common.storage.impl.SFTPFileSystemJSch;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

public interface SFTPFileSystemJSchIT {

    String PID_FILE = "sshd.pid";
    String ROOT_DIR = "rootDir";
    String ROOT_FILE = "rootFile.txt";
    String ONLY_ROOT_CAN_READ_FILE = "onlyRootCanReadFile.txt";
    String ROOT_FILE_WITHIN_ROOT_DIR = "rootDir/rootFile.txt";
    String EXPECTED_SSH_VERSION = "OpenSSH_9.6p1, OpenSSL 3.1.4 24 Oct 2023";
    
    SFTPFileSystemDriver getSftpDriver();
    
    default SFTPFileSystemJSch getJSchSftpDriver() {
        return (SFTPFileSystemJSch) getSftpDriver();
    }
    
    @SuppressWarnings("CommentedOutCode")
    default void checkCanRead(String path, boolean expected) throws Exception {
        SFTPFileSystemJSch jSch = getJSchSftpDriver();
        boolean canReadNEW = jSch.canRead(path);
        assertThat(canReadNEW).isEqualTo(expected);

        /*
        boolean canReadOLD = jSch.canReadOLD(path);
        assertThat(canReadOLD)
                .withFailMessage(String.format("for path [%s], expected canReadOLD[%s] was NOT same as canReadNEW[%s]",path, canReadOLD, canReadNEW))
                .isEqualTo(canReadNEW);                
         */
    }
    
    
    @SuppressWarnings("CommentedOutCode")
    default void checkCanWrite(String path, boolean expected) throws Exception {
        SFTPFileSystemJSch jSch = getJSchSftpDriver();
        boolean canWriteNEW = jSch.canWrite(path);
        assertThat(canWriteNEW).isEqualTo(expected);

        /*
        boolean canWriteOLD = jSch.canWriteOLD(path);
        assertThat(canWriteOLD)
                .withFailMessage(String.format("for path [%s], expected canWriteOLD[%s] was NOT same as canWriteNEW[%s]", path, canWriteOLD, canWriteNEW))
                .isEqualTo(canWriteNEW);                
         */
    }
    
    @ParameterizedTest
    @ValueSource(strings = {PID_FILE, ROOT_FILE, ROOT_DIR, ROOT_FILE_WITHIN_ROOT_DIR})
    default void testCanRead(String path) throws Exception {
        checkCanRead(path, true);
    }

    @ParameterizedTest
    @ValueSource(strings = {ROOT_FILE, ROOT_DIR, ROOT_FILE_WITHIN_ROOT_DIR})
    default void testCannotWrite(String path) throws Exception {
        checkCanWrite(path, false);
    }
    
    @ParameterizedTest
    @ValueSource(strings = {ONLY_ROOT_CAN_READ_FILE})
    default void testCannotRead(String path) throws Exception {
        checkCanRead(path, false);
    }
    
    @ParameterizedTest
    @ValueSource(strings = {PID_FILE})
    default void testCanWrite(String path) throws Exception {
        checkCanWrite(path, true);
    }
    
    @ParameterizedTest
    @CsvSource({
        "'ls -r -c1 root*.txt', 'rootFile.txt'",
        "pwd, /config",
        "'echo $SHELL', /bin/bash"
    })
    default void checkRunCommandStdOut(String command, String stdOut) throws Exception {
        SFTPFileSystemJSch.CommandResult result = getJSchSftpDriver().runCommand(command);
        assertThat(result.getExitStatus()).isZero();
        assertThat(result.getStdOut()).isEqualTo(stdOut);
    }
    
    @Test
    default void checkRunCommandStdErr() throws Exception {
        SFTPFileSystemJSch.CommandResult result = getJSchSftpDriver().runCommand("ssh -V");
        assertThat(result.getExitStatus()).isEqualTo(0);
        assertThat(result.getStdError()).isEqualTo(EXPECTED_SSH_VERSION);
    }

    @Test
    default void checkRunCommandThatFails() throws Exception {
        SFTPFileSystemJSch.CommandResult result = getJSchSftpDriver().runCommand("blah");
        assertThat(result.getExitStatus()).isNotZero();
        assertThat(result.getStdOut()).isBlank();
        assertThat(result.getStdError()).isEqualTo("bash: line 1: blah: command not found");
    }
    
    /*
     * The user who sftp's into the sftp server is 'testuser' who is NOT the root user.
     */
    @Test
    default void testSftpUserViaWhoAmi() throws Exception {
        SFTPFileSystemJSch driver = (SFTPFileSystemJSch) getSftpDriver();
        SFTPFileSystemJSch.CommandResult result = driver.runCommand("whoami");
        System.out.println(result);
        assertThat(result.getExitStatus()).isZero();
        assertThat(result.getStdOut()).contains("testuser");
    }
    
    @Nested
    class InputStreamTests {
        
        @Test
        void testNullStream() {
            assertThat(SFTPFileSystemJSch.readFromStream(null)).isEqualTo("");
        }

        @Test
        void testNonNullStream() {
            byte[] bytes = "BLAH-BLAH".getBytes(StandardCharsets.UTF_8);
            InputStream is = new ByteArrayInputStream(bytes);
            assertThat(SFTPFileSystemJSch.readFromStream(is)).isEqualTo("BLAH-BLAH");
        }
    }
}
