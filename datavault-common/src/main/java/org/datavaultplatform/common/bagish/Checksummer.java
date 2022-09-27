package org.datavaultplatform.common.bagish;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.io.FileUtils;
import org.springframework.util.Assert;

import static org.apache.commons.codec.digest.DigestUtils.md5Hex;
import static org.apache.commons.codec.digest.DigestUtils.sha1Hex;
import static org.apache.commons.codec.digest.DigestUtils.sha256Hex;
import static org.apache.commons.codec.digest.DigestUtils.sha512Hex;

@Slf4j
public class Checksummer {

    /**
     * Compute a hash value for file contents
     * @param file The file we want to hash
     * @param alg The algorithm we want to use
     * @return The hash value as a string
     * @throws FileNotFoundException if the file isn't found!
     * @throws IOException if we run into any IO issues
     */
    public String computeFileHash(File file, SupportedAlgorithm alg) throws Exception {

        FileUtils.checkFileExists(file, false);
        Assert.isTrue(alg != null, () -> "The SupportedAlgorithm cannot be null");

        final String hash;

        try (FileInputStream fis = new FileInputStream(file)) {

            switch (alg) {
                case MD5:
                    hash = md5Hex(fis);
                    break;
                case SHA1:
                    hash = sha1Hex(fis);
                    break;
                case SHA256:
                    hash = sha256Hex(fis);
                    break;
                case SHA512:
                    hash = sha512Hex(fis);
                    break;
                default:
                    throw new Exception(String.format("Unsupported checksum algorithm [%s]", alg));
            }
        } catch (IOException ex) {
            log.trace("Error computing checksum for [{}]", file, ex);
            throw ex;
        }
        log.info("File[{}]Checksum[{}]", file, hash);
        return hash;
    }
}
