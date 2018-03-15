package org.datavaultplatform.common.io;

public class FileUtils extends org.apache.commons.io.FileUtils {
    
    public static char[] units = new char[] { 'K','M','G','T' };
    
    public static long parseFormattedSizeToBytes(String humanReadableSize) {
        int identifier = -1;
        long value = -1;
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
}
