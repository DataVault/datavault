package org.datavault.storage;

import java.io.File;
import java.nio.file.Path;
import java.util.UUID;

public interface ArchiveAdapter {
    
    // Store a bag in the archive, returning an ID.
    public UUID depositBag(File bag);
    
    // Retrieve a bag with a specific ID from the archive.
    public void retrieveBag(UUID bagId, Path output);
    
}
