package org.datavaultplatform.broker.services;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import com.jcraft.jsch.*;

/**
 * User: Robin Taylor
 * Date: 04/11/2015
 * Time: 09:54
 */
import org.springframework.stereotype.Service;
@Service
public class UserKeyPairService {

    // comment added at the end of public key
    private static final String PUBKEY_COMMENT = "datavault";
    private String passphrase;
    private String privateKey;
    private String publicKey;

    public String getPassphrase() {
        return passphrase;
    }

    public void setPassphrase(String passphrase) {
        this.passphrase = passphrase;
    }

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


    public void generateNewKeyPair() {

        JSch jschClient = new JSch();

        try {
            KeyPair keyPair = KeyPair.genKeyPair(jschClient, KeyPair.RSA);
            OutputStream privKeyBaos = new ByteArrayOutputStream();
            keyPair.writePrivateKey(privKeyBaos, passphrase.getBytes());

            OutputStream pubKeyBaos = new ByteArrayOutputStream();
            keyPair.writePublicKey(pubKeyBaos, PUBKEY_COMMENT);

            setPrivateKey(privKeyBaos.toString());
            setPublicKey(pubKeyBaos.toString());

        } catch (JSchException e) {
            throw new IllegalArgumentException("problem with generating ssh key", e);
        }
    }

}
