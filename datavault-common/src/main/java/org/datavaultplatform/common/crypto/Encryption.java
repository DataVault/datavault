package org.datavaultplatform.common.crypto;

import com.bettercloud.vault.SslConfig;
import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultConfig;
import com.bettercloud.vault.VaultException;
import com.bettercloud.vault.json.Json;
import com.bettercloud.vault.json.JsonArray;
import com.bettercloud.vault.json.JsonObject;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.apache.commons.lang3.StringUtils;
import org.datavaultplatform.common.task.Context;
import org.datavaultplatform.common.task.Context.AESMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.EnumSet;
import jakarta.annotation.PostConstruct;
import java.security.Security;
import java.security.Provider;
import org.springframework.util.Assert;

import com.google.common.base.Splitter;
import lombok.SneakyThrows;
import org.apache.commons.codec.digest.DigestUtils;

public class Encryption {

    private static final Logger logger = LoggerFactory.getLogger(Encryption.class);

    public static final int BUFFER_SIZE = 50 * 1024; // 50KB
    public static final int SMALL_BUFFER_SIZE = 1024; // 1KB
    public static final int AES_BLOCK_SIZE = 16; // 16 Bytes
    public static final int AES_KEY_SIZE = 256;
    public static final int IV_SIZE = 96;
    public static final int IV_CBC_SIZE = 16;
    public static final int TAG_BIT_LENGTH = 128;
    public static final String GCM_ALGO_TRANSFORMATION_STRING = "AES/GCM/NoPadding";
    public static final String CBC_ALGO_TRANSFORMATION_STRING = "AES/CBC/PKCS5Padding";
    public static final String CTR_ALGO_TRANSFORMATION_STRING = "AES/CTR/PKCS5Padding";
    public static final String CCM_ALGO_TRANSFORMATION_STRING = "AES/CCM/NoPadding";
    private static final String KEYSTORE_TYPE = "JCEKS";

    private static int encBufferSize = SMALL_BUFFER_SIZE;

    private static boolean vaultEnable;
    private static String vaultAddress;
    private static String vaultToken;
    private static String vaultKeyPath;
    private static String vaultDataEncryptionKeyName;
    private static String vaultPrivateKeyEncryptionKeyName;
    private static String vaultSslPEMPath;

    private static boolean keystoreEnable;
    private static String keystorePath;
    private static String keystorePassword;

    private static Vault vault = null;

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
        // Specifying algorithm key will be used for
        KeyGenerator keygen = KeyGenerator.getInstance("AES");
        // Specifying Key size to be used, Note: This would need JCE Unlimited
        // Strength to be installed explicitly
        keygen.init(key_size);
        SecretKey aesKey = keygen.generateKey();

