package org.datavaultplatform.worker.operations;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.EnumSet;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;

import org.apache.commons.io.IOUtils;

public class Encryption {
    public static int BUFFER_SIZE = 50 * 1024; // 50KB
    
    public static int AES_KEY_SIZE = 256;
    public static int IV_SIZE = 96;
    public static int TAG_BIT_LENGTH = 128;
    public static String GCM_ALGO_TRANSFORMATION_STRING = "AES/GCM/PKCS5Padding";
    public static String ECB_ALGO_TRANSFORMATION_STRING = "AES/ECB/PKCS5Padding";
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
    
    public static byte[] generateIV(){
        return generateIV(IV_SIZE);
    }

    public static byte[] generateIV(int size) {
        byte iv[] = new byte[size];
        SecureRandom secRandom = new SecureRandom();
        secRandom.nextBytes(iv); // SecureRandom initialized using self-seeding
        return iv;
    }

    public static byte[] aesGCMEncrypt(String message, SecretKey aesKey, byte[] iv) {
        return aesGCMEncrypt(message, aesKey, iv, null);
    }

    public static byte[] aesGCMEncrypt(String message, SecretKey aesKey, byte[] iv, byte[] aadData) {
        Cipher c = initGCMCipher(Cipher.ENCRYPT_MODE, aesKey, iv, aadData);

        byte[] cipherTextInByteArr = null;
        try {
            cipherTextInByteArr = c.doFinal(message.getBytes());
        } catch (IllegalBlockSizeException illegalBlockSizeExc) {
            System.out.println("Exception while encrypting, due to block size " + illegalBlockSizeExc);
        } catch (BadPaddingException badPaddingExc) {
            System.out.println("Exception while encrypting, due to padding scheme " + badPaddingExc);
            badPaddingExc.printStackTrace();
        }

        return cipherTextInByteArr;
    }

    public static byte[] aesGCMDecrypt(byte[] encryptedMessage, SecretKey aesKey, byte[] iv) {
        return aesGCMDecrypt(encryptedMessage, aesKey, iv, null);
    }

    public static byte[] aesGCMDecrypt(byte[] encryptedMessage, SecretKey aesKey, byte[] iv, byte[] aadData) {
        Cipher c = initGCMCipher(Cipher.DECRYPT_MODE, aesKey, iv, aadData);

        byte[] plainTextInByteArr = null;
        try {
            plainTextInByteArr = c.doFinal(encryptedMessage);
        } catch (IllegalBlockSizeException illegalBlockSizeExc) {
            System.out.println("Exception while decryption, due to block size " + illegalBlockSizeExc);
        } catch (BadPaddingException badPaddingExc) {
            System.out.println("Exception while decryption, due to padding scheme " + badPaddingExc);
            badPaddingExc.printStackTrace();
        }

        return plainTextInByteArr;
    }
    
    public static Cipher initGCMCipher(int opmode, SecretKey aesKey, byte[] iv){
        return initGCMCipher(opmode, aesKey, iv, null);
    }
    
    /**
     * Initialise a AES-GCM Cipher
     * 
     * @param opmode - 
     * @param aesKey - secret key
     * @param iv - Initailisation Vector
     * @param aadData - additional authenticated data (optional)
     * @return
     */
    public static Cipher initGCMCipher(int opmode, SecretKey aesKey, byte[] iv, byte[] aadData) {
        Cipher c = null;

        // Initialize GCM Parameters
        GCMParameterSpec gcmParamSpec = new GCMParameterSpec(TAG_BIT_LENGTH, iv);

        try {
            // Transformation specifies algortihm, mode of operation and padding
            c = Cipher.getInstance(GCM_ALGO_TRANSFORMATION_STRING);
        } catch (NoSuchAlgorithmException noSuchAlgoExc) {
            System.out.println(
                    "Exception while encrypting. Algorithm being requested is not available in this environment "
                            + noSuchAlgoExc);
            System.exit(1);
        } catch (NoSuchPaddingException noSuchPaddingExc) {
            System.out.println(
                    "Exception while encrypting. Padding Scheme being requested is not available this environment "
                            + noSuchPaddingExc);
            System.exit(1);
        }

        try {
            c.init(opmode, aesKey, gcmParamSpec, new SecureRandom());
        } catch (InvalidKeyException invalidKeyExc) {
            System.out.println(
                    "Exception while encrypting. Key being used is not valid. It could be due to invalid encoding, wrong length or uninitialized "
                            + invalidKeyExc);
            System.exit(1);
        } catch (InvalidAlgorithmParameterException invalidAlgoParamExc) {
            System.out.println("Exception while encrypting. Algorithm parameters being specified are not valid "
                    + invalidAlgoParamExc);
            System.exit(1);
        }

        if (aadData != null) {
            try {
                c.updateAAD(aadData); // add AAD tag data before encrypting
            } catch (IllegalArgumentException illegalArgumentExc) {
                System.out.println("Exception thrown while encrypting. Byte array might be null " + illegalArgumentExc);
                System.exit(1);
            } catch (IllegalStateException illegalStateExc) {
                System.out
                        .println("Exception thrown while encrypting. CIpher is in an illegal state " + illegalStateExc);
                System.exit(1);
            } catch (UnsupportedOperationException unsupportedExc) {
                System.out.println("Exception thrown while encrypting. Provider might not be supporting this method "
                        + unsupportedExc);
                System.exit(1);
            }
        }

        return c;
    }
    
