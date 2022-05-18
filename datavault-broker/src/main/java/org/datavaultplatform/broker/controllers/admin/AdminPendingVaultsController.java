package org.datavaultplatform.broker.controllers.admin;

import static org.datavaultplatform.common.util.Constants.HEADER_USER_ID;

import org.datavaultplatform.broker.services.PendingVaultsService;
import org.jsondoc.core.annotation.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api(name="AdminPendingVaults", description = "Administrator pending vault functions.")
public class AdminPendingVaultsController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AdminPendingVaultsController.class);
	
	private final PendingVaultsService pendingVaultsService;

	public AdminPendingVaultsController(PendingVaultsService pendingVaultsService) {
		this.pendingVaultsService = pendingVaultsService;
	}

	@DeleteMapping("/admin/pendingVaults/{id}")
	public void delete(@RequestHeader(HEADER_USER_ID) String userID,
									  @PathVariable("id") String vaultID) {

		pendingVaultsService.delete(vaultID);
	}


}
