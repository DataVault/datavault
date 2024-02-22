package org.datavaultplatform.common.crypto;

import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.zip.CRC32;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Base64;
import org.datavaultplatform.test.SlowTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Slf4j
public class EncryptionTest {

    private static File bigdataResourcesDir;
    private static File testDir;

    @BeforeAll
    public static void setUpClass() {
        String resourcesPath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test"
                + File.separator + "resources";

        bigdataResourcesDir = new File(resourcesPath + File.separator + "big_data");
        testDir = new File(resourcesPath + File.separator + "tmp");

        Security.addProvider(new BouncyCastleProvider());

        System.out.println("\nSecurity-Provider:");
        for (Provider prov : Security.getProviders()) {
            System.out.println("  " + prov + ": " + prov.getInfo());
        }
    }

    @BeforeEach
    public void setUp() {
        try {
            testDir.mkdir();
        } catch (SecurityException se) {
            fail(se.getMessage());
        }
    }

    @Test
    public void testEncryptDecryptSecret() {
        System.out.println("Start testEncryptDecryptSecret...");

        String secret = "This is a complex Secret!";

        SecretKey secretKey = null;
        try {
            secretKey = Encryption.generateSecretKey();
        } catch (Exception e) {
            log.error("unexpected exception",e);
            fail("Exception happened while generating secret key " + e);
        }

        byte[] encrypted = null;
        byte[] iv = Encryption.generateIV();
        try {
            encrypted = Encryption.encryptSecret(secret, secretKey, iv);
        } catch (Exception e) {
            log.error("unexpected exception",e);
            fail("Exception happened while encrypting " + e);
        }

        String encryptedTxt = Base64.toBase64String(encrypted);
        System.out.println("encrypted: "+encryptedTxt);
        String ivTxt = Base64.toBase64String(iv);
        System.out.println("iv: "+ivTxt);

        byte[] result = null;
        try {
            result = Encryption.decryptSecret(Base64.decode(encryptedTxt), Base64.decode(ivTxt), secretKey);
        } catch (Exception e) {
            log.error("unexpected exception",e);
            fail("Exception happened while decrypting " + e);
        }

        System.out.println("decrypted: "+new String(result));

        assertEquals(secret, new String(result));
    }

    @Test
    public void testSimpleEncryptDecryptSecret() {
        System.out.println("Start testSimpleEncryptDecryptSecret...");

        byte[] input = "This is a simple secret!".getBytes();

        SecretKey aesKey = null;
        try {
            KeyGenerator keygen = KeyGenerator.getInstance("AES");
            keygen.init(256);
            aesKey = keygen.generateKey();
        } catch (Exception e) {
            log.error("unexpected exception",e);
            fail("Exception happened while generating key " + e);
        }

        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance("AES/GCM/NoPadding", "BC");
        } catch (Exception e) {
            log.error("unexpected exception",e);
            fail("Exception happened while generating key " + e);
        }

        byte[] iv = new byte[96];
        SecureRandom secRandom = new SecureRandom();
        secRandom.nextBytes(iv); // SecureRandom initialized using self-seeding

        GCMParameterSpec spec = new GCMParameterSpec(128, iv);

        try {
            cipher.init(Cipher.ENCRYPT_MODE, aesKey, spec);
        } catch (Exception e) {
            log.error("unexpected exception",e);
            fail("Exception happened while initializing Cipher " + e);
        }

        byte[] cipherText = null;
        try {
            cipherText = cipher.doFinal(input);
        } catch (Exception e) {
            log.error("unexpected exception",e);
            fail("Exception happened while encrypting " + e);
        }
        System.out.println("encrypted: "+new String(cipherText));

        try {
            cipher.init(Cipher.DECRYPT_MODE, aesKey, spec);
        } catch (Exception e) {
            log.error("unexpected exception",e);
            fail("Exception happened while initializing Cipher " + e);
        }

