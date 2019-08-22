package org.datavaultplatform.webapp.controllers.admin;

import org.datavaultplatform.common.model.Permission;
import org.datavaultplatform.webapp.model.AdminDashboardPermissionsModel;
import org.datavaultplatform.webapp.services.PermissionsService;
import org.datavaultplatform.webapp.services.RestService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
public class AdminController {

    private RestService restService;

    private PermissionsService permissionsService;

    public void setRestService(RestService restService) {
        this.restService = restService;
    }

    public void setPermissionsService(PermissionsService permissionsService) {
        this.permissionsService = permissionsService;
    }

    @RequestMapping(value = "/admin", method = RequestMethod.GET)
    public String adminIndex(ModelMap modelMap, Principal principal) {
        AdminDashboardPermissionsModel permissionsModel = permissionsService.getDashboardPermissions(principal);

        if (permissionsModel.hasPermission(Permission.CAN_VIEW_VAULTS_SIZE)) {
            modelMap.addAttribute("canViewVaultsSize", true);
            modelMap.addAttribute("vaultsize", getVaultSize());
        }
        if (permissionsModel.hasPermission(Permission.CAN_VIEW_IN_PROGRESS)) {
            modelMap.addAttribute("canViewInProgress", true);
            modelMap.addAttribute("depositsinprogress", restService.getDepositsInProgressCount());
            modelMap.addAttribute("retrievesinprogress", restService.getRetrievesInProgressCount());
        }
        if (permissionsModel.hasPermission(Permission.CAN_VIEW_QUEUES)) {
            modelMap.addAttribute("canViewQueues", true);
            modelMap.addAttribute("depositqueue", restService.getDepositsQueue());
            modelMap.addAttribute("retrievequeue", restService.getRetrievesQueue());
        }
        if (permissionsModel.hasPermission(Permission.CAN_MANAGE_VAULTS)) {
            modelMap.addAttribute("canManageVaults", true);
            modelMap.addAttribute("vaultcount", restService.getVaultsCount());
        }
        if (permissionsModel.hasPermission(Permission.CAN_MANAGE_DEPOSITS)) {
            modelMap.addAttribute("canManageDeposits", true);
            modelMap.addAttribute("depositcount", restService.getDepositsCount());
        }
        if (permissionsModel.hasPermission(Permission.CAN_VIEW_RETRIEVES)) {
            modelMap.addAttribute("canViewRetrieves", true);
            modelMap.addAttribute("retrievecount", restService.getRetrievesCount());
        }
        if (permissionsModel.hasPermission(Permission.CAN_MANAGE_BILLING_DETAILS)) {
            modelMap.addAttribute("canManageBillingDetails", true);
        }
        if (permissionsModel.hasPermission(Permission.CAN_VIEW_SCHOOL_ROLE_ASSIGNMENTS)) {
            modelMap.addAttribute("canManageSchoolUsers", true);
            modelMap.addAttribute("groupcount", restService.getGroupsCount());
        }
        if (permissionsModel.hasPermission(Permission.CAN_MANAGE_ROLES)) {
            modelMap.addAttribute("canManageRoles", true);
            modelMap.addAttribute("rolecount", getManagableRolesCount());
        }
        if (permissionsModel.hasPermission(Permission.CAN_VIEW_EVENTS)) {
            modelMap.addAttribute("canViewEvents", true);
            modelMap.addAttribute("eventcount", restService.getEventCount());
        }
        if (permissionsModel.hasPermission(Permission.CAN_MANAGE_RETENTION_POLICIES)) {
            modelMap.addAttribute("canManageRetentionPolicies", true);
            modelMap.addAttribute("policycount", restService.getRetentionPolicyListing().length);
        }
        if (permissionsModel.hasPermission(Permission.CAN_MANAGE_ARCHIVE_STORES)) {
            modelMap.addAttribute("canManageArchiveStores", true);
            modelMap.addAttribute("archivestorescount", restService.getArchiveStores().length);
        }

        return "admin/index";
    }

    private long getVaultSize() {
        Long vaultSize = restService.getVaultsSize();
        if (vaultSize == null) {
            vaultSize = 0L;
        }
        return vaultSize;
    }

    private int getManagableRolesCount() {
        // Total number of roles =
        //        1 (for the IS Admin)
        //      + number of read-only roles
        //      + number of editable roles
        return 1 + restService.getViewableRoles().size() + restService.getEditableRoles().size();
    }
}


