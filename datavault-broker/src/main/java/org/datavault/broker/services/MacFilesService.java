package org.datavault.broker.services;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ArrayList;
import org.datavault.common.model.FileInfo;
import org.datavault.common.storage.impl.LocalFileSystem;

/**
 * User: Robin Taylor
 * Date: 19/03/2015
 * Time: 13:34
 */
public class MacFilesService {

    private String activeDir;
    private LocalFileSystem fs;

    public void setactiveDir(String activeDir) {
        this.activeDir = activeDir;
        
        String name = "filesystem";
        String auth = ""; // Local file access doesn't require credentials
        
        try {
            fs = new LocalFileSystem(name, auth, activeDir);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public List<FileInfo> getFilesListing(String filePath) {
        return fs.list(filePath);
    }
    
    public boolean validPath(String filePath) {
        return fs.validPath(filePath);
    }
}

