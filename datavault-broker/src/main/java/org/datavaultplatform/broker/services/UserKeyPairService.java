package org.datavaultplatform.broker.services;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import com.jcraft.jsch.*;

/**
 * User: Robin Taylor
 * Date: 04/11/2015
 * Time: 09:54
 */
public class UserKeyPairService {

    // comment added at the end of public key
    private static final String PUBKEY_COMMENT = "datavault";
    // Todo : inject this in Spring config, it shouldn't be visible in Github
    private static final String PASSPHRASE = "datavault";
    private String privateKey;
    private String publicKey;

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public static String getPASSPHRASE() {
        return PASSPHRASE;
    }


    public void generateNewKeyPair() {

        JSch jschClient = new JSch();

        try {
            KeyPair keyPair = KeyPair.genKeyPair(jschClient, KeyPair.RSA);
            OutputStream privKeyBaos = new ByteArrayOutputStream();
            keyPair.writePrivateKey(privKeyBaos, PASSPHRASE.getBytes());

            OutputStream pubKeyBaos = new ByteArrayOutputStream();
            keyPair.writePublicKey(pubKeyBaos, PUBKEY_COMMENT);

            setPrivateKey(privKeyBaos.toString());
            setPublicKey(pubKeyBaos.toString());

        } catch (JSchException e) {
            throw new IllegalArgumentException("problem with generating ssh key", e);
        }
    }

}
