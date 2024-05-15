package org.datavaultplatform.common.services;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
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
import org.apache.directory.ldap.client.api.LdapNetworkConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;

/**
 * User: Robin Taylor
 * Date: 09/03/2015
 * Time: 11:33
 * 
 * Without Builder, it's easy to get the constructor parameters in the wrong order.
 */
@Slf4j
public class LDAPService {

    private static final Logger logger = LoggerFactory.getLogger(LDAPService.class);


    private final String host;
    private final int port;
    private final boolean useSsl;
    private final String dn;
    private final String password;
    private final String searchContext;
    private final String searchFilter;
    private final Set<String> attrs = new HashSet<>();

    private LDAPService(String host, Integer port, Boolean useSsl, String dn,
        String password, String searchContext, String searchFilter, Set<String> attrs) {

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
        this.attrs.addAll(attrs);

    }

    public static LDAPServiceBuilder builder(){
        return new LDAPServiceBuilder();
    }

    public static class LDAPServiceBuilder {

        private static final String ALL_LDAP_ATTRIBUTES = "*";
        private  String host;
        private  Integer port;
        private  Boolean useSsl;
        private  String dn;
        private  String password;
        private  String searchContext;
        private  String searchFilter;
        private  final Set<String> attrs = new HashSet<>();

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
        public LDAPServiceBuilder attrs(List<String> attrs){
            this.attrs.clear();
            this.attrs.addAll(attrs);
            return this;
        }
        private static Set<String> getCleanAttrs(Set<String> attrs){
            Set<String> cleanAttrs = new HashSet<>();
            if(attrs == null){
                return cleanAttrs;
            }
            //We don't want any blank attributes
            Set<String> nonBlank = attrs.stream().filter(StringUtils::isNotBlank).collect(Collectors.toSet());
            if(nonBlank.isEmpty()){
                // If not attributes are specified, we'll get them all
                cleanAttrs.add(ALL_LDAP_ATTRIBUTES);
            }else{
                cleanAttrs.addAll(nonBlank);
            }
            return cleanAttrs;
        }
        public LDAPService build() {
            return new LDAPService(host, port, useSsl,  dn,
                password,  searchContext,  searchFilter,  getCleanAttrs(attrs));
        }
    }

    private LdapNetworkConnection getConnection() throws LdapException {
        LdapNetworkConnection connection = new LdapNetworkConnection(host, port, useSsl);
        connection.setTimeOut(0);
        connection.bind(dn, password);
        if(connection.isConnected() == false){
            throw new LdapException("failed to connect");
        }
        return connection;
    }

    private  void closeConnection(LdapNetworkConnection connection) {
        IOUtils.closeQuietly(connection);
    }

    private HashMap<String, String> search(LdapNetworkConnection connection, String id) throws LdapException, CursorException {
        logger.info("Search LDAP for [{}]", id);

        HashMap<String, String> attributes = new HashMap<>();

        logger.info("Attributes to retrieve: {}", attrs);
        EntryCursor cursor = null;
        try {
            cursor = connection.search(searchContext,
                "(" + searchFilter + "=" + id + ")", SearchScope.ONELEVEL, attrs.toArray(new String[]{}));
            // We appear to loop round a set of LDAP entries, in fact we only expect to have found one match.
            while (cursor.next()) {
                logger.info("In cursor loop");
                Entry entry = cursor.get();
                if(entry == null){
                    continue;
                }
                logger.info("The entry object as a string:[{}]", entry);

                // Now store all the returned attributes in a HashMap
                for (Attribute attribute : entry.getAttributes()) {
                    if(attribute == null){
                        continue;
                    }
                    logger.info("Adding attribute:[{}]  with value[{}]", id, attribute.getString());
                    attributes.put(attribute.getId(), attribute.getString());
                }
            }
        } catch (RuntimeException ex) {
            logger.error("problem with ldap search for [{}]", id, ex);
        } finally {
            IOUtils.closeQuietly(cursor);
        }
        logger.info("LDAP attributes are [{}]", attributes);
        return attributes;
    }