        return aesKey;
    }

    /**
     * Generate an Initialisation Vector using default size (i.e. Encryption.IV_SIZE)
     *
     * @return Initialisation Vector
     */
    public static byte[] generateIV(){
        return generateIV(IV_SIZE);
    }

    /**
     * Generate an Initialisation Vector
     *
     * @param size in bytes for the iv
     * @return Initialisation Vector
     */
    public static byte[] generateIV(int size) {
        byte[] iv = new byte[size];
        SecureRandom secRandom = new SecureRandom();
        secRandom.nextBytes(iv); // SecureRandom initialized using self-seeding
        return iv;
    }

    public static Cipher initGCMCipher(String keyName, int opmode, byte[] iv) throws Exception{
        return initGCMCipher(opmode, Encryption.getSecretKey(keyName), iv, null);
    }

    public static Cipher initGCMCipher(int opmode, SecretKey aesKey, byte[] iv) throws Exception{
        return initGCMCipher(opmode, aesKey, iv, null);
    }

    /**
     * Initialise an AES-GCM Cipher with Bouncy Castle Provider
     *
     * GCM is a very fast but arguably complex combination of CTR mode and GHASH,
     * a MAC over the Galois field with 2^128 elements.
     * Its wide use in important network standards like TLS 1.2 is reflected
     * by a special instruction Intel has introduced to speed up the calculation of GHASH.
     *
     * @param opmode -
     * @param aesKey - secret key
     * @param iv - Initialisation Vector
     * @param aadData - additional authenticated data (optional)
     * @return
     */
    public static Cipher initGCMCipher(int opmode, SecretKey aesKey, byte[] iv, byte[] aadData) throws Exception {

        // Initialize GCM Parameters
        GCMParameterSpec gcmParamSpec = new GCMParameterSpec(TAG_BIT_LENGTH, iv);

        // Transformation specifies algorithm, mode of operation and padding
        Cipher c = Cipher.getInstance(GCM_ALGO_TRANSFORMATION_STRING, "BC");

        c.init(opmode, aesKey, gcmParamSpec, new SecureRandom());


        if (aadData != null) {
            c.updateAAD(aadData); // add AAD tag data before encrypting
        }

        return c;
    }

    public static Cipher initCBCCipher(String keyName, int opmode, byte[] iv) throws Exception {
        return initCBCCipher(opmode, Encryption.getSecretKey(keyName), iv);
    }

    /**
     * Initialise an AES-CBC Cipher
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

        // Initialize Parameters
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

        // Transformation specifies algorithm, mode of operation and padding
        Cipher c = Cipher.getInstance(CBC_ALGO_TRANSFORMATION_STRING);

        c.init(opmode, aesKey, ivParameterSpec);

        return c;
    }


    /**
     * Perform crypto using a 1024 Bytes buffer.
     * Depending on the Cipher provided will perform Encryption or Decryption.
     *
     * @param inputFile
     * @param outputFile
     * @param cipher
     * @throws Exception
     */
    public static void doByteBufferFileCrypto(File inputFile, File outputFile, Cipher cipher) throws Exception {

        try (InputStream is = new FileInputStream(inputFile);
            OutputStream os = new CipherOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile)), cipher)) {
            logger.info("starting crypto copy from [{}] to [{}] ", inputFile, outputFile);
            IOUtils.copy(is, os);
            logger.info("finished crypto from [{}] to [{}] ", inputFile, outputFile);
        }
        logger.info("Converted input[{}/{}]output[{}/{}]", inputFile, inputFile.length(),
            outputFile, outputFile.length());
    }

    @Deprecated
    public static void doStreamFileCrypto(File inputFile, File outputFile, Cipher cipher) throws Exception {
        FileInputStream fis = new FileInputStream(inputFile);

        FileOutputStream fos = new FileOutputStream(outputFile);
        CipherOutputStream cos = new CipherOutputStream(fos, cipher);

        byte[] buffer = new byte[encBufferSize];
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

    private static SecretKey getSecretKey(String keyName) throws Exception {
        if(Encryption.getVaultEnable()){
            return Encryption.getSecretKeyFromVault(keyName);
        } else if(Encryption.getKeystoreEnable()){
            return Encryption.getSecretKeyFromKeyStore(keyName);
        } else {
            throw new Exception("ERROR: Neither Hashicorp Vault or Keystore has been enabled.");
        }
    }

    private static SecretKey getSecretKeyFromVault(String keyName) throws Exception {
        if(vault == null) {
            setVault();
        }

        logger.debug("get secret key: "+getVaultKeyPath()+" "+keyName);

//        String encodedKey = vault.logical().read(context.getVaultKeyPath()).getData().get(context.getVaultKeyName());

        final String jsonString = new String(
                vault.logical().read(getVaultKeyPath()).getRestResponse().getBody(),
            StandardCharsets.UTF_8);

//        logger.debug("jsonString: " + jsonString);

        final JsonObject jsonObject = Json.parse(jsonString).asObject();


        final JsonObject jsonDataObject = jsonObject.get("data").asObject().get("data").asObject();
//        logger.debug("jsonDataObject: " + jsonDataObject.toString());

        String encodedKey = jsonDataObject.get(keyName).asString();

        logger.debug("encodedKey received: "+encodedKey);

        // decode the base64 encoded string
        byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
        // rebuild key using SecretKeySpec
        SecretKey secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");

        return secretKey;
    }

    private static void setVault() throws VaultException {
        final VaultConfig vaultConfig = new VaultConfig()
                .address(getVaultAddress())
                .token(getVaultToken());

        logger.info("Vault PEM path: '"+getVaultSslPEMPath()+"'");
        logger.info("context.getVaultSslPEMPath().trim().equals(\"\"): "+getVaultSslPEMPath().equals(""));

        if(getVaultSslPEMPath().trim().equals("")) {
            logger.debug("Won't use SSL Certificate.");
        } else {
            logger.debug("Use PEM file: '"+getVaultSslPEMPath().trim()+"'");
            vaultConfig.sslConfig(new SslConfig()
                        .pemFile(new File(getVaultSslPEMPath().trim())
                    ).build());
        }
        vaultConfig.build();

        logger.debug("Vault address: "+vaultConfig.getAddress());
//        logger.debug("Vault token: "+vaultConfig.getToken()); // we should not display token in logs
        logger.debug("Vault Max Retries: "+vaultConfig.getMaxRetries());
        logger.debug("Vault Retry Interval: "+vaultConfig.getRetryIntervalMilliseconds());
        logger.debug("Vault Retry Open Timeout: "+vaultConfig.getOpenTimeout());
        logger.debug("Vault Retry Read Timeout: "+vaultConfig.getReadTimeout());

        vault = new Vault(vaultConfig);
    }

    public static Vault getVault() {
        return vault;
    }

    public static byte[] encryptFile(Context context, File file)  throws Exception {
        return encryptFile(context.getEncryptionMode(), file);
    }
    /**
     * Perform encryption on file
     *
     * @param aesMode - the aes encryption mode
     * @param file - file to be encrypted
     * @return generated IV
     * @throws Exception
     */
    public static byte[] encryptFile(AESMode aesMode, File file)  throws Exception {
        return doCrypto(aesMode, file, Cipher.ENCRYPT_MODE, null);
    }

    public static void decryptFile(Context context, File file, byte[] iv)  throws Exception {
        decryptFile(context.getEncryptionMode(), file, iv);
    }
    /**
     * Perform decryption on file
     * @param aesMode - the aes encryption mode
     * @param file - encrypted file
     * @param iv - Initialisation Vector used for the encryption
     * @throws Exception
     */
    public static void decryptFile(AESMode aesMode, File file, byte[] iv)  throws Exception {
        doCrypto(aesMode, file, Cipher.DECRYPT_MODE, iv);
    }

    public static String getDigestForIv(byte[] iv) {
        StringBuilder digest = new StringBuilder();
        digest.append(iv.length);
        digest.append("-");

        // We use md5 here because it's short. We don't need a secure hash
        String md5 = String.join("-",
            Splitter.fixedLength(5).splitToList(DigestUtils.md5Hex(iv)));

        digest.append(md5);
        return digest.toString();
    }

    private static byte[] doCrypto(AESMode aesMode, File file, int encryptMode, byte[] iv) throws Exception {

        if(encryptMode == Cipher.ENCRYPT_MODE) {
            // Generating IV
            iv = Encryption.generateIV(Encryption.IV_SIZE);
        } else {
            String ivDigest = getDigestForIv(iv);
            String base64iv = Base64.getEncoder().encodeToString(iv);
            logger.info("Decrypting [{}] using iv-byte[] with digest [{}]/bas64[{}]", file, ivDigest, base64iv);
        }

        final Cipher cipher;
        switch (aesMode) {
            case CBC:
                cipher = Encryption.initCBCCipher(getVaultDataEncryptionKeyName(), encryptMode, iv); break;
            case GCM:
            default:
                cipher = Encryption.initGCMCipher(getVaultDataEncryptionKeyName(), encryptMode, iv); break;
        }

        File tempFile = new File(file.getAbsoluteFile() + ".temp");
        String action = encryptMode == Cipher.ENCRYPT_MODE ? "encrypting" : "decrypting";
        logger.info("{} chunk: [{}][{}]bytes", action, file.getName(), file.length());
        try {
            long before = file.length();
            Encryption.doByteBufferFileCrypto(file, tempFile, cipher);
            long expected = encryptMode == Cipher.ENCRYPT_MODE ? before + 16 : before - 16;
            long actual = tempFile.length();
            if (actual != expected) {
                logger.warn("Problem:{}:[{}]expected[{}]got[{}]", action, file, expected, actual);
            }
        } catch (Exception ex) {
            String msg = "Error while " + action + " file: " + file.getName();
            throw new CryptoException(msg, ex);
        }

        logger.info("Action[{}]Before[{}/{}]After[{}/{}]", action, file, file.length(), tempFile, tempFile.length());

        // todo : move this out of here and do it for all chunks after the encryption stage in order to allow the
        // todo : whole step to be restarted in a future ideal world.
        FileUtils.deleteQuietly(file);
        FileUtils.moveFile(tempFile, file);

        return iv;
    }

    public static byte[] encryptSecret(String privateKey, byte[] iv) throws Exception {
        return encryptSecret(privateKey, null, iv);
    }

    public static byte[] encryptSecret(String secret, SecretKey secretKey, byte[] iv) throws Exception {
        Cipher cipher;
        if(secretKey == null) {
            cipher = Encryption.initGCMCipher(getVaultPrivateKeyEncryptionKeyName(), Cipher.ENCRYPT_MODE, iv);
        }
        else {
            cipher = Encryption.initGCMCipher(Cipher.ENCRYPT_MODE, secretKey, iv);
        }

        return cipher.doFinal(secret.getBytes());
    }

    public static byte[] decryptSecret(byte[] privateKey, byte[] iv) throws Exception {
        return decryptSecret(privateKey, iv, null);
    }

    public static byte[] decryptSecret(byte[] encryptedSecret, byte[] iv, SecretKey secretKey) throws Exception {
        Cipher cipher;
        if(secretKey == null) {
            cipher = Encryption.initGCMCipher(getVaultPrivateKeyEncryptionKeyName(), Cipher.DECRYPT_MODE, iv);
        }
        else {
            cipher = Encryption.initGCMCipher(Cipher.DECRYPT_MODE, secretKey, iv);
        }

        return cipher.doFinal(encryptedSecret);
    }

    /**
     * Save the AES secret key in a JCEKS KeyStore
     *
     * @param alias - name of the key
     * @param secretKey - the secret key value
     * @throws Exception
     */
    public static void saveSecretKeyToKeyStore(String alias, SecretKey secretKey) throws Exception {
        KeyStore ks = KeyStore.getInstance(KEYSTORE_TYPE);
        try (FileInputStream fis = new FileInputStream(Encryption.getKeystorePath())) {
            ks.load(fis, Encryption.getKeystorePassword().toCharArray());
        } catch ( FileNotFoundException fnfe ) {
            ks.load(null, Encryption.getKeystorePassword().toCharArray());
        }
        KeyStore.ProtectionParameter protParam = new KeyStore.PasswordProtection(Encryption.getKeystorePassword().toCharArray());
        KeyStore.SecretKeyEntry skEntry = new KeyStore.SecretKeyEntry(secretKey);
        ks.setEntry(alias, skEntry, protParam);
        try (FileOutputStream fos = new FileOutputStream(Encryption.getKeystorePath())) {
            ks.store(fos, Encryption.getKeystorePassword().toCharArray());
        }
    }

    public static SecretKey getSecretKeyFromKeyStore(String alias) throws Exception {
        KeyStore ks = KeyStore.getInstance(KEYSTORE_TYPE);
        try (FileInputStream fis = new FileInputStream(Encryption.getKeystorePath())) {
            ks.load(fis, Encryption.getKeystorePassword().toCharArray());
        }
        final SecretKey secretKey;

        if (alias == null) {
            secretKey = null;
        } else {
            secretKey = (SecretKey) ks.getKey(alias,
                Encryption.getKeystorePassword().toCharArray());
        }

        Assert.isTrue(secretKey != null, () -> String.format("No key found in keystore[%s] for KeyName[%s]", Encryption.getKeystorePath(), alias));
        logger.info("found non-null SecretKey for key-alias [{}]", alias);
        return secretKey;
    }

    public static String getVaultAddress() {
        return vaultAddress;
    }

    public static int getEncBufferSize() { return encBufferSize; }

    public void setEncBufferSize(String encBufferSize) {
        int bytes = Math.toIntExact(org.datavaultplatform.common.io.FileUtils.parseFormattedSizeToBytes(encBufferSize));
        Encryption.encBufferSize = bytes;
    }

    public void setVaultAddress(String vaultAddress) {
        Encryption.vaultAddress = vaultAddress;
    }

    public static String getVaultToken() {
        return vaultToken;
    }

    public void setVaultToken(String vaultToken) {
        Encryption.vaultToken = vaultToken;
    }

    public static String getVaultKeyPath() {
        return vaultKeyPath;
    }

    public void setVaultKeyPath(String vaultKeyPath) {
        Encryption.vaultKeyPath = vaultKeyPath;
    }

    public static String getVaultSslPEMPath() {
        return vaultSslPEMPath;
    }

    public void setVaultSslPEMPath(String vaultSslPEMPath) {
        Encryption.vaultSslPEMPath = vaultSslPEMPath;
    }


    public static String getVaultDataEncryptionKeyName() {
        return vaultDataEncryptionKeyName;
    }

    public void setVaultDataEncryptionKeyName(String vaultDataEncryptionKeyName) {
        Encryption.vaultDataEncryptionKeyName = vaultDataEncryptionKeyName;
    }

    public static String getVaultPrivateKeyEncryptionKeyName() {
        return vaultPrivateKeyEncryptionKeyName;
    }



    public void setVaultPrivateKeyEncryptionKeyName(String vaultPrivateKeyEncryptionKeyName) {
        Encryption.vaultPrivateKeyEncryptionKeyName = vaultPrivateKeyEncryptionKeyName;
    }

    public static boolean getVaultEnable() {
        return vaultEnable;
    }

    public void setVaultEnable(boolean isEnabled) {
        vaultEnable = isEnabled;
    }

    public static boolean getKeystoreEnable() {
        return keystoreEnable;
    }

    public void setKeystoreEnable(boolean isEnabled) {
        keystoreEnable = isEnabled;
    }

    public static String getKeystorePath() {
        return keystorePath;
    }

    public void setKeystorePath(String path) {
        staticSetKeystorePath(path);
    }

    public static void staticSetKeystorePath(String path) {
        keystorePath = path;
        if (keystorePath == null) {
            logger.warn("KeyStore path is null.");
            return;
        }
        try {
            File file = new File(keystorePath);
            if (!file.exists()) {
                logger.warn("KeyStore[{}] does not exist.", keystorePath);
                return;
            }
            if (!file.canRead()) {
                logger.warn("KeyStore[{}] is not readable.", keystorePath);
                return;
            }
            try (FileInputStream fis = new FileInputStream(file)) {
                String sha1 = DigestUtils.sha1Hex(fis).toLowerCase();
                PosixFileAttributeView view = Files.getFileAttributeView(file.toPath(), PosixFileAttributeView.class);
                PosixFileAttributes attrs = view.readAttributes();

                long size = attrs.size();
                logger.info("KeyStore[{}] size[{}].", file.getCanonicalPath(), size);

                FileTime creationTime = attrs.creationTime();
                FileTime lastModifiedTime = attrs.lastModifiedTime();
                FileTime lastAccessTime = attrs.lastAccessTime();

                logger.info("KeyStore[{}] creationTime    [{}]", file.getCanonicalPath(), creationTime);
                logger.info("KeyStore[{}] lastModifiedTime[{}]", file.getCanonicalPath(), lastModifiedTime);
                logger.info("KeyStore[{}] lastAccessTime  [{}]", file.getCanonicalPath(), lastAccessTime);

                logger.info("KeyStore[{}] SHA-1[{}]", file.getCanonicalPath(), sha1);
            }
        } catch (Exception ex) {
            String msg = String.format("Problem getting SHA-1 digest of [%s].", keystorePath);
            logger.error(msg, ex);
        }
    }

    public static String getKeystorePassword() {
        return keystorePassword;
    }

    public void setKeystorePassword(String password) {
        keystorePassword = password;
    }

    public static void staticSetKeystorePassword(String password) {
        keystorePassword = password;
    }

    /**
     * Allow running encryption method outside of the app.
     * @param args a string array of arguments to the main class, should be the name of the method.
     */
    public static void main(String [] args) {
        addBouncyCastleSecurityProvider();
        String methodName = args[0];

        if(methodName.equals("generateSecretKey")){
            SecretKey key = null;
            try {
                key = Encryption.generateSecretKey();
            } catch (NoSuchAlgorithmException e) {
                logger.error("unexpected exception",e);
                System.exit(1);
            }
            String encodedKey = Base64.getEncoder().encodeToString(key.getEncoded());
            logger.info(encodedKey);
        }
        if(methodName.equals("generateSecretKeyAndAddToJCEKS")){
            String jsonFileName = args[1];
            generateSecretKeyAndAddToJCEKS(jsonFileName);
        }
    }

    static void generateSecretKeyAndAddToJCEKS(String keyStoreFileName) {
        try {
            KeyStoreInfo keyStoreInfo = extractKeyStoreInfo(keyStoreFileName);
            generateSecretKeyAndAddToJCEKS(keyStoreInfo);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    static KeyStoreInfo extractKeyStoreInfo(String keyStoreFileName) throws Exception {
        try (FileReader jsonReader = new FileReader(keyStoreFileName)) {
            JsonObject jsonObject = Json.parse(jsonReader).asObject();
            String path = jsonObject.get("path").asString();
            String password = jsonObject.get("password").asString();
            JsonArray aliasesRaw = jsonObject.get("key_aliases").asArray();
            List<String> aliases = new ArrayList<>();
            aliasesRaw.forEach(jValue -> aliases.add(jValue.asString()));
            return KeyStoreInfo.builder()
                .path(path)
                .password(password)
                .aliases(aliases)
                .build();
        }
    }

    static void generateSecretKeyAndAddToJCEKS(KeyStoreInfo info) throws Exception {
        Encryption.staticSetKeystorePath(info.getPath());
        Encryption.staticSetKeystorePassword(info.getPassword());
        List<String> aliases = info.getAliases();
        SecretKey[] keys = new SecretKey[aliases.size()];
        for(int i = 0; i < aliases.size(); i++) {
            String alias = aliases.get(i);
            logger.info("Creating " + alias + " to " + Encryption.getKeystorePath());
            keys[i] = Encryption.generateSecretKey();
            Encryption.saveSecretKeyToKeyStore(alias, keys[i]);
        }

        for(int i = 0; i < aliases.size(); i++) {
            String alias = aliases.get(i);
            SecretKey returnKey = Encryption.getSecretKeyFromKeyStore(alias);
            if (!returnKey.equals(keys[i])) {
                System.err.println("ERROR! The " + alias + " key return by KeyStore is different!");
            }
            String encodedKey = Base64.getEncoder().encodeToString(keys[i].getEncoded());
            logger.info(alias + ": " + encodedKey);
        }
    }

    @PostConstruct
    public static void addBouncyCastleSecurityProvider() {
        logger.info("Adding Bouncy Castle Provider.");
        Provider[] before = Security.getProviders();
        int result = Security.addProvider(new BouncyCastleProvider());
        if (result == -1) {
            logger.warn("BouncyCastle already added!");
        }
        Provider[] after = Security.getProviders();
        logger.info("before[{}] result[{}] after[{}]", before.length, result, after.length);
        String bcVersion = BouncyCastleProvider
            .class
            .getPackage()
            .getImplementationVersion();
        logger.info("Added Bouncy Castle Provider [{}].", bcVersion);
        checkKeyNamesAreNotSame();
        logKeyDigests();
        initialised = true;
    }

    private static boolean initialised = false;

    public static boolean isInitialised() {
        return initialised;
    }

    public static void checkKeyNamesAreNotSame() {
        String keyNameData = Encryption.getVaultDataEncryptionKeyName();
        if (StringUtils.isNotBlank(keyNameData)) {
            String keyNamePrivateKey = Encryption.getVaultPrivateKeyEncryptionKeyName();
            // we are only concerned when Not Blank AND the same
            if (keyNameData.equalsIgnoreCase(keyNamePrivateKey)) {
                logger.warn("The two encryption key names are the same - [{}]", keyNameData);
            }
        }
    }

    @Data
    @Builder
    static class KeyStoreInfo {
        final String path;
        final String password;
        final List<String> aliases;
    }

    public static void logKeyDigests() {
        if (!keystoreEnable && !vaultEnable) {
            logger.warn("no vault or keystore enabled");
            return;
        }
        String nameData = Encryption.getVaultDataEncryptionKeyName();
        String digestData = getDigestForDataEncryptionKey();
        logger.info("key[Data]name[{}]digest[{}]", nameData, digestData);

        String nameSSH  = Encryption.getVaultPrivateKeyEncryptionKeyName();
        String digestSSH  = getDigestForSSHEncryptionKey();
        logger.info("key[SSH ]name[{}]digest[{}]", nameSSH, digestSSH);

        logKeyStoreCreationDates();
    }

    @SneakyThrows
    public static String getDigestForDataEncryptionKey() {
        return getDigestForKeyName(getVaultDataEncryptionKeyName());
    }

    @SneakyThrows
    public static String getDigestForSSHEncryptionKey() {
        return getDigestForKeyName(getVaultPrivateKeyEncryptionKeyName());
    }

    private static String getDigestForKeyName(String name){
        if (name == null) {
            return null;
        }
        try {
            SecretKey key = getSecretKey(name);
            return getKeyDigest(key);
        } catch(Exception ex) {
            logger.warn("Problem getting digest for key name [{}]message[{}]", name, ex.getMessage());
            return null;
        }
    }
    /*
     * An example KeyDigest would be formatted :  '10A94-34EDB-45662-CD92F-BEB02-29CCC-09145-0122F'
     * This digest is only meant for information purposes. To allow humans a way of checking
     * which keys are actually being used.
     * For extra security - we only use "part" of the base16 value of the key for the digest.
     */
    public static String getKeyDigest(SecretKey key) {
        String encoded =  new BigInteger(1, key.getEncoded()).toString(16).toUpperCase();
        String digest = encoded.substring(0,40);
        String readableDigest = String.join("-",
            Splitter.fixedLength(5).splitToList(digest));
        return readableDigest;
    }

    @SneakyThrows
    private static KeyStore loadKeyStore() {
        KeyStore ks = KeyStore.getInstance(KEYSTORE_TYPE);
        try (FileInputStream fis = new FileInputStream(Encryption.getKeystorePath())) {
            ks.load(fis, Encryption.getKeystorePassword().toCharArray());
        } catch ( FileNotFoundException fnfe ) {
            ks.load(null, Encryption.getKeystorePassword().toCharArray());
        }
        return ks;
    }

    private static void updateMapWithCreationDate(String keyName, KeyStore ks, Map<String,Date> keyNameToDateMap){
        try {
            if (keyName != null && ks.containsAlias(keyName)) {
                Date date = ks.getCreationDate(keyName);
                logger.info("Encryption Alias[{}]CreationDate[{}]", keyName,
                    date);
                keyNameToDateMap.put(keyName, date);
            } else {
                logger.warn("No Key for [{}]", keyName);
            }
        } catch (Exception ex) {
            logger.warn("Unable to get Creation Date for key [{}]", keyName, ex);
        }
    }

    @SneakyThrows
    private static Set<String> getAliases(KeyStore ks){
        Set<String> result = new TreeSet<>();
        Enumeration<String> eAliases = ks.aliases();
        while(eAliases.hasMoreElements()){
            String alias  = eAliases.nextElement();
            result.add(alias);
        }
        return result;
    }

    @SneakyThrows
    private static void logKeyStoreCreationDates() {
        if(!keystoreEnable){
            return;
        }
        Map<String, Date> result = new HashMap<>();
        String keyStorePath = getKeystorePath();
        try {
            logger.info("Loading keystore from [{}]...", keyStorePath);
            KeyStore ks = loadKeyStore();

            getAliases(ks).forEach(alias -> {
                updateMapWithCreationDate(alias, ks, result);
            });
        } catch (Exception e) {
            logger.warn("ProblemLoadingKeyStoreFile[{}]", getKeystorePath());
        }
    }
}
