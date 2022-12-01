There should be two text files in this directory 

* `encodedPublicKey.txt`
* `encodedPrivateKey.txt`

These files are used by `org.datavaultplatform.common.crypto.SshRsaKeyUtilsTest`.

The files in this directory are Base64 encoded to stop GitHub from thinking that
a public/private keypair has been committed to GitHub by mistake.

The files in this directory were generated using `org.datavaultplatform.broker.services.UserKeyPairService`
which uses **JSch** library. We aim to replace the **JSch** library. We must ensure we support key pairs that **JSch** has generated
in the past and are being used by DataVault. DataVault holds the private keys but the public keys are installed on 3rd party SFTP Servers associated with DataVault users.

### To decode the encoded text files
To use Unix/Linux/Mac command line to see the contents of the encoded files:

* `base64 -d ./encodedPrivateKey.txt`
* `base64 -d ./encodedPublicKey.txt`

### To Generate a similar Key Pair with the same Format
To use Unix/Linux/Mac command line to generate similar files with the same format:

* `openssl genrsa -des3 -out ./id_example 1024` (You will be prompted for a passphrase for the private key)

* `chmod 400 ./id_example`

* `ssh-keygen -y -f ./id_example > ./id_example.pub` (You will be prompted for the passphrase for the private key)

That should give you 2 files:

* `id_example` - the private key

* `id_example.pub` - the public key

The private key should start with:

    -----BEGIN RSA PRIVATE KEY-----
    Proc-Type: 4,ENCRYPTED
    DEK-Info: DES-EDE3-CBC

The private key should end with:

    -----END RSA PRIVATE KEY-----

The public key should start with:

    ssh-rsa AAAA