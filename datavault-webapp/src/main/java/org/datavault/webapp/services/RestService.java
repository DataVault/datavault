package org.datavault.webapp.services;

import java.util.HashMap;
import java.util.Map;

import org.datavault.common.model.Files;
import org.datavault.common.model.Vault;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

/**
 * User: Robin Taylor
 * Date: 01/05/2015
 * Time: 14:04
 */
public class RestService {


    private String brokerURL;

    public void setBrokerURL(String brokerURL) {
        this.brokerURL = brokerURL;
    }

    public Map<String, String> getFilesListing() {

        RestTemplate restTemplate = new RestTemplate();
        Files files = restTemplate.getForObject(brokerURL + "/files", Files.class);

        return files.getFilesMap();
    }

    public Vault[] getVaultsListing() {

        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<Vault[]> responseEntity = restTemplate.getForEntity(brokerURL + "/vaults", Vault[].class);
        Vault[] vaults = responseEntity.getBody();

        return vaults;
    }

    public Vault getVault(String id) {

        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<Vault> responseEntity = restTemplate.getForEntity(brokerURL + "/vaults/" + id, Vault.class);
        Vault vault = responseEntity.getBody();

        return vault;
    }

    public Vault addVault(Vault vault) {
        RestTemplate restTemplate = new RestTemplate();

        Vault returnedVault = restTemplate.postForObject(brokerURL + "/vaults/", vault , Vault.class);

        return returnedVault;
    }


}
