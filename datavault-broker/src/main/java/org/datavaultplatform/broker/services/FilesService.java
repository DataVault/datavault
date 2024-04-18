package org.datavaultplatform.broker.services;


import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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

    private Duration timeout;

    public FilesService(StorageClassNameResolver resolver) {
        this.resolver = resolver;
        this.timeout = Duration.ofSeconds(300);
    }

    public List<FileInfo> getFilesListing(String filePath, FileStore fileStore) {
        return connect(fileStore)
                .map(userStore -> userStore.list(filePath))
                .orElse(Collections.emptyList());
    }

    public Long getFilesize(String filePath, FileStore fileStore) {
        try {
            Optional<UserStore> optUserStore = connect(fileStore);
            if (optUserStore.isPresent()) {
                return executeWithTimeout(new FileSizeTask(optUserStore.get(), filePath), timeout);
            } else {
                return null;
            }
        } catch (Exception e) {
            log.error("unexpected exception", e);
            return null;
        }
    }

    public boolean validPath(String filePath, FileStore fileStore) {
        return connect(fileStore)
                .map(userStore -> userStore.valid(filePath))
                .orElse(false);
    }


    protected void setTimeout(Duration timeout) {
        this.timeout = timeout;
    }

    @SuppressWarnings("OptionalOfNullableMisuse")
    private Optional<UserStore> connect(FileStore fileStore) {
        try {
            UserStore userStore = UserStore.fromFileStore(fileStore, resolver);
            return Optional.ofNullable(userStore);
        } catch (Exception e) {
            log.error("unexpected exception", e);
            return Optional.empty();
        }
    }

    private static class FileSizeTask implements Callable<Long> {

        private final String filePath;
        private final UserStore userStore;

        public FileSizeTask(UserStore userStore, String filePath) {
            this.filePath = filePath;
            this.userStore = userStore;
        }

        @Override
        public Long call() throws Exception {
            return userStore.getSize(filePath);
        }

        @Override
        public String toString() {
            return String.format("FileSizeTask(filePath[%s]userStore[%s])", filePath, userStore);
        }
    }
    
    protected static <T> T executeWithTimeout(Callable<T> callable, Duration timeout) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Future<T> future = executor.submit(callable);
            executor.shutdown(); //no more  tasks to be submitted
            long start = System.currentTimeMillis();
            T result = future.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
            long diff = System.currentTimeMillis() - start;
            log.info("time for {} is [{}]ms", callable, diff);
            return result;
        } catch (TimeoutException ex) {
            log.warn("timed out {}", callable);
            return null;
        } catch (InterruptedException ex) {
            return null;
        } catch (ExecutionException ee) {
            Throwable th = ee.getCause();
            if (th instanceof Exception) {
                throw (Exception) th;
            } else if (th instanceof Error) {
                throw (Error) th;
            } else {
                throw new Exception(th);
            }
        } finally {
            executor.shutdownNow();
        }
    }
}

