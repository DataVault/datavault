package org.datavaultplatform.common.io;

import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * This Class is an extension of {@link org.apache.commons.io.FileUtils} to add some util features required for the
 * datavault project.
 */
public class FileUtils extends org.apache.commons.io.FileUtils {
    
    public static char[] units = new char[] { 'K','M','G','T' };


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
}
