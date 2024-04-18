package org.datavaultplatform.common.storage.impl;

import com.jcraft.jsch.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.util.encoders.Base64;
import org.datavaultplatform.common.PropNames;
import org.datavaultplatform.common.crypto.Encryption;
import org.datavaultplatform.common.io.Progress;
import org.datavaultplatform.common.model.FileInfo;
import org.datavaultplatform.common.storage.Device;
import org.datavaultplatform.common.storage.SFTPFileSystemDriver;
import org.datavaultplatform.common.storage.impl.ssh.UtilityJSch;
import org.springframework.util.Assert;
import org.slf4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * An implementation of SFTPFileSystemDriver to use JCraft's Jsch ssh/sftp library.
 */
@Slf4j
public class SFTPFileSystemJSch extends Device implements SFTPFileSystemDriver {


    public static final String STRICT_HOST_KEY_CHECKING = "StrictHostKeyChecking";
    public static final String NO = "no";

    private static final String PATH_SEPARATOR = "/";
    private static final int RETRIES = 25;

    private final Clock clock;

    private final String host;
    private final String rootPath;
    private final String username;
    private final String password;
    private final byte[] encPrivateKey;
    private final byte[] encIV;
    private final String passphrase;

    private final int port;

    private Session session = null;
    private ChannelSftp channelSftp = null;
    private UtilityJSch.SFTPMonitor monitor = null;

    static {
        JSch.setLogger(JSchLogger.getInstance());
    }

    public SFTPFileSystemJSch(String name, Map<String, String> config) {
        this(name, config, Clock.systemDefaultZone());
    }

    public SFTPFileSystemJSch(String name, Map<String, String> config, Clock clock) {
        super(name, config);
        this.clock = clock;

        log.info("Construct SFTPFileSystemJSch...");

        // Unpack the config parameters (in an implementation-specific way)
        host = config.get(PropNames.HOST);
        port = Integer.parseInt(config.get(PropNames.PORT));
        rootPath = config.get(PropNames.ROOT_PATH);
        username = config.get(PropNames.USERNAME);
        password = config.get(PropNames.PASSWORD);
        log.info("casting byte[]...");
        encPrivateKey = Base64.decode(config.getOrDefault(PropNames.PRIVATE_KEY, ""));
        encIV = Base64.decode(config.getOrDefault(PropNames.IV, ""));
        log.info("done!");
        passphrase = config.get("passphrase");
        log.info("SFTPFileSystemJSch created...");
    }

    private synchronized void Connect() throws Exception {
        Assert.isTrue(session == null, "The session should be null before Connecting");
        Assert.isTrue(channelSftp == null, "The channelSftp should be null before Connecting");
        try {

        JSch jsch = new JSch();
        session = jsch.getSession(username, host, port);

        if (password != null && !password.isEmpty()) {
            session.setPassword(password);
        } else {
            byte[] privateKey = Encryption.decryptSecret(encPrivateKey, encIV);

            log.debug("Private Key: {}", new String(privateKey));
            jsch.addIdentity(username, privateKey, null, passphrase.getBytes());
        }

        // todo : check its a known host??
        //jsch.setKnownHosts(".../.ssh/known_hosts");

        java.util.Properties properties = new java.util.Properties();
        properties.put(STRICT_HOST_KEY_CHECKING, NO);
        session.setConfig(properties);
        for (int i = 0; i < RETRIES; i++) {
            int attempt = i + 1;
            try {
               log.info("Sftp connection attempt[{}/{}]", attempt, RETRIES);
               session.connect();
               break;
            } catch(JSchException ex) {
               if (i == RETRIES - 1) {
                   log.error("problem with Jsch attempt[{}/{}]", attempt, RETRIES, ex);
                   throw ex;
               } else {
                   log.warn("problem with Jsch attempt[{}/{}]", attempt, RETRIES, ex);
               }
            }
        }

        log.info("ssh-version-client [{}]", session.getClientVersion());
        log.info("ssh-version-server [{}]", session.getServerVersion());

        // Start a channel for SFTP
        Channel channel = session.openChannel("sftp");
        channel.connect();
        channelSftp = (ChannelSftp) channel;
        channelSftp.cd(rootPath);
        } catch (Exception ex) {
            log.error("Problem Connecting", ex);
            Disconnect();
            throw ex;
        } 
    }

