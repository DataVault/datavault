package org.datavaultplatform.webapp.services;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.directory.api.ldap.model.cursor.CursorException;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.datavaultplatform.common.model.User;
import org.datavaultplatform.webapp.exception.InvalidUunException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
        List<String> result = new ArrayList<>();
        try {
            result = ldapService.autocompleteUID(term);
        } catch (LdapException | CursorException | IOException ex) {
            ex.printStackTrace();
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
            newUser.setAdmin(false);

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
}
