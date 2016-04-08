package org.datavaultplatform.common.storage;

import java.io.*;
import java.security.*;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

public class Verify {
    
    private static final String algorithm = "SHA-1";
    
    public enum Method {LOCAL_ONLY, COPY_BACK};
    
    public static String getDigest(File file) throws Exception {

        MessageDigest sha1 = MessageDigest.getInstance(algorithm);
        
        try (InputStream is = new FileInputStream(file)) {
            byte[] buffer = new byte[8192];
            int len = is.read(buffer);

            while (len != -1) {
                sha1.update(buffer, 0, len);
                len = is.read(buffer);
            }
            
            return new HexBinaryAdapter().marshal(sha1.digest());
        }
    }
    
    public static String getAlgorithm() {
        return algorithm;
    }

}
