package org.datavaultplatform.broker.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.nio.file.Path;
import java.nio.file.Paths;

import gov.loc.repository.bagit.BagFactory;
import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.Bag.BagPartFactory;
import gov.loc.repository.bagit.Manifest;
import gov.loc.repository.bagit.ManifestReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import org.datavaultplatform.common.model.FileFixity;

@Service
public class MetadataService {

    private final String metaDir;

    public MetadataService(@Value("${metaDir}")String metaDir) {
        this.metaDir = metaDir;
    }

    private HashMap<String, String> getFileTypes(Path bag) {
       
        try {
            HashMap<String, String> fileTypes;
            File fileTypeMetaFile = bag.resolve("metadata").resolve("filetype.json").toFile();
            ObjectMapper mapper = new ObjectMapper();
            TypeReference<HashMap<String,String>> typeRef = new TypeReference<HashMap<String,String>>() {};
            fileTypes = mapper.readValue(fileTypeMetaFile, typeRef);
            
            return fileTypes;
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public ArrayList<FileFixity> getManifest(String bagId) {
        
        ArrayList<FileFixity> files = new ArrayList<>();
        
        try {
            Path metaBagPath = Paths.get(metaDir, bagId);
            
            // Get the file type metadata
            HashMap<String, String> fileTypes = getFileTypes(metaBagPath);
            
            BagFactory factory = new BagFactory();
            Bag bag = factory.createBag(metaBagPath.toFile());
            bag.loadFromFiles();

            BagPartFactory partFactory = factory.getBagPartFactory();

            List<Manifest> manifests = bag.getPayloadManifests();

            for (Manifest manifest : manifests) {

                String fixityAlgorithm = manifest.getAlgorithm().bagItAlgorithm;
                
                Path manifestPath = metaBagPath.resolve(manifest.getFilepath());
                File manifestFile = manifestPath.toFile();
                FileInputStream stream = new FileInputStream(manifestFile);

                // TODO: Can we specify this as StandardCharsets.UTF_8 ?
                ManifestReader reader = partFactory.createManifestReader(stream, "utf-8");

                while (reader.hasNext()) {
                    ManifestReader.FilenameFixity file = reader.next();
                    
                    String filePath = file.getFilename();
                    String bagDataPrefix = "data/";
                    if (filePath.startsWith(bagDataPrefix)) {
                        filePath = filePath.replaceFirst(bagDataPrefix, "");
                    }
                    
                    String fileType = "";
                    if (fileTypes.containsKey(filePath)) {
                        fileType = fileTypes.get(filePath);
                    }
                    
                    files.add(new FileFixity(filePath,
                                             file.getFixityValue(),
                                             fixityAlgorithm,
                                             fileType));
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

