package org.datavaultplatform.broker.authentication;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

import org.datavaultplatform.broker.controllers.admin.AdminPendingVaultsController;
import org.datavaultplatform.common.model.Permission;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

public class AdminPendingVaultsControllerAuthTest extends BaseControllerAuthTest {

  @MockBean
  AdminPendingVaultsController controller;

  @Test
  void testDeletePendingVault() {
    doNothing().when(controller).delete(USER_ID_1, "vault-id-123");

    checkWorksWhenAuthenticatedFailsOtherwise(
        delete("/admin/pendingVaults/{id}", "vault-id-123"),
        null,
        Permission.CAN_MANAGE_VAULTS);

    verify(controller).delete(USER_ID_1, "vault-id-123");
    checkHasSecurityAdminVaultsRole();
  }

}
