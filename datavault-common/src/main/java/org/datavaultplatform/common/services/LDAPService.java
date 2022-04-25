package org.datavaultplatform.common.services;

import org.springframework.util.Assert;
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

/**
 * Without Builder, it's easy to get the constructor parameters in the wrong order.
 */
public class LDAPService {

    private static final Logger logger = LoggerFactory.getLogger(LDAPService.class);

    private LdapConnection connection;

    private final String host;
    private final int port;
    private final boolean useSsl;
    private final String dn;
    private final String password;
    private final String searchContext;
    private final String searchFilter;
    private final String attrs;

    private LDAPService(String host, Integer port, Boolean useSsl, String dn,
        String password, String searchContext, String searchFilter, String attrs) {

        //1 HOST
        Assert.notNull(host, () -> "host must not be null");

        //2 PORT
        Assert.notNull(port, () -> "port must not be null");
        Assert.isTrue(port > 0, () -> "port must be > 0");

        //3 UseSSL
        Assert.notNull(useSsl, () -> "useSsl must not be null");

        //4 DN
        Assert.notNull(dn, () -> "dn must not be null");

        //5 PASSWORD
        Assert.notNull(password, () -> "password must not be null");

        //6 SEARCH CONTEXT
        Assert.notNull(searchContext, () -> "searchContext must not be null");

        //7 SEARCH FILTER
        Assert.notNull(searchFilter, () -> "searchFilter must not be null");

        //8 ATTRS
        Assert.notNull(attrs, () -> "attrs must not be null");

        this.host = host;
        this.port = port;
        this.useSsl = useSsl;
        this.dn = dn;
        this.password = password;
        this.searchContext = searchContext;
        this.searchFilter = searchFilter;
        this.attrs = attrs;

    }

    public static LDAPServiceBuilder builder(){
        return new LDAPServiceBuilder();
    }

    public static class LDAPServiceBuilder {
        private  String host;
        private  Integer port;
        private  Boolean useSsl;
        private  String dn;
        private  String password;
        private  String searchContext;
        private  String searchFilter;
        private  String attrs;

        private LDAPServiceBuilder() {
        }

        public LDAPServiceBuilder host(String host){
            this.host = host;
            return this;
        }
        public LDAPServiceBuilder port(int port){
            this.port = port;
            return this;
        }
        public LDAPServiceBuilder useSSL(boolean useSSL){
            this.useSsl = useSSL;
            return this;
        }
        public LDAPServiceBuilder dn(String dn){
            this.dn = dn;
            return this;
        }
        public LDAPServiceBuilder password(String password){
            this.password = password;
            return this;
        }
        public LDAPServiceBuilder searchContext(String searchContext){
            this.searchContext = searchContext;
            return this;
        }
        public LDAPServiceBuilder searchFilter(String searchFilter){
            this.searchFilter = searchFilter;
            return this;
        }
        public LDAPServiceBuilder attrs(String attrs){
            this.attrs = attrs;
            return this;
        }
        public LDAPService build() {
            return new LDAPService(host, port, useSsl,  dn,
                password,  searchContext,  searchFilter,  attrs);
        }
    }

    public void getConnection() throws LdapException, CursorException {
        connection = new LdapNetworkConnection(host, port, useSsl);
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
                "(|(uid=*"  + term + "*)(cn=*"+ term + "*))",
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
        System.out.println("----------------------Search info for UUN:------------ " + uid);
      
        SearchRequest searchRequest = createSearchRequest(
                "(uid="  + uid + ")" ,
                1,
                "uid", "cn", "mail", "eduniRefNo");
    
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

    public HashMap<String, String> getLDAPAttributes(String name) throws LdapException, IOException, CursorException {
        HashMap<String, String> attributes;

        try {
            getConnection();
            attributes = search(name);

        } catch (Exception e) {
            throw e;

        } finally {
            try {
                closeConnection();
            } catch (LdapException e) {
                throw e;
            }
        }

        return attributes;
    }
}