package org.datavaultplatform.broker.controllers.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.datavaultplatform.broker.services.PendingVaultsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import static org.datavaultplatform.common.util.Constants.HEADER_USER_ID;

@RestController
@Tag(name="admin-pending-vaults-controller", description = "Administrator pending vault functions.")
public class AdminPendingVaultsController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AdminPendingVaultsController.class);
	
	private final PendingVaultsService pendingVaultsService;

	public AdminPendingVaultsController(PendingVaultsService pendingVaultsService) {
		this.pendingVaultsService = pendingVaultsService;
	}

	@Operation(
			summary = "Delete a Pending Vault",
			description = "Deletes a Pending Vault from the system."
	)
	@DeleteMapping("/admin/pendingVaults/{vaultId}")
	public void delete(@RequestHeader(HEADER_USER_ID) String userID,
					   @PathVariable String vaultId) {

		pendingVaultsService.delete(vaultId);
	}


}
