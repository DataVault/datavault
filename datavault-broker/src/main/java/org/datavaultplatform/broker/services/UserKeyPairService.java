package org.datavaultplatform.broker.services;

import lombok.Builder;
import lombok.Data;

public interface UserKeyPairService {

  // comment added at the end of public key
  String PUBKEY_COMMENT = "datavault";

  String getPassphrase();

  KeyPairInfo generateNewKeyPair();

  @Data
  @Builder
  class KeyPairInfo {

    private final String publicKey;
    private final String privateKey;
    private final String fingerPrint;
    private final Integer keySize;
  }
}
