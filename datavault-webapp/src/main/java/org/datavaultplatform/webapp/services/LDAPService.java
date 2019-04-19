package org.datavaultplatform.webapp.services;

import org.apache.directory.api.ldap.model.cursor.CursorException;
import org.apache.directory.api.ldap.model.cursor.EntryCursor;
import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.message.AliasDerefMode;
import org.apache.directory.api.ldap.model.message.SearchRequest;
import org.apache.directory.api.ldap.model.message.SearchRequestImpl;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.ldap.client.api.EntryCursorImpl;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.ldap.client.api.LdapNetworkConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * User: Robin Taylor
 * Date: 09/03/2015
 * Time: 11:33
 */

public class LDAPService {

    private static final Logger logger = LoggerFactory.getLogger(LDAPService.class);

    private LdapConnection connection;

    private String host;
    private String port;
    private String useSsl;
    private String dn;
    private String password;
    private String searchContext;
    private String searchFilter;
    private String attrs;

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public void setUseSsl(String useSsl) {
        this.useSsl = useSsl;
    }

    public void setDn(String dn) {
        this.dn = dn;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setSearchContext(String searchContext) {
        this.searchContext = searchContext;
    }

    public void setSearchFilter(String searchFilter) {
        this.searchFilter = searchFilter;
    }

    public void setAttrs(String attrs) {
        this.attrs = attrs;
    }

    public void getConnection() throws LdapException, CursorException {
        connection = new LdapNetworkConnection(host, Integer.parseInt(port), Boolean.parseBoolean(useSsl));
        connection.setTimeOut(0);
        connection.bind(dn, password);
    }

    public void closeConnection() throws LdapException {
        connection.unBind();
    }

    public HashMap<String, String> search(String id) throws LdapException, CursorException, IOException {
        logger.info("Search LDAP for " + id);

        HashMap<String, String> attributes = new HashMap<String, String>();

        logger.info("Attributes to retrieve: " + attrs);
        EntryCursor cursor = connection.search(searchContext, "(" + searchFilter + "="  + id + ")", SearchScope.ONELEVEL, attrs);
        // We appear to loop round a set of LDAP entries, in fact we only expect to have found one match.
        while (cursor.next()) {
        	logger.info("In cursor loop");
            Entry entry = cursor.get();
            logger.info("The entry object as a string:" + entry.toString());

            // Now store all the returned attributes in a HashMap
            for (Attribute attribute : entry.getAttributes()) {
            	logger.info("Adding attribute:" + id + " with value " +  attribute.getString());
                attributes.put(attribute.getId(), attribute.getString());
            }
        }

        cursor.close();

        logger.info("LDAP attributes are " + attributes.toString());
        return attributes;
    }

    public List<String> autocompleteUID(String term) throws LdapException, CursorException, IOException {
        logger.info("Search UUN containning " + term);

        getConnection();

        if ( searchContext == null )
        {
            throw new IllegalArgumentException( "The base Dn cannot be null" );
        }

        // Create a new SearchRequest object
        SearchRequest searchRequest = createSearchRequest(
                "(uid=*"  + term + "*)",
                5,
                "uid", "cn");

        EntryCursor cursor = new EntryCursorImpl( connection.search(searchRequest) );

        List<String> entries = new ArrayList<>();

        // We appear to loop round a set of LDAP entries, in fact we only expect to have found one match.
        while (cursor.next()) {
            logger.info("In cursor loop");
            Entry entry = cursor.get();
            logger.info("The entry object as a string:" + entry.toString());

            String entryString = entry.get("uid").getString() + " - " + entry.get("cn").getString();

            entries.add(entryString);
        }

        cursor.close();
        closeConnection();

        logger.info("LDAP found " + entries.size() + " results.");
        return entries;
    }

    public HashMap<String, String> getLdapUserInfo(String uid) throws LdapException, CursorException, IOException {
        logger.info("Search info for UUN: " + uid);

        getConnection();

        if ( searchContext == null )
        {
            throw new IllegalArgumentException( "The base Dn cannot be null" );
        }

        // Create a new SearchRequest object
        SearchRequest searchRequest = createSearchRequest(
                "(uid="  + uid + ")",
                1,
                "uid", "cn", "email", "eduniRefNo");

        // We don't want this to take too long as it might, default is 30s
        connection.setTimeOut(5000);
        EntryCursor cursor = new EntryCursorImpl( connection.search(searchRequest) );
        connection.setTimeOut(0);

        HashMap<String, String> attributes = new HashMap<>();

        // We appear to loop round a set of LDAP entries, in fact we only expect to have found one match.
        while (cursor.next()) {
            logger.info("In cursor loop");
            Entry entry = cursor.get();
            logger.info("The entry object as a string:" + entry.toString());

            // Now store all the returned attributes in a HashMap
            for (Attribute attribute : entry.getAttributes()) {
                attributes.put(attribute.getId(), attribute.getString());
            }
        }

        cursor.close();
        closeConnection();

        logger.info("LDAP attributes are " + attributes.toString());
        return attributes;
    }

    private SearchRequest createSearchRequest(String filter, int sizeLimit, String... attributes) throws LdapException{
        if ( searchContext == null )
        {
            throw new IllegalArgumentException( "The base Dn cannot be null" );
        }

        // Create a new SearchRequest object
        SearchRequest searchRequest = new SearchRequestImpl();

        searchRequest.setBase( new Dn( searchContext ) );
        searchRequest.setFilter( filter );
        searchRequest.setScope( SearchScope.ONELEVEL );
        searchRequest.addAttributes( attributes );
        searchRequest.setDerefAliases( AliasDerefMode.DEREF_ALWAYS );
        searchRequest.setSizeLimit(sizeLimit);

        return searchRequest;
    }
}