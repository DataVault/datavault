package org.datavaultplatform.broker.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.datavaultplatform.broker.queue.Sender;
import org.datavaultplatform.broker.services.*;
import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.model.*;
import org.datavaultplatform.common.request.CreateDeposit;
import org.datavaultplatform.common.request.CreateVault;
import org.datavaultplatform.common.response.DepositInfo;
import org.datavaultplatform.common.response.VaultInfo;
import org.datavaultplatform.common.task.Task;
import org.jsondoc.core.annotation.*;
import org.jsondoc.core.pojo.ApiVerb;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RestController
@Api(name="Statistics", description = "System statistics")
public class StatisticsController {

    private VaultsService vaultsService;
    private DepositsService depositsService;
    private RestoresService restoresService;
    private PoliciesService policiesService;
    private GroupsService groupsService;
    private UsersService usersService;

    private static final Logger logger = LoggerFactory.getLogger(StatisticsController.class);

    public void setVaultsService(VaultsService vaultsService) {
        this.vaultsService = vaultsService;
    }

    public void setDepositsService(DepositsService depositsService) {
        this.depositsService = depositsService;
    }

    public void setRestoresService(RestoresService restoresService) {
        this.restoresService = restoresService;
    }

    public void setPoliciesService(PoliciesService policiesService) {
        this.policiesService = policiesService;
    }

    public void setGroupsService(GroupsService groupsService) {
        this.groupsService = groupsService;
    }

    public void setUsersService(UsersService usersService) {
        this.usersService = usersService;
    }


    // Get the specified Vault object and validate it against the current User
    private Vault getUserVault(User user, String vaultID) throws Exception {

        Vault vault = vaultsService.getVault(vaultID);

        if (vault == null) {
            throw new Exception("Vault '" + vaultID + "' does not exist");
        }

        if (!vault.getUser().equals(user)) {
            throw new Exception("Access denied");
        }

        return vault;
    }

    @RequestMapping(value = "/statistics/count", method = RequestMethod.GET)
    public int getVaultsCount(@RequestHeader(value = "X-UserID", required = true) String userID) throws Exception {

        return vaultsService.count();
    }

    @RequestMapping(value = "/statistics/size", method = RequestMethod.GET)
    public Long getVaultsSize(@RequestHeader(value = "X-UserID", required = true) String userID) throws Exception {

        return depositsService.size();
    }

    @RequestMapping(value = "/statistics/depositcount", method = RequestMethod.GET)
    public int getDepositsCount(@RequestHeader(value = "X-UserID", required = true) String userID) throws Exception {

        return depositsService.count();
    }

    @RequestMapping(value = "/statistics/depositinprogresscount", method = RequestMethod.GET)
    public int getDepositsInProgressCount(@RequestHeader(value = "X-UserID", required = true) String userID) throws Exception {

        return depositsService.inProgressCount();
    }

    @RequestMapping(value = "/statistics/restorecount", method = RequestMethod.GET)
    public int getRestoresCount(@RequestHeader(value = "X-UserID", required = true) String userID) throws Exception {

        return restoresService.count();
    }

    @RequestMapping(value = "/statistics/restoreinprogresscount", method = RequestMethod.GET)
    public int getRestoresInProgressCount(@RequestHeader(value = "X-UserID", required = true) String userID) throws Exception {

        return restoresService.inProgressCount();
    }

    @RequestMapping(value = "/vaults/depositqueuecount", method = RequestMethod.GET)
    public int getDepositsQueueCount(@RequestHeader(value = "X-UserID", required = true) String userID) throws Exception {

        return depositsService.queueCount();
    }

    @RequestMapping(value = "/vaults/depositinprogress", method = RequestMethod.GET)
    public List<Deposit> getDepositsInProgress(@RequestHeader(value = "X-UserID", required = true) String userID) throws Exception {

        return depositsService.inProgress();
    }

    @RequestMapping(value = "/vaults/restorequeuecount", method = RequestMethod.GET)
    public int getRestoresQueuedCount(@RequestHeader(value = "X-UserID", required = true) String userID) throws Exception {

        return restoresService.queueCount();
    }

    @RequestMapping(value = "/vaults/restoreinprogress", method = RequestMethod.GET)
    public List<Restore> getRestoresInProgress(@RequestHeader(value = "X-UserID", required = true) String userID) throws Exception {

        return restoresService.inProgress();
    }

    
    @RequestMapping(value = "/vaults/policycount/{status}", method = RequestMethod.GET)
    public int getPolicyStatusCount(@RequestHeader(value = "X-UserID", required = true) String userID,
                                    @PathVariable("status") int status) throws Exception {

        return vaultsService.getPolicyCount(status);
    }
}
