package org.datavaultplatform.worker.tasks;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.datavaultplatform.common.storage.impl.MultiLocalFileSystem;
import org.datavaultplatform.worker.utils.DepositEvents;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public abstract class BaseDepositTwoArchivesIT extends BaseDepositIT {

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
        return destDir1.getCanonicalPath();
    }
    @SneakyThrows
    String getArchiveStoreRootPath1() {
        return getArchiveStoreRootPath();
    }
    @SneakyThrows
    String getArchiveStoreRootPath2() {
        return destDir2.getCanonicalPath();
    }

    @Override
    String getArchiveStoreClassName() {
        return MultiLocalFileSystem.class.getName();
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    @SneakyThrows
    @Override
    String getSampleDepositMessage(String srcPath, String bagId) {
        Resource depositMessageTwoArchiveStores = new ClassPathResource("sampleMessages/sampleDepositMessageTwoArchiveStores.json");

        String temp1 = FileUtils.readFileToString(depositMessageTwoArchiveStores.getFile(),
                StandardCharsets.UTF_8);
        String temp2 = temp1.replaceAll("/tmp/dv/src", sourceDir.getCanonicalPath());
        String temp3 =  temp2.replaceAll("/tmp/dv/dest1", getArchiveStoreRootPath1());
        String temp4 =  temp3.replaceAll("/tmp/dv/dest2", getArchiveStoreRootPath2());
        String temp5 = temp4.replaceAll("src-path-1", srcPath);
        String temp6 = temp5.replaceAll("bf73a7f5-42d1-4c3f-864a-a171af8373d4", bagId);

        return temp6;
    }

}