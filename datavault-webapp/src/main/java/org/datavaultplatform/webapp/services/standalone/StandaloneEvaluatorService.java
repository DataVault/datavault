package org.datavaultplatform.webapp.services.standalone;

import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.model.Group;
import org.datavaultplatform.common.model.Vault;
import org.datavaultplatform.common.response.VaultInfo;
import org.datavaultplatform.webapp.services.EvaluatorService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Profile("standalone")
@Service
@Slf4j
public class StandaloneEvaluatorService implements EvaluatorService {

  @Override
  public VaultInfo getVault(String id) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Group getGroup(String groupID) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Vault getVaultRecord(String id) {
    throw new UnsupportedOperationException();
  }
}
