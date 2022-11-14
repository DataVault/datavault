package org.datavaultplatform.broker.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.loc.repository.bagit.domain.Bag;
import gov.loc.repository.bagit.domain.Manifest;
import gov.loc.repository.bagit.reader.BagReader;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.model.FileFixity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@Slf4j
public class MetadataService {

    private static final TypeReference<HashMap<String, String>> TYPE_REF =
        new TypeReference<HashMap<String, String>>() {};

    private final String metaDir;

    public MetadataService(@Value("${metaDir}") String metaDir) {
        this.metaDir = metaDir;
    }

    private Map<String, String> getFileTypes(Path bagPath) {
        try {

            File fileTypeMetaFile = bagPath
                .resolve("metadata")
                .resolve("filetype.json")
                .toFile();

            Map<String, String> fileTypes = new ObjectMapper().readValue(fileTypeMetaFile, TYPE_REF);

            return fileTypes;

        } catch (Exception ex) {
            log.error("problem finding file types in bag [{}]", bagPath, ex);
            return Collections.EMPTY_MAP;
        }
    }

    public List<FileFixity> getManifest(String bagId) {
        List<FileFixity> files;
        try {
            Path metaBagPath = Paths.get(metaDir, bagId);

            // Get the file type metadata
            Map<String, String> fileTypes = getFileTypes(metaBagPath);

            BagReader reader = new BagReader();
            Bag bag = reader.read(metaBagPath);

            Set<Manifest> manifests = bag.getPayLoadManifests();

            files = manifests.stream()
                .flatMap(manifest -> getFileFixityForManifest(manifest, fileTypes).stream())
                .collect(Collectors.toCollection(ArrayList::new));
        } catch (Exception ex) {
            log.error("problem getting manifest for [{}]", bagId, ex);
            files = Collections.EMPTY_LIST;
        }
        return files;
    }

    private List<FileFixity> getFileFixityForManifest(Manifest manifest,
        Map<String, String> fileTypes) {
        List<FileFixity> result = new ArrayList<>();

        manifest.getFileToChecksumMap().forEach((filePath, checksum) -> {
            FileFixity fileFixity = getFileFixity(manifest, fileTypes, filePath, checksum);
            result.add(fileFixity);
        });

        return result;
    }

    private FileFixity getFileFixity(Manifest manifest, Map<String, String> fileTypes, Path filePath,
        String checksum) {

        String fileName = filePath.getFileName().toString();

        String bagDataPrefix = "data/";
        if (fileName.startsWith(bagDataPrefix)) {
            fileName = fileName.replaceFirst(bagDataPrefix, "");
        }

        String fileType = fileTypes.getOrDefault(fileName, "");

        FileFixity fileFixity = new FileFixity(
            fileName,
            checksum,
            manifest.getAlgorithm().getBagitName(),
            fileType);

        return fileFixity;
    }
}
