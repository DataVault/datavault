package org.datavaultplatform.worker.tasks.retrieve;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.datavaultplatform.common.crypto.Encryption;
import org.datavaultplatform.common.storage.Device;
import org.datavaultplatform.common.task.Context;
import org.datavaultplatform.common.util.StorageClassNameResolver;
import org.datavaultplatform.common.util.StorageClassUtils;
import org.datavaultplatform.common.util.Utils;
import org.springframework.util.Assert;

import java.io.File;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;

@Slf4j
public abstract class RetrieveUtils {

    public static final String DATA_VAULT_HIDDEN_FILE_NAME = ".datavault";
    public static final String DV_TEMP_DIR_PREFIX = "dvTempDir";
    public static final String DOT_TAR = ".tar";

    public static void throwChecksumError(
            String actualCheckSum,
            String expectedCheckSum,
            File problemFile,
            String context) throws Exception {

        String msg = String.join(":",
                "Checksum failed",
                context,
                "(actual)" + actualCheckSum + " != (expected)" + expectedCheckSum,
                problemFile.getCanonicalPath()
        );
        log.error(msg);
        throw new Exception(msg);
    }

    public static RuntimeException getRuntimeException(Exception ex) {
        if (ex instanceof RuntimeException) {
            return (RuntimeException) ex;
        } else {
            return new RuntimeException(ex);
        }
    }

    @SneakyThrows
    public static File createTempDataVaultHiddenFile() {
        File tempDir = Files.createTempDirectory(DV_TEMP_DIR_PREFIX).toFile();
        File tempDataVaultHiddenFile = new File(tempDir, DATA_VAULT_HIDDEN_FILE_NAME);
        tempDataVaultHiddenFile.createNewFile();
        Assert.isTrue(tempDataVaultHiddenFile.exists(), "temp data hidden file does not exist");
        Assert.isTrue(tempDataVaultHiddenFile.isFile(), "temp data hidden file is not a file");
        Assert.isTrue(tempDataVaultHiddenFile.canRead(), "temp data hidden file is not readable");
        Assert.isTrue(tempDataVaultHiddenFile.length() == 0, "temp data hidden file is not empty");
        return tempDataVaultHiddenFile;
    }

    public static LocalDate parseLocalDate(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        try {
            return LocalDate.parse(value, DateTimeFormatter.BASIC_ISO_DATE);
        } catch (DateTimeParseException ex) {
            log.warn("cannot parse [{}] into LocalDate", value);
            return null;
        }
    }

    public static boolean getOldRecompose(String depositCreationDate, String contextRecomposeDate) {

        LocalDate depositDate = RetrieveUtils.parseLocalDate(depositCreationDate);
        log.info("DepositDate is: [{}]", depositDate);

        LocalDate recomposeDate = RetrieveUtils.parseLocalDate(contextRecomposeDate);
        log.info("RecomposeDate is: [{}]", recomposeDate);

        // if deposit creation date is before the recomposeDate
        return depositDate != null
                && recomposeDate != null
                && depositDate.isBefore(recomposeDate);
    }
    
    public static Device createDevice(String storageClass, Map<String,String> properties, StorageClassNameResolver storageClassNameResolver) {
        Device device = StorageClassUtils.createStorage(
                storageClass,
                properties,
                Device.class, storageClassNameResolver);
        return device;
    }
    public static void decryptAndCheckTarFile(String label, Context context, byte[] tarIV, File tarFile, String encTarDigest, String tarDigest) throws Exception {
        if (tarIV != null) {
            // Decrypt tar file
            Utils.checkFileHash(label+"-enc", tarFile, encTarDigest);
            Encryption.decryptFile(context, tarFile, tarIV);
        }
        Utils.checkFileHash(label+"-non-enc", tarFile, tarDigest);
    }

    public static String getTarFileName(String bagID) {
        return bagID  + DOT_TAR;
    }
}