    @SuppressWarnings("ConstantValue")
    private synchronized void Disconnect() {
        if (channelSftp != null) {
            try {
                channelSftp.exit();
            } catch (RuntimeException ex) {
                log.warn("problem Exiting channelSftp ", ex);
            } finally {
                channelSftp = null;
            }
        }

        if (session != null) {
            try {
                session.disconnect();
            } catch (RuntimeException ex) {
                log.warn("problem Disconnecting session", ex);
            } finally {
                session = null;
            }
        }
        Assert.isTrue(this.session == null, "session should be null after Disconnecting");
        Assert.isTrue(this.channelSftp == null, "channelSftp should be null after Disconnecting");
    }

    public CommandResult runCommand(String command) throws Exception {
        try {
            Connect();
            return runCommandInternal(command);
        } finally {
            Disconnect();
        }
    }
    
    private CommandResult runCommandInternal(String command) throws Exception {
        
        Assert.isTrue(StringUtils.isNotBlank(command), "The command cannot be blank");
        Assert.isTrue(session != null, String.format("The session should not be null when running command[%s]", command));
        
        ChannelExec channelExec = null;
        
        try {
            // Start a channel for commands
            channelExec = (ChannelExec)session.openChannel("exec");
            
            channelExec.setCommand(command);
            channelExec.setInputStream(null);
            
            InputStream err = channelExec.getErrStream();
            InputStream in = channelExec.getInputStream();

            channelExec.connect();

            String stdErr = readFromStream(err);
            String stdOut = readFromStream(in);
            
            int exitStatus = -1;
            if(channelExec.isClosed()) {
                exitStatus = channelExec.getExitStatus();
            } else { 
                log.warn("command[{}] sftp-exec stream is not closed", command);
            }
            log.info("command[{}] exit-status: [{}]", command, exitStatus);
            
            CommandResult result = new CommandResult();
            result.setCommmand(command);
            result.setExitStatus(exitStatus);
            result.setStdOut(stdOut);
            result.setStdError(stdErr);
            log.info("command[{}] result[{}]", command, result);
            return result;
            
        } catch (Exception e) {
            log.error("command[{}] unexpected exception running sftp-exec", command, e);
            throw e;
        } finally {
            if (channelExec != null) {
                try {
                    channelExec.disconnect();
                } catch (RuntimeException ex) {
                    log.warn("command[{}] problem disconnecting channelExec", command, ex);
                }
            }
        }
    }
    
    @Override
    public List<FileInfo> list(String path) {
        
        ArrayList<FileInfo> files = new ArrayList<>();
        
        try {
            Connect();
            
            CommandResult pwdResult = runCommandInternal("pwd");
            log.info("result_pwd {}", pwdResult);

            CommandResult whoamiResult = runCommandInternal("whoami");
            log.info("result_whoami {}", whoamiResult);

            CommandResult badCommandResult = runCommandInternal("badCommand");
            log.info("result_badCommand {}", badCommandResult);

            Vector<ChannelSftp.LsEntry> filelist = channelSftp.ls(rootPath + PATH_SEPARATOR + path);

            for (int i = 0; i < filelist.size(); i++) {
                ChannelSftp.LsEntry entry = filelist.get(i);
                
                if (entry.getFilename().equals(".") ||
                        entry.getFilename().equals("..")) {
                        continue;
                }
                
                String entryKey = path + PATH_SEPARATOR + entry.getFilename();
                boolean canRead = canReadInternal(entryKey);
                boolean canWrite = canWriteInternal(entryKey);
                
                FileInfo info = new FileInfo(entryKey,
                                             "", // Absolute path - unused?
                                             entry.getFilename(),
                                             entry.getAttrs().isDir(),
                                             canRead,
                                             canWrite);

                log.info(info.toString());
                files.add(info);
                
                // Other useful properties:
                // entry.getAttrs().getSize()
                // entry.getAttrs().isDir()
            }
            
        } catch (Exception e) {
            log.error("unexpected exception", e);
        } finally {
            Disconnect();
        }
        
        return files;
    }
    
