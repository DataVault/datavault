package org.datavault.common.storage.impl;

import org.datavault.common.storage.Device;
import org.datavault.common.storage.Auth;
import org.datavault.common.model.FileInfo;
import org.datavault.common.io.Progress;

import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Vector;

import com.jcraft.jsch.*;

public class SFTPFileSystem extends Device {

    private String host = null;
    private String rootPath = null;    
    private Session session = null;
    private Channel channel = null;
    private ChannelSftp channelSftp = null;
    private final int port = 22;
    private final String PATH_SEPARATOR = "/";
    
    public SFTPFileSystem(String name, Auth auth, Map<String,String> config) throws Exception {
        super(name, auth, config);
        
        // Unpack the config parameters (in an implementation-specific way)
        host = config.get("host");
        rootPath = config.get("rootPath");
    }
    
    private void Connect() throws Exception {
        JSch jsch = new JSch();
        session = jsch.getSession(auth.getUsername(), host, port);
        session.setPassword(auth.getPassword());
        java.util.Properties properties = new java.util.Properties();
        properties.put("StrictHostKeyChecking", "no");
        session.setConfig(properties);
        session.connect();
        channel = session.openChannel("sftp");
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
        /* Unimplemented */
        return true;
    }
    
    @Override
    public long getSize(String path) {
        /* Unimplemented */
        return 0;
    }

    @Override
    public boolean isDirectory(String path) {
        /* Unimplemented */
        return false;
    }
    
    @Override
    public String getName(String path) {
        /* Unimplemented */
        return "";
    }
    
    @Override
    public long getUsableSpace() {
        /* Unimplemented */
        return 0;
    }

    @Override
    public void copyToWorkingSpace(String path, File working, Progress progress) throws Exception {
        /* Unimplemented */
    }

    @Override
    public void copyFromWorkingSpace(String path, File working, Progress progress) throws Exception {
        /* Unimplemented */
    }
}