    public static Cipher initECBCipher(int opmode, SecretKey aesKey) {
        Cipher c = null;

        try {
            // Transformation specifies algortihm, mode of operation and padding
            c = Cipher.getInstance(ECB_ALGO_TRANSFORMATION_STRING);
        } catch (NoSuchAlgorithmException noSuchAlgoExc) {
            System.out.println(
                    "Exception while encrypting. Algorithm being requested is not available in this environment "
                            + noSuchAlgoExc);
            System.exit(1);
        } catch (NoSuchPaddingException noSuchPaddingExc) {
            System.out.println(
                    "Exception while encrypting. Padding Scheme being requested is not available this environment "
                            + noSuchPaddingExc);
            System.exit(1);
        }

        try {
            c.init(opmode, aesKey);
        } catch (InvalidKeyException invalidKeyExc) {
            System.out.println(
                    "Exception while encrypting. Key being used is not valid. It could be due to invalid encoding, wrong length or uninitialized "
                            + invalidKeyExc);
            System.exit(1);
        }

        return c;
    }
    
    public static Cipher initCBCCipher(int opmode, SecretKey aesKey, byte[] iv) {
        Cipher c = null;

        // Initialize GCM Parameters
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

        try {
            // Transformation specifies algortihm, mode of operation and padding
            c = Cipher.getInstance(CBC_ALGO_TRANSFORMATION_STRING);
        } catch (NoSuchAlgorithmException noSuchAlgoExc) {
            System.out.println(
                    "Exception while encrypting. Algorithm being requested is not available in this environment "
                            + noSuchAlgoExc);
            System.exit(1);
        } catch (NoSuchPaddingException noSuchPaddingExc) {
            System.out.println(
                    "Exception while encrypting. Padding Scheme being requested is not available this environment "
                            + noSuchPaddingExc);
            System.exit(1);
        }

        try {
            c.init(opmode, aesKey, ivParameterSpec);
        } catch (InvalidKeyException invalidKeyExc) {
            System.out.println(
                    "Exception while encrypting. Key being used is not valid. It could be due to invalid encoding, wrong length or uninitialized "
                            + invalidKeyExc);
            System.exit(1);
        } catch (InvalidAlgorithmParameterException invalidAlgoParamExc) {
            System.out.println("Exception while encrypting. Algorithm parameters being specified are not valid "
                    + invalidAlgoParamExc);
            System.exit(1);
        }

        return c;
    }
    
    public static Cipher initCTRCipher(int opmode, SecretKey aesKey, byte[] iv) {
        Cipher c = null;

        // Initialize CTR Parameters
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

        try {
            // Transformation specifies algortihm, mode of operation and padding
            c = Cipher.getInstance(CTR_ALGO_TRANSFORMATION_STRING);
        } catch (NoSuchAlgorithmException noSuchAlgoExc) {
            System.out.println(
                    "Exception while encrypting. Algorithm being requested is not available in this environment "
                            + noSuchAlgoExc);
            System.exit(1);
        } catch (NoSuchPaddingException noSuchPaddingExc) {
            System.out.println(
                    "Exception while encrypting. Padding Scheme being requested is not available this environment "
                            + noSuchPaddingExc);
            System.exit(1);
        }

        try {
            c.init(opmode, aesKey, ivParameterSpec);
        } catch (InvalidKeyException invalidKeyExc) {
            System.out.println(
                    "Exception while encrypting. Key being used is not valid. It could be due to invalid encoding, wrong length or uninitialized "
                            + invalidKeyExc);
            System.exit(1);
        } catch (InvalidAlgorithmParameterException invalidAlgoParamExc) {
            System.out.println("Exception while encrypting. Algorithm parameters being specified are not valid "
                    + invalidAlgoParamExc);
            System.exit(1);
        }

        return c;
    }
    
