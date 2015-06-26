package org.datavault.broker.services;

import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.ArrayList;
import java.nio.file.Path;
import java.nio.file.Paths;

import gov.loc.repository.bagit.BagFactory;
import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.Bag.BagPartFactory;
import gov.loc.repository.bagit.Manifest;
import gov.loc.repository.bagit.ManifestReader;

import org.datavault.common.model.FileFixity;

public class MetadataService {

    private String metaDir;
    
    public void setMetaDir(String metaDir) {
        this.metaDir = metaDir;
    }
    
    public ArrayList<FileFixity> getManifest(String bagId) throws IOException {
        
        ArrayList<FileFixity> files = new ArrayList<>();
        
        try {
            Path metaBagPath = Paths.get(metaDir, bagId);
            BagFactory factory = new BagFactory();
            Bag bag = factory.createBag(metaBagPath.toFile());
            bag.loadFromFiles();

            BagPartFactory partFactory = factory.getBagPartFactory();

            List<Manifest> manifests = bag.getPayloadManifests();

            for (Manifest manifest : manifests) {

                Path manifestPath = metaBagPath.resolve(manifest.getFilepath());
                File manifestFile = manifestPath.toFile();
                FileInputStream stream = new FileInputStream(manifestFile);

                // TODO: Can we specify this as StandardCharsets.UTF_8 ?
                ManifestReader reader = partFactory.createManifestReader(stream, "utf-8");

                while (reader.hasNext()) {
                    ManifestReader.FilenameFixity file = reader.next();
                    files.add(new FileFixity(file.getFilename(), file.getFixityValue()));
                }
                reader.close();
            }
        } catch (Exception e) {
            // TODO: how should we handle "missing data" here?
            e.printStackTrace();
        }
        
        return files;
    }
}

