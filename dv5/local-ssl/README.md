These scripts help test the setup of DV spring boot apps with SSL/TLS certificates.
The certificates generated here are Self-Signed certificates generated for testing purposes only.

The following properties will need to be configured in each spring boot app to enable SSL/TLS;

Here is an example of the properties that need to be configured in the properties file of each spring boot app;

```
server.port=8443
server.ssl.enabled=true
server.ssl.key-store=/home/user1/webapp.p12
server.ssl.key-store-password=${PASSWORD}
server.ssl.key-store-type=PKCS12
server.ssl.key-alias=theKey
server.ssl.key-password=${PASSWORD}
```

Thes SSL/TLS property changes have been included in the following scripts
* `dv5/local-ssl/scripts/runLocalBroker.sh`
* `dv5/local-ssl/scripts/runLocalWebapp.sh`
* `dv5/local-ssl/scripts/runLocalWorker.sh`

### Scripts for SSL/TLS Self-Signed certificate generation
 * `generateP12keyStores.sh` - generates three .p12 keystore files which each hold a SSL/TLS private key. 
 * `inspectP12keyStores.sh` - show the contents of the three .p12 keystore files which each hold a SSL/TLS private key.

### Local Development to check SSL/TLS configuration
  We are going to edit `/etc/hosts` and add the following entries:
```
127.0.0.1	webapp.dv.local
127.0.0.1	broker.dv.local
127.0.0.1	worker.dv.local
```

### Self-Signed SSL/TLS certificates
The Self-Signed certificates work okay for local testing. To use them, please note the following
 *  Web browers like Google Chome will warn you when you visit the `https://webapp.dv.local:7443` site - that is using Self-Signed cert
 *  The `curl` command requires the `-k` option to work with self-signed certs
 * Spring's `RestTemplate` class (used for WebApp-Broker communication) - will normally NOT work with Self-Signed certs.

### Self-Signed Certs and Spring's RestTemplate

The WebApp's `RestTemplateConfig` has some extra config which can be enabled via the property
`broker.using.selfsignedcert`. This config will allow the WebApp to talk to a Broker which uses a Self-Signed cert.