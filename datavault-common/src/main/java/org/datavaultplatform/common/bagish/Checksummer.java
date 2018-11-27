package org.datavaultplatform.common.bagish;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Checksummer {

    /**
     * Compute a hash value for file contents
     * @param file The file we want to hash
     * @param alg The algorithm we wnat to use
     * @return The hash value as a string
     * @throws FileNotFoundException if the file isn't found!
     * @throws IOException if we run into any IO issues
     */
    public String computeFileHash(File file, SupportedAlgorithm alg) throws Exception {
        String hash = null;
        FileInputStream fis = new FileInputStream(file);

        switch (alg) {
            case MD5:
                hash = org.apache.commons.codec.digest.DigestUtils.md5Hex(fis);
                break;
            case SHA1:
                hash = org.apache.commons.codec.digest.DigestUtils.shaHex(fis);
                break;
            case SHA256:
                hash = org.apache.commons.codec.digest.DigestUtils.sha256Hex(fis);
                break;
            case SHA512:
                hash = org.apache.commons.codec.digest.DigestUtils.sha512Hex(fis);
                break;
            default:
                throw new Exception("Unsupported checksum algorithm");
        }

        fis.close();
        return hash;
    }
}
