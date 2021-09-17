package org.datavaultplatform.broker.controllers.admin;

import java.util.Date;

import org.datavaultplatform.broker.services.PendingVaultsService;
import org.datavaultplatform.common.model.User;
import org.jsondoc.core.annotation.Api;
import org.jsondoc.core.annotation.ApiHeader;
import org.jsondoc.core.annotation.ApiHeaders;
import org.jsondoc.core.annotation.ApiMethod;
import org.jsondoc.core.pojo.ApiVerb;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api(name="AdminPendingVaults", description = "Administrator pending vault functions.")
public class AdminPendingVaultsController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AdminPendingVaultsController.class);
	
	private PendingVaultsService pendingVaultsService;
	
	
	public void setPendingVaultsService(PendingVaultsService pendingVaultsService) {
        this.pendingVaultsService = pendingVaultsService;
    }
	/*
	@RequestMapping(value = "/admin/pendingVaults/addVault/{pendingVaultId}", method = RequestMethod.POST)
	public boolean addVaultForPendingVault(@PathVariable("pendingVaultId") String pendingVaultId,
			                               @RequestBody Date reviewDate) throws Exception {
		return false;
	}*/

	@RequestMapping(value = "/admin/pendingVaults/{id}", method = RequestMethod.DELETE)
	public void delete(@RequestHeader(value = "X-UserID", required = true) String userID,
									  @PathVariable("id") String vaultID) throws Exception {

		pendingVaultsService.delete(vaultID);
	}


}
