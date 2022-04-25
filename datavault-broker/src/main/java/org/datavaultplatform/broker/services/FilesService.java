package org.datavaultplatform.broker.services;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import org.datavaultplatform.common.model.FileInfo;
import org.datavaultplatform.common.model.FileStore;
import org.datavaultplatform.common.storage.UserStore;

/**
 * User: Robin Taylor
 * Date: 19/03/2015
 * Time: 13:34
 */
import org.springframework.stereotype.Service;
@Service
public class FilesService {

    private UserStore userStore;
    private final long TIMEOUT_SECONDS = 40;
    
    private boolean connect(FileStore fileStore) {
        try {
            Class<?> clazz = Class.forName(fileStore.getStorageClass());
            Constructor<?> constructor = clazz.getConstructor(String.class, Map.class);
            Object instance = constructor.newInstance(fileStore.getStorageClass(), fileStore.getProperties());
            userStore = (UserStore)instance;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
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
        private String filePath = null;
        
        public FileSizeTask(String filePath) {
            this.filePath = filePath;
        }

        @Override
        public void run() {
            try {
                result = userStore.getSize(filePath);
            } catch (Exception e) {
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
            e.printStackTrace();
            return null;
        }
    }
    
    public boolean validPath(String filePath, FileStore fileStore) {
        connect(fileStore);
        return userStore.valid(filePath);
    }
}

