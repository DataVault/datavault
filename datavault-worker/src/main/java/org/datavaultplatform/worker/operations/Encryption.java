package org.datavaultplatform.worker.operations;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.EnumSet;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;

public class Encryption {
    public static int BUFFER_SIZE = 50 * 1024; // 50KB
    public static int SMALL_BUFFER_SIZE = 1024; // 1KB
    public static int AES_BLOCK_SIZE = 16; // 16 Bytes
    
    public static int AES_KEY_SIZE = 256;
    public static int IV_SIZE = 96;
    public static int IV_CBC_SIZE = 16;
    public static int TAG_BIT_LENGTH = 128;
    public static String GCM_ALGO_TRANSFORMATION_STRING = "AES/GCM/NoPadding";
    public static String CBC_ALGO_TRANSFORMATION_STRING = "AES/CBC/PKCS5Padding";
    public static String CTR_ALGO_TRANSFORMATION_STRING = "AES/CTR/PKCS5Padding";
    public static String CCM_ALGO_TRANSFORMATION_STRING = "AES/CCM/NoPadding";
    
    /**
     * Generate a secret key for AES encryption Need JCE Unlimited Strength to
     * be installed explicitly
     * 
     * @return Key secret key
     * @throws NoSuchAlgorithmException
     *             if cryptographic algorithm is not available in the
     *             environment i.e. AES
     */
    public static SecretKey generateSecretKey() throws NoSuchAlgorithmException{
        return generateSecretKey(AES_KEY_SIZE);
    }
    
    public static SecretKey generateSecretKey(int key_size) throws NoSuchAlgorithmException {
        SecretKey aesKey = null;

        // Specifying algorithm key will be used for
        KeyGenerator keygen = KeyGenerator.getInstance("AES");
        // Specifying Key size to be used, Note: This would need JCE Unlimited
        // Strength to be installed explicitly
        keygen.init(key_size);
        aesKey = keygen.generateKey();

        return aesKey;
    }
    
    /**
     * Generate a Initialisation Vector using default size (i.e. Encryption.IV_SIZE)
     *  
     * @param size in bytes for the iv
     * @return Initialisation Vector
     */
    public static byte[] generateIV(){
        return generateIV(IV_SIZE);
    }
    
    /**
     * Generate a Initialisation Vector
     *  
     * @param size in bytes for the iv
     * @return Initialisation Vector
     */
    public static byte[] generateIV(int size) {
        byte iv[] = new byte[size];
        SecureRandom secRandom = new SecureRandom();
        secRandom.nextBytes(iv); // SecureRandom initialized using self-seeding
        return iv;
    }
    
    public static Cipher initGCMCipher(int opmode, SecretKey aesKey, byte[] iv) throws Exception{
        return initGCMCipher(opmode, aesKey, iv, null);
    }
    
    /**
     * Initialise a AES-GCM Cipher with Bouncy Castle Provider
     * 
     * GCM is a very fast but arguably complex combination of CTR mode and GHASH, 
     * a MAC over the Galois field with 2^128 elements. 
     * Its wide use in important network standards like TLS 1.2 is reflected 
     * by a special instruction Intel has introduced to speed up the calculation of GHASH.
     * 
     * @param opmode - 
     * @param aesKey - secret key
     * @param iv - Initailisation Vector
     * @param aadData - additional authenticated data (optional)
     * @return
     */
    public static Cipher initGCMCipher(int opmode, SecretKey aesKey, byte[] iv, byte[] aadData) throws Exception {
        Cipher c = null;

        // Initialize GCM Parameters
        GCMParameterSpec gcmParamSpec = new GCMParameterSpec(TAG_BIT_LENGTH, iv);

        // Transformation specifies algortihm, mode of operation and padding
        c = Cipher.getInstance(GCM_ALGO_TRANSFORMATION_STRING, "BC");
        
        c.init(opmode, aesKey, gcmParamSpec, new SecureRandom());
        

        if (aadData != null) {
            c.updateAAD(aadData); // add AAD tag data before encrypting
        }

        return c;
    }
    
    /**
     * Initialise a AES-CBC Cipher
     * 
     * CBC has an IV and thus needs randomness every time a message is encrypted, 
     * changing a part of the message requires re-encrypting everything after the change, 
     * transmission errors in one ciphertext block completely destroy the plaintext and 
     * change the decryption of the next block, decryption can be parallelized / encryption can't, 
     * the plaintext is malleable to a certain degree.
     * 
     * @param opmode
     * @param aesKey
     * @param iv
     * @return
     */
    public static Cipher initCBCCipher(int opmode, SecretKey aesKey, byte[] iv) throws Exception {
        Cipher c = null;

        // Initialize Parameters
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

        // Transformation specifies algortihm, mode of operation and padding
        c = Cipher.getInstance(CBC_ALGO_TRANSFORMATION_STRING);
        
        c.init(opmode, aesKey, ivParameterSpec);
        
        return c;
    }

    
    /**
     * Perform crypto using a 1024 Bytes buffer.
     * Depending on the Cipher provided will performe encrytion or Decryption.
     * 
     * @param inputFile
     * @param outputFile
     * @param cipher
     * @throws Exception
     */
    public static void doByteBufferFileCrypto(File inputFile, File outputFile, Cipher cipher) throws Exception {
        byte[] plainBuf = new byte[SMALL_BUFFER_SIZE];
        try (InputStream in = Files.newInputStream(inputFile.toPath());
                OutputStream out = Files.newOutputStream(outputFile.toPath())) {
            int nread;
            while ((nread = in.read(plainBuf)) > 0) {
                byte[] encBuf = cipher.update(plainBuf, 0, nread);
                out.write(encBuf);
            }       
            byte[] encBuf = cipher.doFinal();
            out.write(encBuf);
        }
    }
    
    @Deprecated
    public static void doStreamFileCrypto(File inputFile, File outputFile, Cipher cipher) throws Exception {
        FileInputStream fis = new FileInputStream(inputFile);
        
        FileOutputStream fos = new FileOutputStream(outputFile);
        CipherOutputStream cos = new CipherOutputStream(fos, cipher);
        
        byte[] buffer = new byte[BUFFER_SIZE];
        int count;
        while ((count = fis.read(buffer)) > 0)
        {
            cos.write(buffer, 0, count);
        }
        
        fis.close();
        cos.close();
        fos.close();
    }
    
    @Deprecated
    public static void doMappedBufferedCrypto(File inputFile, File outputFile, Cipher cipher) throws Exception {
        Path inputPathRead = inputFile.toPath();
        FileChannel inputFileChannel = (FileChannel) Files.newByteChannel(inputPathRead, EnumSet.of(StandardOpenOption.READ));
        MappedByteBuffer inputMappedByteBuffer =
                inputFileChannel.map(
                        FileChannel.MapMode.READ_ONLY,
                        0,
                        inputFile.length());

        outputFile.createNewFile();
        Path outputPathRead = outputFile.toPath();
        FileChannel outputFileChannel = (FileChannel) Files.newByteChannel(outputPathRead, EnumSet.of(StandardOpenOption.READ, StandardOpenOption.WRITE));
        MappedByteBuffer outputMappedByteBuffer =
                outputFileChannel.map(
                        FileChannel.MapMode.READ_WRITE,
                        0,
                        cipher.getOutputSize((int) inputFile.length()) );

        cipher.doFinal(inputMappedByteBuffer, outputMappedByteBuffer);

        inputMappedByteBuffer.clear(); // do something with the data and clear/compact it.
        inputFileChannel.close();
        outputMappedByteBuffer.clear(); // do something with the data and clear/compact it.
        outputFileChannel.close();
    }
}