    public  List<String> autocompleteUID(String term) throws LdapException, CursorException {
        logger.info("Search UUN containing {}", term);
        LdapNetworkConnection connection = getConnection();
        EntryCursor cursor = null;
        try {
            if (searchContext == null) {
                throw new IllegalArgumentException("The base Dn cannot be null");
            }

            // Create a new SearchRequest object
            SearchRequest searchRequest = createSearchRequest(
                "(|(uid=*" + term + "*)(cn=*" + term + "*))",
                5,
                "uid", "cn");
            cursor = new EntryCursorImpl(connection.search(searchRequest));

            List<String> entries = new ArrayList<>();

            // We appear to loop round a set of LDAP entries, in fact we only expect to have found one match.
            while (cursor.next()) {
                logger.info("In cursor loop");
                Entry entry = cursor.get();
                logger.info("The entry object as a string:[{}]",entry.toString());

                String entryString =
                    entry.get("uid").getString() + " - " + entry.get("cn").getString();

                entries.add(entryString);
            }
            logger.info("LDAP found [{}] results.", entries.size());
            //sort by alphabetical order - more consistent for testing
            return entries.stream().sorted(Comparator.naturalOrder()).collect(Collectors.toList());
        } finally {
            IOUtils.closeQuietly(cursor);
            closeConnection(connection);
        }
    }

    public  HashMap<String, String> getLdapUserInfo(String uid) throws LdapException, CursorException {
        logger.info("Search info for UUN: [{}]", uid);

        LdapNetworkConnection connection = getConnection();

        if (searchContext == null) {
            throw new IllegalArgumentException("The base Dn cannot be null");
        }
        logger.info("----------------------Search info for UUN:------------ [{}]", uid);

        EntryCursor cursor = null;
        try {

            SearchRequest searchRequest = createSearchRequest(
                "(uid=" + uid + ")",
                1,
                "uid", "cn", "mail", "eduniRefNo");

            // We don't want this to take too long as it might, default is 30s
            connection.setTimeOut(5000);
            cursor = new EntryCursorImpl(connection.search(searchRequest));

            HashMap<String, String> attributes = new HashMap<>();

            // We appear to loop round a set of LDAP entries, in fact we only expect to have found one match.
            while (cursor.next()) {
                logger.info("In cursor loop");
                Entry entry = cursor.get();
                logger.info("The entry object as a string: [{}]", entry.toString());

                // Now store all the returned attributes in a HashMap
                for (Attribute attribute : entry.getAttributes()) {
                    attributes.put(attribute.getId(), attribute.getString());
                }
            }
            logger.info("LDAP attributes are [{}]", attributes);
            return attributes;
        } finally {
            IOUtils.closeQuietly(cursor);
        }
    }

    private SearchRequest createSearchRequest(String filter, int sizeLimit, String... attributes) throws LdapException{
        if (searchContext == null) {
            throw new IllegalArgumentException("The base Dn cannot be null");
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

    public  HashMap<String, String> getLDAPAttributes(String name) throws LdapException, CursorException {
        LdapNetworkConnection connection = null;
        try {
            connection = getConnection();
            HashMap<String, String> attributes = search(connection, name);
            logger.info("after search [{}]", attributes);
            return attributes;
        } catch (LdapException | CursorException ex) {
            logger.error("problem searching for[{}]", name, ex);
            throw ex;
        } finally {
            closeConnection(connection);
        }
    }

    public void testConnection(String ldapConnectionTestSearchTerm) {
        log.info("ldap.connection.search.term[{}]", ldapConnectionTestSearchTerm);
        if (StringUtils.isBlank(ldapConnectionTestSearchTerm)) {
            log.info("ldap.connection.search.term is blank, not testing ldap connection");
            return;
        }
        try {
            List<String> result = autocompleteUID(ldapConnectionTestSearchTerm);
            log.info("ldap connection test result for ldap.connection.search.term[{}] is {}", ldapConnectionTestSearchTerm,
                result);
        } catch (Exception ex) {
            log.error("Error Testing Ldap Connection with ldap.connection.search.term[{}]", ldapConnectionTestSearchTerm, ex);
        }
    }

    public static void testLdapConnection(ApplicationContext ctx) {
        try {
            LDAPService ldapService = ctx.getBean(LDAPService.class);
            String ldapConnectionTestSearchTerm = ctx.getEnvironment().getProperty("ldap.connection.test.search.term","");
            ldapService.testConnection(ldapConnectionTestSearchTerm);
        } catch(NoSuchBeanDefinitionException ex) {
            log.info("No LDAPService bean found");
        }
    }
}