    public static Cipher initCFBCipher(int opmode, SecretKey aesKey, byte[] iv) {
        Cipher c = null;

        // Initialize CTR Parameters
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

        try {
            // Transformation specifies algortihm, mode of operation and padding
            c = Cipher.getInstance(CTR_ALGO_TRANSFORMATION_STRING);
        } catch (NoSuchAlgorithmException noSuchAlgoExc) {
            System.out.println(
                    "Exception while encrypting. Algorithm being requested is not available in this environment "
                            + noSuchAlgoExc);
            System.exit(1);
        } catch (NoSuchPaddingException noSuchPaddingExc) {
            System.out.println(
                    "Exception while encrypting. Padding Scheme being requested is not available this environment "
                            + noSuchPaddingExc);
            System.exit(1);
        }

        try {
            c.init(opmode, aesKey, ivParameterSpec);
        } catch (InvalidKeyException invalidKeyExc) {
            System.out.println(
                    "Exception while encrypting. Key being used is not valid. It could be due to invalid encoding, wrong length or uninitialized "
                            + invalidKeyExc);
            System.exit(1);
        } catch (InvalidAlgorithmParameterException invalidAlgoParamExc) {
            System.out.println("Exception while encrypting. Algorithm parameters being specified are not valid "
                    + invalidAlgoParamExc);
            System.exit(1);
        }

        return c;
    }
    
    public static void encryptFile(File inputFile, File outputFile, Cipher cipher) throws Exception {
        FileInputStream fis = new FileInputStream(inputFile);
        
        FileOutputStream fos = new FileOutputStream(outputFile);
        CipherOutputStream cos = new CipherOutputStream(fos, cipher);
        
        int bufferSize = (int) inputFile.length();
        System.out.println("File size: " + bufferSize);
        
        IOUtils.copyLarge(fis, cos, 0, bufferSize); // changing buffer size doesn't help slow time with big file
        
        fis.close();
        cos.close();
        fos.close();
    }
    
    public static void decryptFile(File inputFile, File outputFile, Cipher cipher) throws Exception {
        FileInputStream fis = new FileInputStream(inputFile);
        CipherInputStream cis = new CipherInputStream(fis, cipher);
        
        FileOutputStream fos = new FileOutputStream(outputFile);

        int bufferSize = (int) inputFile.length();
        System.out.println("File size: " + bufferSize);
        
        IOUtils.copyLarge(cis, fos, 0, bufferSize); // changing buffer size doesn't help slow time with big file

        cis.close();
        fis.close();
        fos.close();
    }
    
