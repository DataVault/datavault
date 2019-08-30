package org.datavaultplatform.common.storage.impl;

import com.jcraft.jsch.*;
import org.bouncycastle.util.encoders.Base64;
import org.datavaultplatform.common.crypto.Encryption;
import org.datavaultplatform.common.io.Progress;
import org.datavaultplatform.common.model.FileInfo;
import org.datavaultplatform.common.storage.Device;
import org.datavaultplatform.common.storage.UserStore;
import org.datavaultplatform.common.storage.impl.ssh.Utility;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.Date;

public class SFTPFileSystem extends Device implements UserStore {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(SFTPFileSystem.class);

    private String host = null;
    private String rootPath = null;
    private String username = null;
    private String password = null;
    private byte[] encPrivateKey = null;
    private byte[] encIV = null;
    private String passphrase = null;
    
    private Session session = null;
    private ChannelSftp channelSftp = null;
    private int port;
    private final String PATH_SEPARATOR = "/";
    
    private Utility.SFTPMonitor monitor = null;
    private final int RETRIES = 25;
    
    public SFTPFileSystem(String name, Map<String,String> config) throws Exception {
        super(name, config);

        System.out.println("Construct SFTPFileSystem...");
        
        // Unpack the config parameters (in an implementation-specific way)
        host = config.get("host");
        port = Integer.parseInt(config.get("port"));
        rootPath = config.get("rootPath");
        username = config.get("username");
        password = config.get("password");
        System.out.println("casting byte[]...");
        encPrivateKey = Base64.decode(config.get("privateKey"));
        encIV = Base64.decode(config.get("iv"));
        System.out.println("done!");
        passphrase = config.get("passphrase");
        System.out.println("SFTPFileSystem created...");
    }
    
    private void Connect() throws Exception {
        JSch jsch = new JSch();
        session = jsch.getSession(username, host, port);

        byte[] privateKey = Encryption.decryptSecret(encPrivateKey, encIV);

        logger.debug("Private Key: "+new String(privateKey));

        if (password != null && !password.isEmpty()) {
            session.setPassword(password);
        } else {
            jsch.addIdentity(username, privateKey, null, passphrase.getBytes());
        }

        // todo : check its a known host??
        //jsch.setKnownHosts(".../.ssh/known_hosts");

        java.util.Properties properties = new java.util.Properties();
        properties.put("StrictHostKeyChecking", "no");
        session.setConfig(properties);
        for (int i = 0; i < RETRIES; i++) {
            try {
               logger.info("Sftp connection attempt " + i);
               session.connect();
               break;
            } catch(JSchException ex) {
               if (i == RETRIES - 1) {
                   throw ex;
               }
               continue;
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
                while(in.available() > 0) {
                int i = in.read(tmp, 0, 1024);
                if (i < 0) {
                    break;
                }
                response.append(new String(tmp, 0, i));
            }
        
            if (channelExec.isClosed()) {
                if (in.available() > 0) continue; 
                    System.out.println("exit-status: " + channelExec.getExitStatus());
                    break;
                }
            }
            
            System.out.println("response: " + response);
            return response.toString();
            
        } catch (Exception e) {
            e.printStackTrace();
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
            
            Vector filelist = channelSftp.ls(rootPath + PATH_SEPARATOR + path);
            
            for (int i = 0; i < filelist.size(); i++) {
                ChannelSftp.LsEntry entry = (ChannelSftp.LsEntry)filelist.get(i);
                
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
            e.printStackTrace();
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
            
            SftpATTRS attrs = channelSftp.stat(rootPath + PATH_SEPARATOR + path);
            return true;
            
        } catch (Exception e) {
            e.printStackTrace();
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
            e.printStackTrace();
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
            e.printStackTrace();
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
            e.printStackTrace();
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
            e.printStackTrace();
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

            path = channelSftp.pwd() + "/" + path;
            channelSftp.cd(path);

            // Create timestamped folder to avoid overwriting files
            String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
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
            e.printStackTrace();
            throw e;
        } finally {
            Disconnect();
        }
        
        return path;
    }
}