    @Override
    public boolean valid(String path) {
        /* Unimplemented */
        return true;
    }

    @Override
    public boolean exists(String relativePath) {
        
        // TODO: handle symbolic links
        String path = getFullPath(relativePath);
        try {
            Connect();
            
            channelSftp.stat(path);
            return true;
            
        } catch (Exception e) {
            log.warn(String.format("does not exist[%s]",path), e);
            return false;
        } finally {
            Disconnect();
        }
    }
    
    @Override
    public long getSize(String path) throws Exception {
        
        try {
            Connect();
            
            path = channelSftp.pwd() + "/" + path;
            final SftpATTRS attrs = channelSftp.stat(path);
            
            if (attrs.isDir()) {
                if (!path.endsWith("/")) {
                    path = path + "/";
                }
                
                return UtilityJSch.calculateSize(channelSftp, path);
                
            } else {
                return attrs.getSize();
            }
            
        } catch (Exception e) {
            log.error("unexpected exception", e);
            throw e;
        } finally {
            Disconnect();
        }
    }

    @Override
    public boolean isDirectory(String path) throws Exception {
        try {
            Connect();
            
            SftpATTRS attrs = channelSftp.stat(rootPath + PATH_SEPARATOR + path);
            return attrs.isDir();
            
        } catch (Exception e) {
            log.error("unexpected exception", e);
            throw e;
        } finally {
            Disconnect();
        }
    }
    
    @Override
    public String getName(String path) {
        if (path.contains(PATH_SEPARATOR)) {
            return path.substring(path.lastIndexOf(PATH_SEPARATOR) + 1);
        } else {
            return path;
        }
    }
    
    @Override
    public long getUsableSpace() throws Exception {
        try {
            Connect();
            
            // Requires the "statvfs" extension e.g. OpenSSL
            // getAvailForNonRoot() returns a count of 1k blocks
            // TODO: is this working correctly with different fragment sizes?
            // TODO: how widely supported is this extension?
            SftpStatVFS statVFS = channelSftp.statVFS(rootPath);
            return statVFS.getAvailForNonRoot() * statVFS.getBlockSize(); // bytes
            
        } catch (Exception e) {
            log.error("unexpected exception", e);
            throw e;
        } finally {
            Disconnect();
        }
    }

    @Override
    public void retrieve(String relativePath, File working, Progress progress) throws Exception {

        try {
            Connect();
            
            String path = getFullPath(relativePath);
            final SftpATTRS attrs = channelSftp.stat(path);
            
            if (attrs.isDir() && !path.endsWith("/")) {
                path = path + "/";
            }
            
            monitor = new UtilityJSch.SFTPMonitor(progress, clock);
            
            UtilityJSch.getDir(channelSftp, path, working, attrs, monitor);
            
        } catch (Exception e) {
            log.error("unexpected exception", e);
            throw e;
        } finally {
            Disconnect();
        }
    }

    @Override
    public String store(String relativePath, File working, Progress progress, String timestampDirName) throws Exception {
        Assert.isTrue(StringUtils.isNotBlank(timestampDirName), "The timestampDirName cannot be blank");
        String path = getFullPath(relativePath);
        try {
            Connect();

            channelSftp.cd(path);

            // Create timestamped folder to avoid overwriting files
            path = path + PATH_SEPARATOR + timestampDirName;

            mkdir(channelSftp, timestampDirName);
            channelSftp.cd(timestampDirName);
            
            if (working.isDirectory()) {
                // Create top-level directory
                String dirName = working.getName();
                mkdir(channelSftp, dirName);
                channelSftp.cd(dirName);
            }
            
            monitor = new UtilityJSch.SFTPMonitor(progress, clock);

            UtilityJSch.send(channelSftp, working, monitor);

        } catch (Exception e) {
            log.error("unexpected exception", e);
            throw e;
        } finally {
            Disconnect();
        }
        
        return path;
    }

