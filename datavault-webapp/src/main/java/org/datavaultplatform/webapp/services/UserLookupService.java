package org.datavaultplatform.webapp.services;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.directory.api.ldap.model.cursor.CursorException;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.datavaultplatform.common.model.User;
import org.datavaultplatform.common.request.CreateVault;
import org.datavaultplatform.common.services.LDAPService;
import org.datavaultplatform.webapp.exception.InvalidUunException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnBean(RestService.class)
public class UserLookupService {

    private static final Logger logger = LoggerFactory.getLogger(UserLookupService.class);

    private LDAPService ldapService;

    private RestService restService;

    public void setLdapService(LDAPService ldapService) {
        this.ldapService = ldapService;
    }

    public void setRestService(RestService restService) {
        this.restService = restService;
    }

    public List<String> getSuggestedUuns(String term) {
        List<String> result;
        try {
            result = ldapService.autocompleteUID(term);
        } catch (LdapException | CursorException | IOException ex) {
            ex.printStackTrace();
            // A fallback to known users in the Database in case LDAP is not available:
            return Arrays.stream(restService.getUsers())
                    .map(user -> String.format("%s - %s %s",
                        user.getID(),
                        user.getFirstname().replace(" - ", " – "),
                        user.getLastname().replace(" - ", " – ")
                    )
            ).collect(Collectors.toList());
        }
        return result;
    }

    public User ensureUserExists(String uun) throws InvalidUunException {
        User user = restService.getUser(uun);
        if (user == null) {
            // Validate UUN
            HashMap<String, String> attributes;
            try {
                attributes = ldapService.getLdapUserInfo(uun);
            } catch (LdapException | CursorException | IOException e) {
                throw new InvalidUunException(uun, e);
            }
            if (attributes.size() < 1){
                throw new InvalidUunException(uun);
            }

            String[] names = attributes.get("cn").split(" "); attributes.remove("cn");

            logger.info("Adding user {} - {} {}", uun, names[0], names[1]);
            User newUser = new User();
            newUser.setFirstname(names[0]);
            newUser.setLastname(names[1]);
            newUser.setID(attributes.get("uid")); attributes.remove("uid");
            newUser.setEmail(attributes.get("mail")); attributes.remove("mail");
            newUser.setProperties(attributes);

            // Generate random password to make sure account is not easily accessible
            String password = RandomStringUtils.randomAscii(10);
            newUser.setPassword(password);

            restService.addUser(newUser);
            return newUser;

        } else {
            logger.info("User {} already exists!", uun);
            return user;
        }
    }
    
    public boolean isUUN(String uun) {
    	boolean exists = false;
    	try {
    		HashMap<String, String> attributes = ldapService.getLdapUserInfo(uun);
    		logger.info("isUUN - uun: {} ", uun);
    		logger.info("isUUN - ATTRIBUTES: {} ", attributes);
    		logger.info("isUUN - attributes.size(): {} ", attributes.size());
            if (attributes.size() > 0){
            	exists = true;
            }
            
        } catch (Exception e) {
        	if(logger.isDebugEnabled()){
                logger.error("isUUN {}", uun, e);
            }
        }
    	
    	return exists;
    }

    private String checkUserList(List<String> list, String errorUrl) {
        String retVal = "";

        for (String li : list) {
            String result = this.checkUser(li, errorUrl);
            if (result != null && ! result.isEmpty()) {
                return result;
            }
        }

        return retVal;

    }

    private String checkUser(String user, String errorUrl) {
        String retVal = "";
        // exclude the empty dummy user
        if (user != null && ! user.equals("")) {
            try {
                this.ensureUserExists(user);
            } catch (InvalidUunException e) {
                    /* @TODO: need to go back to the entered values plus an error message about the problem user.
                    This will do none of that but will return to an empty from
                    Would be good if we checked all of them before erroring too
                    */
                return errorUrl;
            }
        }
        return retVal;
    }

    public String checkNewRolesUserExists(CreateVault vault, String buildUrl) {
        String retVal = "";

        // foreach depositor
        List<String> deps = vault.getDepositors();
        String depResult = this.checkUserList(deps, buildUrl);

        if (depResult != null && ! depResult.isEmpty()) {
            return "redirect:" + depResult;
        }
        // foreach ndm
        List<String> ndms = vault.getNominatedDataManagers();
        String ndmResult = this.checkUserList(ndms, buildUrl);

        if (ndmResult != null && ! ndmResult.isEmpty()) {
            return "redirect:" + ndmResult;
        }

        // foreach data creator
        List<String> creators = vault.getDataCreators();
        String creatorResult = this.checkUserList(creators, buildUrl);

        if (creatorResult != null && ! creatorResult.isEmpty()) {
            return "redirect:" + creatorResult;
        }
        // owner
        String owner = vault.getVaultOwner();
        String ownerResult = this.checkUser(owner, buildUrl);

        if (ownerResult != null && !ownerResult.isEmpty()) {
            return "redirect:" + ownerResult;
        }

        // contact
        String contact = vault.getContactPerson();
        String contactResult = this.checkUser(contact, buildUrl);

        if (contactResult != null && !contactResult.isEmpty()) {
            return "redirect:" + contactResult;
        }

        return retVal;
    }
}
