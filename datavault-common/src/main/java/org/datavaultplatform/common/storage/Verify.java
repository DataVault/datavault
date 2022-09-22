package org.datavaultplatform.common.storage;

import java.io.*;
import java.security.*;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Verify {
    
    private static final String algorithm = "SHA-1";
    
    public enum Method {LOCAL_ONLY, COPY_BACK, CLOUD}

    public static String getDigest(File file) throws Exception {

        MessageDigest sha1 = MessageDigest.getInstance(algorithm);
        
        try (InputStream is = new FileInputStream(file)) {
            byte[] buffer = new byte[8192];
            int len = is.read(buffer);

            while (len != -1) {
                sha1.update(buffer, 0, len);
                len = is.read(buffer);
            }
            
            String digest =  new HexBinaryAdapter().marshal(sha1.digest());
            log.info("File[{}]Digest[{}]", file, digest);
            return digest;
        }
    }
    
    public static String getAlgorithm() {
        return algorithm;
    }

}
