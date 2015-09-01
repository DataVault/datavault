package org.datavault.broker.services;

import java.util.HashMap;
import java.util.List;
import org.datavault.common.model.FileInfo;
import org.datavault.common.storage.Auth;
import org.datavault.common.storage.Device;
import org.datavault.common.storage.impl.LocalFileSystem;
import org.datavault.common.storage.impl.SFTPFileSystem;

/**
 * User: Robin Taylor
 * Date: 19/03/2015
 * Time: 13:34
 */
public class MacFilesService {

    private Device fs;

    public void setactiveDir(String activeDir) {
        
        String name = "filesystem";
        Auth auth = null; // Local file access doesn't require credentials
        HashMap<String,String> config = new HashMap<>();
        config.put("rootPath", activeDir);
        
        try {
            fs = new LocalFileSystem(name, auth, config);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        /*
        String name = "sftp";
        Auth auth = new Auth("username", "password", null);
        HashMap<String,String> config = new HashMap<>();
        config.put("host", "example.co.uk");
        config.put("rootPath", "/home/username/");
        
        try {
            fs = new SFTPFileSystem(name, auth, config);
        } catch (Exception e) {
            e.printStackTrace();
        }
        */
    }
    
    public List<FileInfo> getFilesListing(String filePath) {
        return fs.list(filePath);
    }
    
    public boolean validPath(String filePath) {
        return fs.valid(filePath);
    }
}