        byte[] plainText = null;
        try {
            plainText = cipher.doFinal(cipherText);
        } catch (Exception e) {
            log.error("unexpected exception",e);
            fail("Exception happened while decrypting " + e);
        }
        System.out.println("decrypted: "+new String(plainText));

        assertEquals(new String(input), new String(plainText));
    }

    @Test
    public void testBigFileGCMCriptoWithoutAAD() {
        System.out.println("Start testBigFileGCMCriptoWithoutAAD...");

        // Generating Key
        SecretKey aesKey = null;
        try {
            aesKey = Encryption.generateSecretKey(128);
        } catch (NoSuchAlgorithmException noSuchAlgoExc) {
            fail("Key being request is for AES algorithm, "
                    + "but this cryptographic algorithm is not available in the environment " + noSuchAlgoExc);
        }

        // Generating IV
        byte[] iv = Encryption.generateIV(Encryption.IV_SIZE);

        Cipher cIn = null;
        try {
            cIn = Encryption.initGCMCipher(Cipher.ENCRYPT_MODE, aesKey, iv);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            fail("Unexpected Exception thrown while creating cipher: " + sw);
        }

        File inputFile = new File(bigdataResourcesDir, "big_file");

        long startTime = System.currentTimeMillis();

        long csumInputFile = 0;
        try {
            csumInputFile = FileUtils.checksum(inputFile, new CRC32()).getValue();
        } catch (IOException ioe) {
            fail("Unexpected Exception thrown while getting checksum of input file: " + ioe);
        }

        File encryptedFile = new File(testDir, "BigGCMencriptedOutputFile");
        // Create a Tar file from resource dir
        try {
            Encryption.doByteBufferFileCrypto(inputFile, encryptedFile, cIn);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            fail("Unexpected Exception thrown while encrypting file: " + sw);
        }

        long midTime = System.currentTimeMillis();
        System.out.println("\t Encryption: " + TimeUnit.MILLISECONDS.toSeconds(midTime - startTime) + "."
                + TimeUnit.MILLISECONDS.toMillis(midTime - startTime) + " sec");

        File outputFile = new File(testDir, "big_file");

        Cipher cOut = null;
        try {
            cOut = Encryption.initGCMCipher(Cipher.DECRYPT_MODE, aesKey, iv);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            fail("Unexpected Exception thrown while creating cipher: " + sw);
        }

        midTime = System.currentTimeMillis();

        try {
            Encryption.doByteBufferFileCrypto(encryptedFile, outputFile, cOut);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            fail("Unexpected Exception thrown while decrypting file: " + sw);
        }

        long endTime = System.currentTimeMillis();
        System.out.println("\tDecryption: " + TimeUnit.MILLISECONDS.toSeconds(endTime - midTime) + "."
                + TimeUnit.MILLISECONDS.toMillis(endTime - midTime) + " sec");

        assertTrue(outputFile.exists());

        long csumOutputFile = 0;
        try {
            csumOutputFile = FileUtils.checksum(outputFile, new CRC32()).getValue();
        } catch (IOException ioe) {
            fail("Unexpected Exception thrown while getting checksum of output file: " + ioe);
        }

        assertEquals(csumInputFile, csumOutputFile);

        FileUtils.deleteQuietly(encryptedFile);
        FileUtils.deleteQuietly(outputFile);

        System.out.println("End testBigFileGCMCriptoWithoutAAD");
    }

    @Test
    public void testBigFileGCMCriptoWithAAD() {
        System.out.println("Start testBigFileGCMCriptoWithAAD...");

        // Generating Key
        SecretKey aesKey = null;
        try {
            aesKey = Encryption.generateSecretKey(128);
        } catch (NoSuchAlgorithmException noSuchAlgoExc) {
            fail("Key being request is for AES algorithm, "
                    + "but this cryptographic algorithm is not available in the environment " + noSuchAlgoExc);
        }

        // Generating IV
        byte[] iv = Encryption.generateIV(Encryption.IV_SIZE);

        // Any random data can be used as tag. Some common examples could be domain name
        byte[] aadData = "bd67a172c130822d6c306e02b1d9544f".getBytes();

        Cipher cIn = null;
        try {
            cIn = Encryption.initGCMCipher(Cipher.ENCRYPT_MODE, aesKey, iv, aadData);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            fail("Unexpected Exception thrown while creating cipher: " + sw);
        }

        File inputFile = new File(bigdataResourcesDir, "big_file"); // big_file

        long startTime = System.currentTimeMillis();

        long csumInputFile = 0;
        try {
            csumInputFile = FileUtils.checksum(inputFile, new CRC32()).getValue();
        } catch (IOException ioe) {
            fail("Unexpected Exception thrown while getting checksum of input file: " + ioe);
        }

        File encryptedFile = new File(testDir, "BigGCMencriptedOutputFile");
        // Create a Tar file from resource dir
        try {
            Encryption.doByteBufferFileCrypto(inputFile, encryptedFile, cIn);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            fail("Unexpected Exception thrown while encrypting file: " + sw);
        }

        long midTime = System.currentTimeMillis();
        System.out.println("\t Encryption: " + TimeUnit.MILLISECONDS.toSeconds(midTime - startTime) + "."
                + TimeUnit.MILLISECONDS.toMillis(midTime - startTime) + " sec");

        File outputFile = new File(testDir, "big_file");

        Cipher cOut = null;
        try {
            cOut = Encryption.initGCMCipher(Cipher.DECRYPT_MODE, aesKey, iv, aadData);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            fail("Unexpected Exception thrown while creating cipher: " + sw);
        }

        midTime = System.currentTimeMillis();

        try {
            Encryption.doByteBufferFileCrypto(encryptedFile, outputFile, cOut);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            fail("Unexpected Exception thrown while decrypting file: " + sw);
        }

        long endTime = System.currentTimeMillis();
        System.out.println("\tDecryption: " + TimeUnit.MILLISECONDS.toSeconds(endTime - midTime) + "."
                + TimeUnit.MILLISECONDS.toMillis(endTime - midTime) + " sec");

        assertTrue(outputFile.exists());

        long csumOutputFile = 0;
        try {
            csumOutputFile = FileUtils.checksum(outputFile, new CRC32()).getValue();
        } catch (IOException ioe) {
            fail("Unexpected Exception thrown while getting checksum of output file: " + ioe);
        }

        assertEquals(csumInputFile, csumOutputFile);

        FileUtils.deleteQuietly(encryptedFile);
        FileUtils.deleteQuietly(outputFile);

        System.out.println("End testBigFileGCMCriptoWithAAD");
    }

    @Test
    @SlowTest
    @Disabled
    public void testHugeFileGCMCriptoWithAAD() {
        System.out.println("Start testHugeFileGCMCriptoWithAAD...");

        // Generating Key
        SecretKey aesKey = null;
        try {
            aesKey = Encryption.generateSecretKey(128);
        } catch (NoSuchAlgorithmException noSuchAlgoExc) {
            fail("Key being request is for AES algorithm, "
                    + "but this cryptographic algorithm is not available in the environment " + noSuchAlgoExc);
        }

        // Generating IV
        byte[] iv = Encryption.generateIV(Encryption.IV_SIZE);

        // Any random data can be used as tag. Some common examples could be domain name
        byte[] aadData = "bd67a172c130822d6c306e02b1d9544f".getBytes();

        Cipher cIn = null;
        try {
            cIn = Encryption.initGCMCipher(Cipher.ENCRYPT_MODE, aesKey, iv, aadData);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            fail("Unexpected Exception thrown while creating cipher: " + sw);
        }

        File inputFile = new File(bigdataResourcesDir, "1GB_file");

        long startTime = System.currentTimeMillis();

        long csumInputFile = 0;
        try {
            csumInputFile = FileUtils.checksum(inputFile, new CRC32()).getValue();
        } catch (IOException ioe) {
            fail("Unexpected Exception thrown while getting checksum of input file: " + ioe);
        }

        File encryptedFile = new File(testDir, "BigGCMencriptedOutputFile");
        // Create a Tar file from resource dir
        try {
            Encryption.doByteBufferFileCrypto(inputFile, encryptedFile, cIn);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            fail("Unexpected Exception thrown while encrypting file: " + sw);
        }

        long midTime = System.currentTimeMillis();
        System.out.println("\t Encryption: " + TimeUnit.MILLISECONDS.toSeconds(midTime - startTime) + "."
                + TimeUnit.MILLISECONDS.toMillis(midTime - startTime) + " sec");

        File outputFile = new File(testDir, "huge_file");

        Cipher cOut = null;
        try {
            cOut = Encryption.initGCMCipher(Cipher.DECRYPT_MODE, aesKey, iv, aadData);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            fail("Unexpected Exception thrown while creating cipher: " + sw);
        }

        midTime = System.currentTimeMillis();

        try {
            Encryption.doByteBufferFileCrypto(encryptedFile, outputFile, cOut);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            fail("Unexpected Exception thrown while decrypting file: " + sw);
        }

        long endTime = System.currentTimeMillis();
        System.out.println("\tDecryption: " + TimeUnit.MILLISECONDS.toSeconds(endTime - midTime) + "."
                + TimeUnit.MILLISECONDS.toMillis(endTime - midTime) + " sec");

        assertTrue(outputFile.exists());

        long csumOutputFile = 0;
        try {
            csumOutputFile = FileUtils.checksum(outputFile, new CRC32()).getValue();
        } catch (IOException ioe) {
            fail("Unexpected Exception thrown while getting checksum of output file: " + ioe);
        }

        assertEquals(csumInputFile, csumOutputFile);

        FileUtils.deleteQuietly(encryptedFile);
        FileUtils.deleteQuietly(outputFile);

        System.out.println("End testHugeFileGCMCriptoWithAAD");
    }

    @Test
    @SlowTest
    @Disabled
    public void testHugeFileCBCCriptoWithAAD() {
        System.out.println("Start testHugeFileCBCCriptoWithAAD...");

        // Generating Key
        SecretKey aesKey = null;
        try {
            aesKey = Encryption.generateSecretKey(128);
        } catch (NoSuchAlgorithmException noSuchAlgoExc) {
            fail("Key being request is for AES algorithm, "
                    + "but this cryptographic algorithm is not available in the environment " + noSuchAlgoExc);
        }

        // Generating IV
        byte[] iv = Encryption.generateIV(Encryption.IV_CBC_SIZE);

        Cipher cIn = null;
        try {
            cIn = Encryption.initCBCCipher(Cipher.ENCRYPT_MODE, aesKey, iv);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            fail("Unexpected Exception thrown while creating cipher: " + sw);
        }

        File inputFile = new File(bigdataResourcesDir, "1GB_file");

        long startTime = System.currentTimeMillis();

        long csumInputFile = 0;
        try {
            csumInputFile = FileUtils.checksum(inputFile, new CRC32()).getValue();
        } catch (IOException ioe) {
            fail("Unexpected Exception thrown while getting checksum of input file: " + ioe);
        }

        File encryptedFile = new File(testDir, "BigGCMencriptedOutputFile");
        // Create a Tar file from resource dir
        try {
            Encryption.doByteBufferFileCrypto(inputFile, encryptedFile, cIn);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            fail("Unexpected Exception thrown while encrypting file: " + sw);
        }

        long midTime = System.currentTimeMillis();
        System.out.println("\t Encryption: " + TimeUnit.MILLISECONDS.toSeconds(midTime - startTime) + "."
                + TimeUnit.MILLISECONDS.toMillis(midTime - startTime) + " sec");

        File outputFile = new File(testDir, "huge_file");

        Cipher cOut = null;
        try {
            cOut = Encryption.initCBCCipher(Cipher.DECRYPT_MODE, aesKey, iv);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            fail("Unexpected Exception thrown while creating cipher: " + sw);
        }

        midTime = System.currentTimeMillis();

        try {
            Encryption.doByteBufferFileCrypto(encryptedFile, outputFile, cOut);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            fail("Unexpected Exception thrown while decrypting file: " + sw);
        }

        long endTime = System.currentTimeMillis();
        System.out.println("\tDecryption: " + TimeUnit.MILLISECONDS.toSeconds(endTime - midTime) + "."
                + TimeUnit.MILLISECONDS.toMillis(endTime - midTime) + " sec");

        assertTrue(outputFile.exists());

        long csumOutputFile = 0;
        try {
            csumOutputFile = FileUtils.checksum(outputFile, new CRC32()).getValue();
        } catch (IOException ioe) {
            fail("Unexpected Exception thrown while getting checksum of output file: " + ioe);
        }

        assertEquals(csumInputFile, csumOutputFile);

        FileUtils.deleteQuietly(encryptedFile);
        FileUtils.deleteQuietly(outputFile);

        System.out.println("End testHugeFileCBCCriptoWithAAD");
    }

    @Test
    void testAttemptToEncryptWithNullKey() {
        String data = "hello";
        InvalidKeyException ex = assertThrows(InvalidKeyException.class, () -> {
            Encryption.initGCMCipher(Cipher.ENCRYPT_MODE, null,
                data.getBytes(StandardCharsets.UTF_8), null);
        });
        assertEquals("Key for algorithm null not suitable for symmetric enryption.", ex.getMessage());
    }

    @Test
    @SneakyThrows
    void testSecretKeyDigest() {
        SecretKey key = Encryption.generateSecretKey();
        String digest = Encryption.getKeyDigest(key);
        String pattern = "([A-Z0-9]{5}-){7}[A-Z0-9]{5}";
        System.out.printf("[%s]%n", digest);
        boolean digestMatchesPattern = digest.matches(pattern);
        assertTrue(digestMatchesPattern);
    }

    @Test
    void testDigestForIv() {
        byte[] iv1 = new byte[]{-2, -32, 73, 70, 22, -116, 8, -10, 31, 61, -120, 14, -97, 110, 32, 38, 17, 4, 93, 127, 44, -78, -55, -59, 81, 57, 119, -16, -79, -3, 79, -94, -48, 66, -29, -5, 34, 13, -5, -37, 117, 97, 48, -75, -8, 74, 40, 56, -84, 76, -87, 81, 101, -63, -45, 43, -2, -128, -57, -95, -109, 2, 43, -105, -48, 0, -43, -93, -15, 52, 9, 72, -56, 79, -99, 77, 32, 121, -106, -70, -14, 125, 23, 76, -2, -101, -13, 110, 23, -128, 67, 71, -32, -73, 109, -89};

        String digest1 = Encryption.getDigestForIv(iv1);
        System.out.printf("IV1 %s%n", Arrays.toString(iv1));
        System.out.printf("IV1 %s%n",digest1);
        assertEquals("96-fae42-d5895-23d6d-d1861-496c9-32fb4-5d", digest1);

        byte[] iv2 = Arrays.copyOf(iv1, iv1.length);
        assertArrayEquals(iv1, iv2);
        iv2[0]=-1; //just 1 bit different

        String digest2 = Encryption.getDigestForIv(iv2);
        assertEquals("96-9312d-522ce-651cd-3e954-f1959-86817-d7", digest2);

        System.out.printf("IV2 %s%n",Arrays.toString(iv2));
        System.out.printf("IV2 %s%n",digest2);
    }
}
