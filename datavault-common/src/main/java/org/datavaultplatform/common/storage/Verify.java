package org.datavaultplatform.common.storage;

import java.io.*;
import java.nio.file.Files;
import java.security.*;
import jakarta.xml.bind.annotation.adapters.HexBinaryAdapter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Verify {
    
    public static final String SHA_1_ALGORITHM = "SHA-1";
    
    public enum Method {LOCAL_ONLY, COPY_BACK, CLOUD}

    public static String getDigest(File file) throws Exception {

        MessageDigest sha1 = MessageDigest.getInstance(SHA_1_ALGORITHM);
        
        try (InputStream is = Files.newInputStream(file.toPath())) {
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
        return SHA_1_ALGORITHM;
    }

}
