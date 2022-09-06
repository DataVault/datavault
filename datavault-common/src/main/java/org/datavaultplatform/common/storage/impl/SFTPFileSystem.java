package org.datavaultplatform.common.storage.impl;

import com.jcraft.jsch.*;
import java.io.InputStream;
import java.nio.file.Paths;
import java.time.Clock;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.encoders.Base64;
import org.datavaultplatform.common.PropNames;
import org.datavaultplatform.common.crypto.Encryption;
import org.datavaultplatform.common.io.Progress;
import org.datavaultplatform.common.model.FileInfo;
import org.datavaultplatform.common.storage.Device;
import org.datavaultplatform.common.storage.SFTPFileSystemDriver;
import org.datavaultplatform.common.storage.impl.ssh.Utility;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Slf4j
public class SFTPFileSystem extends Device implements SFTPFileSystemDriver {


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
    private Utility.SFTPMonitor monitor = null;

    static {
        JSch.setLogger(JSchLogger.getInstance());
    }

    public SFTPFileSystem(String name, Map<String,String> config) {
        this(name, config, Clock.systemDefaultZone());
    }

    public SFTPFileSystem(String name, Map<String,String> config, Clock clock) {
        super(name, config);
        this.clock = clock;

        log.info("Construct SFTPFileSystem...");
        
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
        log.info("SFTPFileSystem created...");
    }

    public void Connect() throws Exception {
        JSch jsch = new JSch();
        session = jsch.getSession(username, host, port);

        if (password != null && !password.isEmpty()) {
            session.setPassword(password);
        } else {
            byte[] privateKey = Encryption.decryptSecret(encPrivateKey, encIV);

            log.debug("Private Key: "+new String(privateKey));
            jsch.addIdentity(username, privateKey, null, passphrase.getBytes());
        }

        // todo : check its a known host??
        //jsch.setKnownHosts(".../.ssh/known_hosts");

        java.util.Properties properties = new java.util.Properties();
        properties.put(STRICT_HOST_KEY_CHECKING, NO);
        session.setConfig(properties);
        for (int i = 0; i < RETRIES; i++) {
            int attempt = i+1;
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
        
        // Start a channel for SFTP
        Channel channel = session.openChannel("sftp");
        channel.connect();
        channelSftp = (ChannelSftp)channel;
        channelSftp.cd(rootPath);
    }
    
    private void Disconnect() {
        if (channelSftp != null) {
            channelSftp.exit();
        }
        
        if (session != null) {
            session.disconnect();
        }
    }

    private String runCommand(String command) throws Exception {
        
        ChannelExec channelExec = null;
        
        try {
            Connect();
            
            // Start a channel for commands
            channelExec = (ChannelExec)(session.openChannel("exec"));
            
            channelExec.setCommand(command);
            channelExec.setInputStream(null);
            channelExec.setErrStream(System.err);
            InputStream in = channelExec.getInputStream();
            
            channelExec.connect();
            
            byte[] tmp = new byte[1024];
            StringBuilder response = new StringBuilder();

            while(true) {
                while (in.available() > 0) {
                    int i = in.read(tmp, 0, 1024);
                    if (i < 0) {
                        break;
                    }
                    response.append(new String(tmp, 0, i));
                }

                if (channelExec.isClosed()) {
                    if (in.available() > 0) {
                        continue;
                    }
                    log.info("exit-status: " + channelExec.getExitStatus());
                    break;
                }
            }
            
            log.info("response: " + response);
            return response.toString();
            
        } catch (Exception e) {
            log.error("unexpected exception", e);
            throw e;
        } finally {
            if (channelExec != null) {
                channelExec.disconnect();
            }
            Disconnect();
        }
    }
    
    @Override
    public List<FileInfo> list(String path) {
        
        ArrayList<FileInfo> files = new ArrayList<>();
        
        try {
            Connect();
            
            Vector<ChannelSftp.LsEntry> filelist = channelSftp.ls(rootPath + PATH_SEPARATOR + path);

            for (int i = 0; i < filelist.size(); i++) {
                ChannelSftp.LsEntry entry = filelist.get(i);
                
                if (entry.getFilename().equals(".") ||
                        entry.getFilename().equals("..")) {
                        continue;
                }
                
                String entryKey = path + PATH_SEPARATOR + entry.getFilename();
                
                FileInfo info = new FileInfo(entryKey,
                                             "", // Absolute path - unused?
                                             entry.getFilename(),
                                             entry.getAttrs().isDir());
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
    public boolean exists(String path) {
        
        // TODO: handle symbolic links
        
        try {
            Connect();
            
            channelSftp.stat(rootPath + PATH_SEPARATOR + path);
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
                
                return Utility.calculateSize(channelSftp, path);
                
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
            return statVFS.getAvailForNonRoot() * 1024; // bytes
            
        } catch (Exception e) {
            log.error("unexpected exception", e);
            throw e;
        } finally {
            Disconnect();
        }
    }

    @Override
    public void retrieve(String path, File working, Progress progress) throws Exception {
        
        // Strip any leading separators (we want a path relative to the current dir)
        while (path.startsWith(PATH_SEPARATOR)) {
            path = path.replaceFirst(PATH_SEPARATOR, "");
        }
        
        try {
            Connect();
            
            path = channelSftp.pwd() + "/" + path;
            final SftpATTRS attrs = channelSftp.stat(path);
            
            if (attrs.isDir() && !path.endsWith("/")) {
                path = path + "/";
            }
            
            monitor = new Utility.SFTPMonitor(progress);
            
            Utility.getDir(channelSftp, path, working, attrs, monitor);
            
        } catch (Exception e) {
            log.error("unexpected exception", e);
            throw e;
        } finally {
            Disconnect();
        }
    }
    
    @Override
    public String store(String path, File working, Progress progress) throws Exception {

        // Strip any leading separators (we want a path relative to the current dir)
        while (path.startsWith(PATH_SEPARATOR)) {
            path = path.replaceFirst(PATH_SEPARATOR, "");
        }
        
        try {
            Connect();

            path = Paths.get(channelSftp.pwd()).resolve(path).normalize().toString();
            channelSftp.cd(path);

            // Create timestamped folder to avoid overwriting files
            TimeUnit.SECONDS.sleep(2);
            String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date(clock.millis()));
            String timestampDirName = "dv_" + timeStamp;
            path = path + PATH_SEPARATOR + timestampDirName;

            channelSftp.mkdir(timestampDirName);
            channelSftp.cd(timestampDirName);
            
            if (working.isDirectory()) {
                // Create top-level directory
                String dirName = working.getName();
                channelSftp.mkdir(dirName);
                channelSftp.cd(dirName);
            }
            
            monitor = new Utility.SFTPMonitor(progress);

            Utility.send(channelSftp, working, monitor);

        } catch (Exception e) {
            log.error("unexpected exception", e);
            throw e;
        } finally {
            Disconnect();
        }
        
        return path;
    }
}
