package org.datavaultplatform.broker.services;

import org.datavaultplatform.common.model.UserKeyPair;
import org.datavaultplatform.common.model.dao.UserKeyPairDAO;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import com.jcraft.jsch.*;

/**
 * User: Robin Taylor
 * Date: 04/11/2015
 * Time: 09:54
 */
public class UserKeyPairService {

    private UserKeyPairDAO userKeyPairDAO;

    // comment added at the end of public key
    private static final String PUBKEY_COMMENT = "datavault";

    // todo: set this somewhere else
    private String passphrase = "datavault";


    public UserKeyPairDAO getUserKeyPairDAO() {
        return userKeyPairDAO;
    }

    public void setUserKeyPairDAO(UserKeyPairDAO userKeyPairDAO) {
        this.userKeyPairDAO = userKeyPairDAO;
    }

    public void addUserKeyPair(UserKeyPair userKeyPair) {
        userKeyPairDAO.save(userKeyPair);
    }

    public UserKeyPair generateNewKeyPair() {

        JSch jschClient = new JSch();

        try {
            KeyPair keyPair = KeyPair.genKeyPair(jschClient, KeyPair.RSA);
            OutputStream privKeyBaos = new ByteArrayOutputStream();
            keyPair.writePrivateKey(privKeyBaos, passphrase.getBytes());

            OutputStream pubKeyBaos = new ByteArrayOutputStream();
            keyPair.writePublicKey(pubKeyBaos, PUBKEY_COMMENT);

            String privateKey = privKeyBaos.toString();
            String publicKey = pubKeyBaos.toString();

            UserKeyPair userKeyPair = new UserKeyPair();
            userKeyPair.setPrivateKey(privateKey);
            userKeyPair.setPublicKey(publicKey);


            return userKeyPair;

        } catch (JSchException e) {
            throw new IllegalArgumentException("problem with generating ssh key", e);
        }
    }

}