    @Override
    public boolean isMonitoring() {
        return true;
    }

    @SneakyThrows
    private void mkdir(ChannelSftp channelSftp, String dirName) {
        try {
            SftpATTRS stat = channelSftp.stat(dirName);
            if (!stat.isDir()) {
                throw new RuntimeException(String.format("[%s] exists but is not a directory", dirName));
            }
        } catch (SftpException ex) {
            channelSftp.mkdir(dirName);
        }
    }

    @SneakyThrows
    private String getFullPath(String relativePath) {
        return SftpUtils.getFullPath(rootPath, relativePath);
    }

    @Override
    public Logger getLogger() {
        return log;
    }

    @Override
    public boolean canRead(String path) throws Exception {
        try {
            Connect();
            return canReadInternal(path);
        } catch (Exception ex) {
            log.error("unexpected exception", ex);
            throw ex;
        } finally {
            Disconnect();
        }
    }

    @Override
    public boolean canWrite(String path) throws Exception {
        try {
            Connect();
            return canWriteInternal(path);
        } catch (Exception ex) {
            log.error("unexpected exception", ex);
            throw ex;
        } finally {
            Disconnect();
        }
    }

    private boolean canReadInternal(String path) throws Exception {
        CommandResult result = runCommandInternal(String.format("[ -r %s ]", path));
        return result.getExitStatus() == 0;
    }

    private boolean canWriteInternal(String path) throws Exception {
        CommandResult result = runCommandInternal(String.format("[ -w %s ]", path));
        return result.getExitStatus() == 0;
    }

    public boolean canReadOLD(String path) throws Exception {
        try {
            Connect();
            return canReadInternalOLD(path);
        } catch (Exception ex) {
            log.error("unexpected exception", ex);
            throw ex;
        } finally {
            Disconnect();
        }
    }

    public boolean canWriteOLD(String path) throws Exception {
        try {
            Connect();
            return canWriteInternalOLD(path);
        } catch (Exception ex) {
            log.error("unexpected exception", ex);
            throw ex;
        } finally {
            Disconnect();
        }
    }
    
    private boolean canWriteInternalOLD(String path) {
        try {
            return getPermissionsString(path).contains("w");
        } catch (Exception ex) {
            log.warn("problem getting permission string for [{}]", path, ex);
            return false;
        }
    }
    
    private boolean canReadInternalOLD(String path) {
        try {
            return getPermissionsString(path).contains("r");
        } catch (Exception ex) {
            log.warn("problem getting permissions string for [{}]", path, ex);
            return false;
        }
    }

    private String getPermissionsString(String path) throws Exception {
        String fullPath = getFullPath(path);
        final SftpATTRS attrs = channelSftp.stat(fullPath);
        int unixPermissions = attrs.getPermissions();
        String unixPermissionsString = attrs.getPermissionsString();
        log.info("fullPath[{}], path[{}], unixPermissions[{}], unixPermissionsString[{}]", fullPath, path, unixPermissions, unixPermissionsString);
        return unixPermissionsString;
    }
    
    public static String readFromStream(InputStream is ) {
        if (is == null) {
            return "";
        }
        try {
            String result = IOUtils.toString(is, StandardCharsets.UTF_8);
            if(result != null){
                result = result.trim();
            }
            return result;
        } catch (RuntimeException | IOException ex) {
            log.warn("problem reading input stream", ex);
            return "";
        }
    }
    
    @Data
    public static class CommandResult {
        private String commmand;
        private int exitStatus;
        private String stdOut;
        private String stdError;
    }
}
