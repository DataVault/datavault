package org.datavault.webapp.services;

import org.datavault.common.model.Deposit;
import org.datavault.common.model.Vault;
import org.datavault.common.model.FileInfo;
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

    public FileInfo[] getFilesListing(String filePath) {

        RestTemplate restTemplate = new RestTemplate();
        
        if (!filePath.startsWith("/")) {
            filePath = "/" + filePath;
        }
        
        ResponseEntity<FileInfo[]> responseEntity = restTemplate.getForEntity(brokerURL + "/files" + filePath, FileInfo[].class);
        FileInfo[] files = responseEntity.getBody();
        
        return files;
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

    public Deposit addDeposit(String vaultId, Deposit deposit) {
        RestTemplate restTemplate = new RestTemplate();

        Deposit returnedDeposit = restTemplate.postForObject(brokerURL + "/vaults/" + vaultId + "/deposits", deposit , Deposit.class);

        return returnedDeposit;
    }
    
    public Deposit[] getDepositsListing(String vaultId) {

        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<Deposit[]> responseEntity = restTemplate.getForEntity(brokerURL + "/vaults/" + vaultId + "/deposits", Deposit[].class);
        Deposit[] deposits = responseEntity.getBody();

        return deposits;
    }
}
