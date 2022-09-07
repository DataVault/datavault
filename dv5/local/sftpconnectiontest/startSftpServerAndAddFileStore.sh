#!/bin/bash

# we have to look for a port >= 3000 that is not in use.
PORT=$(netstat -aln | awk '
  $6 == "LISTEN" {
    if ($4 ~ "[.:][0-9]+$") {
      split($4, a, /[:.]/);
      port = a[length(a)];
      p[port] = 1
    }
  }
  END {
    for (i = 3000; i < 65000 && p[i]; i++){};
    if (i == 65000) {exit 1};
    print i
  }
')
echo "$PORT"

JSON=$(cat <<- EOF
{
  "id": null,
  "storageClass": "org.datavaultplatform.common.storage.impl.SFTPFileSystem",
  "label": "label-one",
  "properties": {
  "port": "$PORT",
  "host": "localhost",
  "rootPath": "/config"
  }
}
EOF
)
echo $JSON | jq

REPLY=$(curl \
 -H "Content-Type:application/json" \
 -H "X-Client-Key:datavault-webapp" \
 -H "X-UserID:admin1" \
 -X POST -d "$JSON" \
 http://localhost:8080/filestores/sftp)
echo $REPLY | jq
PUB_KEY=$(echo $REPLY | jq -r '.properties.publicKey')
echo $PUB_KEY

docker run --rm --name "sftp-for-connection-test" \
 -e PUBLIC_KEY="$PUB_KEY" \
 -e USER_NAME=admin1 \
 -p $PORT:2222 \
 linuxserver/openssh-server:version-8.6_p1-r3
