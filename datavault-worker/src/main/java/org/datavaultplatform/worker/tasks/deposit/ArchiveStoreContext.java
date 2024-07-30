package org.datavaultplatform.worker.tasks.deposit;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.datavaultplatform.common.storage.ArchiveStore;
import org.springframework.util.Assert;

import java.util.*;

@Slf4j
public class ArchiveStoreContext {

    private final Map<String, String> archiveIds;
    private final HashMap<String, ArchiveStore> archiveStores;

    public ArchiveStoreContext(Map<String, ArchiveStore> archiveStores, Map<String, String> archiveIds) {

        Assert.isTrue(archiveStores != null && !archiveStores.isEmpty(), "The archiveStores map cannot empty");

        this.archiveIds = new TreeMap<>();
        this.archiveStores = new LinkedHashMap<>(archiveStores);
        
        addArchiveIds(archiveIds);
    }

    public Set<ArchiveStoreInfo> getArchiveStoreInfo() {
        HashSet<ArchiveStoreInfo> result = new LinkedHashSet<>();
        for (String archiveStoreId : archiveStores.keySet()) {
            ArchiveStore archiveStore = archiveStores.get(archiveStoreId);
            String archiveId = archiveIds.get(archiveStoreId);
            if(archiveId != null) {
                result.add(new ArchiveStoreInfo(archiveStoreId, archiveStore, archiveId));
            } else {
                log.warn("No ArchiveId for ArchiveStoreId[{}]", archiveStoreId);
            }
        }
        return result;
    }

    public void addArchiveIds(Map<String, String> archiveIdMappings) {
        if (archiveIdMappings == null) {
            log.warn("ignoring null mapping");
            return;
        }
        for (Map.Entry<String, String> entry : archiveIdMappings.entrySet()) {

            String archiveStoreId = entry.getKey();
            String archiveId = entry.getValue();
            
            if (StringUtils.isBlank(archiveStoreId)) {
                log.warn("ignore blank archiveStoreId");
            } else if (StringUtils.isBlank(archiveId)) {
                log.warn("ignore blank archiveId");
            } else if (archiveStores.containsKey(archiveStoreId)) {
                archiveIds.put(archiveStoreId, archiveId);
                log.warn("added mapping from archiveStoreId[{}] to archiveId[{}]", archiveStoreId, archiveId);
            } else {
                log.warn("ignoring invalid mapping from archiveStoreId[{}](unknown) to archiveId[{}] ", archiveStoreId, archiveId);
            }
        }
    }

    public HashMap<String, String> getArchiveIds() {
        return new LinkedHashMap<>(archiveIds);
    }

    public Map<String, ArchiveStore> getArchiveStores() {
        return Collections.unmodifiableMap(this.archiveStores);
    }
}
