package org.datavaultplatform.common.io;

import java.io.File;
import java.io.IOException;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.text.DecimalFormat;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.springframework.util.Assert;

/**
 * This Class is an extension of {@link org.apache.commons.io.FileUtils} to add some util features required for the
 * datavault project.
 */
public class DataVaultFileUtils {
    
    public static final char[] units = new char[] { 'K','M','G','T' };

    /**
     * Take a human readable size (i.e 1GB, 50MB, 10K) and turn it in a long representing the number of Bytes.
     *
     * @param humanReadableSize
     * @return Number of Bytes
     */
    public static long parseFormattedSizeToBytes(String humanReadableSize) {
        int identifier = -1;
        long value;
        int units_index = -1;

        while ( identifier == -1 && units_index < (units.length-1) ) {
            units_index++;
            identifier = humanReadableSize.toUpperCase().indexOf(units[units_index]);
        }
        
        if (identifier >= 0) {
            value = Long.parseLong(humanReadableSize.substring(0,identifier).trim());

            boolean si = true;
            if( identifier == (humanReadableSize.length()-3) &&
                    humanReadableSize.charAt(identifier+1) == 'i' &&
                    humanReadableSize.charAt(identifier+2) == 'B' ) {
                // identifier followed by "iB" then use Binary size i.e. GiB, MiB, KiB ect...
                si = false;
            }

            for (int i = 0; i <= units_index; i++) {
                value = value * (si ? 1000 : 1024);
            }
        } else {
            value = Long.parseLong(humanReadableSize.trim());
        }
        
        return value;
    }

    public static  String getGibibyteSizeStr(long size) {
        double s = size;
        double gibibytes = s/1024/1024/1024;
        DecimalFormat df = new DecimalFormat("#.#");
        df.setRoundingMode(RoundingMode.DOWN);
        String dx = df.format(gibibytes);
        if(dx.matches("0")){
            return FileUtils.byteCountToDisplaySize(size);
        }
        df.setRoundingMode(RoundingMode.HALF_EVEN);
        return df.format(gibibytes) + " GB";
    }


    public static void checkFileExists(File file, boolean createIfMissing) throws IOException {
        Assert.isTrue(file != null, () -> "The file cannot be null");
        if (file.exists() == false) {
            if (!createIfMissing) {
                throw new IllegalArgumentException(String.format("The file [%s] does not exist", file));
            }
            file.createNewFile();
        }
        Assert.isTrue(file.isFile(),
            () -> String.format("[%s] is not a file", file));

        Assert.isTrue(file.canRead(),
            () -> String.format("The file [%s] is not readable", file));
        Assert.isTrue(file.canWrite(),
            () -> String.format("The file [%s] is not writable", file));

    }

    public static void checkDirectoryExists(File dir) {
        checkDirectoryExists(dir, false);
    }

    public static void checkDirectoryExists(File dir, boolean createIfMissing) {
        Assert.isTrue(dir != null, () -> "The directory cannot be null");
        if (dir.exists() == false) {
            if (!createIfMissing) {
                throw new IllegalArgumentException(String.format("The directory [%s] does not exist", dir));
            }
            Assert.isTrue(dir.mkdir(), () -> String.format("Failed to create directory [%s]", dir));
        }
        Assert.isTrue(dir.isDirectory(),
            () -> String.format("[%s] is not a directory", dir));

        Assert.isTrue(dir.canRead(),
            () -> String.format("The directory [%s] is not readable", dir));
        Assert.isTrue(dir.canWrite(),
            () -> String.format("The directory [%s] is not writable", dir));
    }

    @SneakyThrows
    public static String getPermissions(File file) {
        PosixFileAttributes attrs = Files.getFileAttributeView(file.toPath(), PosixFileAttributeView.class)
                .readAttributes();

        return getPermissions(attrs);
    }


    private static String getPermissions(PosixFileAttributes attrs) {
        String permissions = "";

        // Owner permissions
        permissions += getPermissionString("r",attrs, PosixFilePermission.OWNER_READ);
        permissions += getPermissionString("w",attrs, PosixFilePermission.OWNER_WRITE);
        permissions += getPermissionString("x",attrs, PosixFilePermission.OWNER_EXECUTE);

        // Group permissions
        permissions += getPermissionString("r",attrs, PosixFilePermission.GROUP_READ);
        permissions += getPermissionString("w",attrs, PosixFilePermission.GROUP_WRITE);
        permissions += getPermissionString("x",attrs, PosixFilePermission.GROUP_EXECUTE);

        // Other permissions
        permissions += getPermissionString("r",attrs, PosixFilePermission.OTHERS_READ);
        permissions += getPermissionString("w",attrs, PosixFilePermission.OTHERS_WRITE);
        permissions += getPermissionString("x",attrs, PosixFilePermission.OTHERS_EXECUTE);

        return permissions;
    }

    private static String getPermissionString(String function, PosixFileAttributes attrs, PosixFilePermission perm) {
        return attrs.permissions().contains(perm) ? function : "-";
    }
}
