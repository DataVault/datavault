package org.datavault.storage;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class DefaultArchiveAdapter implements ArchiveAdapter {
    
    // TODO: possible plugin architecture for adapters:
    // http://www.oracle.com/technetwork/articles/javase/extensible-137159.html
    
    private final Path archivePathBase = Paths.get("/Users/mtlssth3/datavault_archive");
    
    @Override
    public UUID depositBag(File bag) {
        
        // Generate a new unique ID for this bag
        UUID bagId = UUID.randomUUID();
        
        // Determine bag storage path based on ID
        Path bagPath = archivePathBase.resolve(bagId.toString());
        
        // Store the bag in new directory
        // ...
        
        return bagId;
    }
    
    @Override
    public void retrieveBag(UUID bagId, Path output) {
        
        // Determine bag storage path based on ID
        Path bagPath = archivePathBase.resolve(bagId.toString());
        
        // Copy bag to specified directory
        // ...
        
    }
}