    public static void doBufferedCrypto(File inputFile, File outputFile, Cipher cipher, boolean encrypt) throws Exception {
//        FileInputStream fis = new FileInputStream(inputFile);
        
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
        
        System.out.println("Reading file...");

        byte[] ibuffer = new byte[BUFFER_SIZE];
        byte[] obuffer = new byte[cipher.getOutputSize(ibuffer.length)];

        // If decrypting we got to do this opposite, I think...
        if(!encrypt){
            obuffer = new byte[BUFFER_SIZE];
        }
        byte[] tag = new byte[TAG_BIT_LENGTH];
//        byte[] obuffer = new byte[BUFFER_SIZE];
        
        System.out.println("file size: "+inputFile.length());
        
//        System.out.println("ibuff size: "+ibuffer.length+", obuff size: "+obuffer.length);
        
        System.out.println("Input Mapped Buffer limit: "+inputMappedByteBuffer.limit());
        System.out.println("Output Mapped Buffer limit: "+outputMappedByteBuffer.limit());

        System.out.println("Input Mapped Buffer capacity: "+inputMappedByteBuffer.capacity());
        System.out.println("Output Mapped Buffer capacity: "+outputMappedByteBuffer.capacity());

        System.out.println("Input Buffer: "+ibuffer.length);
        System.out.println("Output Buffer: "+obuffer.length);

        int offset = 0;

        while (inputMappedByteBuffer.hasRemaining()) {
//            System.out.println("mappedByteBuffer position: "+inputMappedByteBuffer.position());
//            System.out.println("remaining: "+inputMappedByteBuffer.remaining());
            if(inputMappedByteBuffer.remaining() <= BUFFER_SIZE){
//                inputMappedByteBuffer.get(ibuffer, 0, inputMappedByteBuffer.remaining());
//                
////                System.out.print("Read: ");System.out.write(ibuffer);System.out.print("\n");
                System.out.println("Last Read");
                inputMappedByteBuffer.get(ibuffer, 0, inputMappedByteBuffer.remaining());
                obuffer = new byte[inputMappedByteBuffer.remaining()];
                offset = cipher.update(ibuffer, 0, inputMappedByteBuffer.remaining(), obuffer, 0);
//                obuffer = cipher.update(ibuffer, 0, inputMappedByteBuffer.remaining());
//                cipher.doFinal(inputMappedByteBuffer, outputMappedByteBuffer);
            } else {
//                mappedByteBuffer.get(ibuffer, 0, BUFFER_SIZE);
//                
//                System.out.print("Read: ");System.out.write(ibuffer);System.out.print("\n");
                System.out.println("Read ");
                inputMappedByteBuffer.get(ibuffer, 0, BUFFER_SIZE);

                offset = cipher.update(ibuffer, 0, BUFFER_SIZE, obuffer, 0);
//                obuffer = cipher.update(ibuffer, 0, BUFFER_SIZE);
//                cipher.update(inputMappedByteBuffer, outputMappedByteBuffer);
            }
//            System.out.println("Writing: ");System.out.write(obuffer);System.out.print("\n");
            System.out.println("Write "+obuffer.length);
            System.out.println("Remaining input "+inputMappedByteBuffer.remaining());
            System.out.println("Remaining output "+outputMappedByteBuffer.remaining());
//            fos.write(obuffer);
            outputMappedByteBuffer.put(obuffer);
//            System.out.println("hasRemaining: "+inputMappedByteBuffer.hasRemaining());
        }

        if(encrypt) {
            cipher.doFinal(tag, 0);
        }else{
            cipher.update(tag, 0, tag.length, obuffer, offset);
            outputMappedByteBuffer.put(obuffer);
            cipher.doFinal(tag, offset);
        }
        outputMappedByteBuffer.put(obuffer);
        
        inputMappedByteBuffer.clear(); // do something with the data and clear/compact it.
        inputFileChannel.close();
        outputMappedByteBuffer.clear(); // do something with the data and clear/compact it.
        outputFileChannel.close();
        
//        System.out.println("File size: " + inputFile.length());
//        
//        byte[] ibuffer = new byte[BUFFER_SIZE];
//        byte[] obuffer = new byte[cipher.getOutputSize(ibuffer.length)];
//        
//        // TODO: try to use ByteBuffer and memory mapped files
//        
//        int readin = 0;
//        while( (readin = fis.read(ibuffer)) > 0 ) {
//            obuffer = cipher.update(ibuffer, 0, readin);
//            
//            fos.write(obuffer);
//            
//            readin = fis.read(ibuffer);
//        }
//        obuffer = cipher.doFinal();
//        fos.write(obuffer);
//        
//        fis.close();
//        fos.close();
    }

    public static void doBufferedCrypto2(File inputFile, File outputFile, Cipher cipher) throws Exception {
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
    
    // TODO: try to use Bouncy Castle
    
    public static void doFullFileCrypto(File inputFile, File outputFile, Cipher cipher) throws Exception {
        FileInputStream fis = new FileInputStream(inputFile);

        FileOutputStream fos = new FileOutputStream(outputFile);
        
        System.out.println("File size: " + inputFile.length());
        
        byte[] ibuffer = new byte[(int) inputFile.length()];
        fis.read(ibuffer);
        byte[] obuffer = cipher.doFinal(ibuffer);
        
        fos.write(obuffer);
        
        fis.close();
        fos.close();
    }
}
