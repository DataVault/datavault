package org.datavaultplatform.worker.operations;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public class Encryption {

    public static int AES_KEY_SIZE = 256;
    public static int IV_SIZE = 96;
    public static int TAG_BIT_LENGTH = 128;
    public static String ALGO_TRANSFORMATION_STRING = "AES/GCM/PKCS5Padding";

    /**
     * Generate a secret key for AES encryption Need JCE Unlimited Strength to
     * be installed explicitly
     * 
     * @return Key secret key
     * @throws NoSuchAlgorithmException
     *             if cryptographic algorithm is not available in the
     *             environment i.e. AES
     */
    public static SecretKey generateSecretKey() throws NoSuchAlgorithmException {
        SecretKey aesKey = null;

        // Specifying algorithm key will be used for
        KeyGenerator keygen = KeyGenerator.getInstance("AES");
        // Specifying Key size to be used, Note: This would need JCE Unlimited
        // Strength to be installed explicitly
        keygen.init(AES_KEY_SIZE);
        aesKey = keygen.generateKey();

        return aesKey;
    }

    public static byte[] generateIV() {
        byte iv[] = new byte[IV_SIZE];
        SecureRandom secRandom = new SecureRandom();
        secRandom.nextBytes(iv); // SecureRandom initialized using self-seeding
        return iv;
    }

    public static byte[] aesEncrypt(String message, SecretKey aesKey, byte[] iv) {
        return aesEncrypt(message, aesKey, iv, null);
    }

    public static byte[] aesEncrypt(String message, SecretKey aesKey, byte[] iv, byte[] aadData) {
        Cipher c = initCipher(Cipher.ENCRYPT_MODE, aesKey, iv, aadData);

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

    public static byte[] aesDecrypt(byte[] encryptedMessage, SecretKey aesKey, byte[] iv) {
        return aesDecrypt(encryptedMessage, aesKey, iv, null);
    }

    public static byte[] aesDecrypt(byte[] encryptedMessage, SecretKey aesKey, byte[] iv, byte[] aadData) {
        Cipher c = initCipher(Cipher.DECRYPT_MODE, aesKey, iv, aadData);

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

    public static Cipher initCipher(int opmode, SecretKey aesKey, byte[] iv, byte[] aadData) {
        Cipher c = null;

        // Initialize GCM Parameters
        GCMParameterSpec gcmParamSpec = new GCMParameterSpec(TAG_BIT_LENGTH, iv);

        try {
            // Transformation specifies algortihm, mode of operation and padding
            c = Cipher.getInstance(ALGO_TRANSFORMATION_STRING);
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
}
