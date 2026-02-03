package org.datavaultplatform.worker.tasks;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.storage.impl.MultiLocalFileSystem;
import org.datavaultplatform.worker.utils.DepositEvents;
import org.junit.jupiter.api.Order;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public abstract class BaseDepositTwoLocationsIT extends BaseDepositIT {

    File destDir1;
    File destDir2;

    @SneakyThrows
    @Override
    void setupDestDirs() {
        assertThat(this.destDir).isNull();
        assertThat(this.destDir1).isNull();
        assertThat(this.destDir2).isNull();
        Path baseTemp = Paths.get(this.tempDir);
        destDir1 = baseTemp.resolve("worker-store-1").toFile();
        assertTrue(destDir1.mkdir());
        destDir2 = baseTemp.resolve("worker-store-2").toFile();
        assertTrue(destDir2.mkdir());
        log.info("dest   dir1 [{}]", destDir1);
        log.info("dest   dir2 [{}]", destDir2);
        assertThat(this.destDir).isNull();
        assertThat(this.destDir1).exists();
        assertThat(this.destDir2).exists();
    }

    @Override
    void checkDepositWorkedOkay(String depositMessage, DepositEvents depositEvents){
        checkDepositWorkedOkayInternal(destDir1, SRC_PATH_DEFAULT, depositMessage, depositEvents);
        checkDepositWorkedOkayInternal(destDir2, SRC_PATH_DEFAULT, depositMessage, depositEvents);
    }

    @Override
    @SneakyThrows
    String getArchiveStoreRootPath() {
        return destDir1.getCanonicalPath() + "," + destDir2.getCanonicalPath();
    }

    @Override
    String getArchiveStoreClassName() {
        return MultiLocalFileSystem.class.getName();
    }
    
}