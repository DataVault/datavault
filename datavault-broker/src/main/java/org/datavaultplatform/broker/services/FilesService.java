package org.datavaultplatform.broker.services;


import java.util.List;
import java.util.concurrent.*;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.model.FileInfo;
import org.datavaultplatform.common.model.FileStore;
import org.datavaultplatform.common.storage.UserStore;
import org.datavaultplatform.common.util.StorageClassNameResolver;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * User: Robin Taylor
 * Date: 19/03/2015
 * Time: 13:34
 */
@Service
@Transactional
@Slf4j
public class FilesService {

    private final StorageClassNameResolver resolver;

    public FilesService(StorageClassNameResolver resolver) {
        this.resolver = resolver;
    }

    //TODO - DHAY this does not seem thread safe
    private UserStore userStore;
    private final long TIMEOUT_SECONDS = 300;
    
    private boolean connect(FileStore fileStore) {
        try {
            userStore = UserStore.fromFileStore(fileStore, resolver);
            return true;
        } catch (Exception e) {
            log.error("unexpected exception",e);
            return false;
        }
    }
    
    public List<FileInfo> getFilesListing(String filePath, FileStore fileStore) {
        if (connect(fileStore)) {
            return userStore.list(filePath);
        } else {
            return null;
        }
    }
    
    private class FileSizeTask implements Runnable {

        public Long result = null;
        private final String filePath;
        
        public FileSizeTask(String filePath) {
            this.filePath = filePath;
        }

        @Override
        public void run() {
            try {
                result = userStore.getSize(filePath);
            } catch (Exception e) {
                log.warn("problem getting file size", e);
                result = null;
            }
        }
    }
    
    public Long getFilesize(String filePath, FileStore fileStore) {
        
        try {
            if (connect(fileStore)) {
                ExecutorService executor = Executors.newSingleThreadExecutor();
                FileSizeTask task = new FileSizeTask(filePath);
                executor.submit(task).get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
                executor.shutdown();
                return task.result;
            } else {
                return null;
            }
        } catch (Exception e) {
            log.error("unexpected exception", e);
            return null;
        }
    }
    
    public boolean validPath(String filePath, FileStore fileStore) {
        connect(fileStore);
        return userStore.valid(filePath);
    }
}

