package org.datavaultplatform.broker.services;

import java.math.BigDecimal;
import java.util.*;

import org.datavaultplatform.common.model.*;
import org.datavaultplatform.common.model.dao.VaultDAO;
import org.datavaultplatform.common.request.CreateVault;
import org.datavaultplatform.common.retentionpolicy.RetentionPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VaultsService {

    private final Logger logger = LoggerFactory.getLogger(VaultsService.class);
    private VaultDAO vaultDAO;

    private RolesAndPermissionsService rolesAndPermissionsService;

    private RetentionPoliciesService retentionPoliciesService;

    private DataCreatorsService dataCreatorsService;

    private BillingService billingService;

    public void setRolesAndPermissionsService(RolesAndPermissionsService rolesAndPermissionsService) {
        this.rolesAndPermissionsService = rolesAndPermissionsService;
    }

    public RetentionPoliciesService getRetentionPoliciesService() {
        return retentionPoliciesService;
    }

    public void setRetentionPoliciesService(RetentionPoliciesService retentionPoliciesService) {
        this.retentionPoliciesService = retentionPoliciesService;
    }

    public void setDataCreatorsService(DataCreatorsService dataCreatorsService) { this.dataCreatorsService = dataCreatorsService; }

    public void setBillingService(BillingService billingService) { this.billingService = billingService; }

    public List<Vault> getVaults() {
        return vaultDAO.list();
    }

    public List<Vault> getVaults(String userId, String sort, String order, String offset, String maxResult) {
        return vaultDAO.list(userId, sort, order, offset, maxResult);
    }

    public void addVault(Vault vault) {
        Date d = new Date();
        vault.setCreationTime(d);
        vaultDAO.save(vault);
    }

    public void orphanVault(Vault vault) {
        vault.setUser(null);
        vaultDAO.update(vault);

        RoleModel dataOwnerRole = rolesAndPermissionsService.getDataOwner();
        rolesAndPermissionsService.getRoleAssignmentsForRole(dataOwnerRole.getId()).stream()
                .filter(roleAssignment -> vault.getID().equals(roleAssignment.getVaultId()))
                .findFirst()
                .ifPresent(roleAssignment -> rolesAndPermissionsService.deleteRoleAssignment(roleAssignment.getId()));
    }

    public void updateVault(Vault vault) {
        vaultDAO.update(vault);
    }

    public void saveOrUpdateVault(Vault vault) {
        vaultDAO.saveOrUpdateVault(vault);
    }

    public Vault getVault(String vaultID) {
        return vaultDAO.findById(vaultID);
    }

    public void setVaultDAO(VaultDAO vaultDAO) {
        this.vaultDAO = vaultDAO;
    }

    public List<Vault> search(String userId, String query, String sort, String order, String offset, String maxResult) {
        return this.vaultDAO.search(userId, query, sort, order, offset, maxResult);
    }

    public int count(String userId) {
        return vaultDAO.count(userId);
    }

    public int getRetentionPolicyCount(int status) {
        return vaultDAO.getRetentionPolicyCount(status);
    }

    public Vault checkRetentionPolicy(String vaultID) throws Exception {
        // Get the vault
        Vault vault = vaultDAO.findById(vaultID);

        retentionPoliciesService.setRetention(vault);

        // Check the policy
        //retentionPoliciesService.run(vault);

        // Set the expiry date
        //vault.setRetentionPolicyExpiry(retentionPoliciesService.getReviewDate(vault));

        // Record when we checked it
        //vault.setRetentionPolicyLastChecked(new Date());

        // Update and return the policy
        vaultDAO.update(vault);
        return vault;
    }

    // Get the specified Vault object and validate it against the current User
    public Vault getUserVault(User user, String vaultID) throws Exception {
        Vault vault = getVault(vaultID);

        if (vault == null) {
            throw new Exception("Vault '" + vaultID + "' does not exist");
        }

        return vault;
    }

    public int getTotalNumberOfVaults(String userId) {
        return vaultDAO.getTotalNumberOfVaults(userId);
    }

    /**
     * Total number of records after applying search filter
     *
     * @param query
     * @return
     */
    public int getTotalNumberOfVaults(String userId, String query) {
        return vaultDAO.getTotalNumberOfVaults(userId, query);
    }

    public Map<String, Long> getAllProjectsSize() {
        Map<String, Long> projectSizeMap = new HashMap<>();
        List<Object[]> allProjectsSize = vaultDAO.getAllProjectsSize();
        if (allProjectsSize != null) {
            for (Object[] projectsizeArray : allProjectsSize) {
                projectSizeMap.put((String) projectsizeArray[0], (Long) projectsizeArray[1]);
            }
        }
        return projectSizeMap;
    }

    public void transferVault(Vault vault, User newOwner, String reason) {
        vault.setUser(newOwner);
        vaultDAO.update(vault);

        RoleModel dataOwnerRole = rolesAndPermissionsService.getDataOwner();
        rolesAndPermissionsService.getRoleAssignmentsForRole(dataOwnerRole.getId()).stream()
                .filter(roleAssignment -> vault.getID().equals(roleAssignment.getVaultId()))
                .findFirst()
                .ifPresent(roleAssignment -> rolesAndPermissionsService.deleteRoleAssignment(roleAssignment.getId()));

        RoleAssignment newDataOwnerAssignment = new RoleAssignment();
        newDataOwnerAssignment.setUserId(newOwner.getID());
        newDataOwnerAssignment.setVaultId(vault.getID());
        newDataOwnerAssignment.setRole(dataOwnerRole);
        rolesAndPermissionsService.createRoleAssignment(newDataOwnerAssignment);
    }

    public void addDepositorRoles(CreateVault createVault, String vaultId) {

        // if vault already has depositors delete them and readd
        List<String> depositors = createVault.getDepositors();
        if (depositors != null) {
            for (String dep: depositors) {
                if (dep != null && ! dep.isEmpty()) {
                    RoleAssignment ra = new RoleAssignment();
                    ra.setVaultId(vaultId);
                    ra.setRole(rolesAndPermissionsService.getDepositor());
                    ra.setUserId(dep);
                    rolesAndPermissionsService.createRoleAssignment(ra);
                    logger.debug("Depositor + '" + dep + "'");
                }
            }
        }
    }

    public void addOwnerRole(CreateVault createVault, String vaultId, String userID) {
        Boolean isOwner = createVault.getIsOwner();
        logger.debug("IsOwner is '" + isOwner + "'");
        String ownerId = userID;
        if (isOwner != null && !isOwner) {
            ownerId = createVault.getVaultOwner();
        }
        logger.debug("VaultOwner is '" + ownerId + "'");
        if (ownerId != null) {
            RoleAssignment ownerRoleAssignment = new RoleAssignment();
            ownerRoleAssignment.setUserId(ownerId);
            ownerRoleAssignment.setVaultId(vaultId);
            ownerRoleAssignment.setRole(rolesAndPermissionsService.getDataOwner());
            rolesAndPermissionsService.createRoleAssignment(ownerRoleAssignment);
        } else {
            // error!
        }
    }

    public Vault processDataCreatorParams(CreateVault createVault, Vault vault) {
        List<String> dcs = createVault.getDataCreators();
        if (dcs != null) {
            logger.debug("Data creator list is :'" + dcs + "'");
            List<DataCreator> creators = new ArrayList();
            for (String creator : dcs) {
                if (creator != null && !creator.isEmpty()) {
                    DataCreator dc = new DataCreator();
                    dc.setName(creator);
                    dc.setVault(vault);
                    creators.add(dc);
                }
            }
            vault.setDataCreator(creators);
        } else {
            logger.debug("Data creator list is :null");
        }

        List<DataCreator> creators = vault.getDataCreators();
        if (creators != null && ! creators.isEmpty()) {
            dataCreatorsService.addCreators(creators);
        }

        return vault;
    }

    public void addNDMRoles(CreateVault createVault, String vaultId) {
        List<String> ndms = createVault.getNominatedDataManagers();
        if (ndms != null) {
            for (String ndm: ndms) {
                if (ndm != null && !ndm.isEmpty()) {
                    RoleAssignment ra = new RoleAssignment();
                    ra.setVaultId(vaultId);
                    ra.setRole(rolesAndPermissionsService.getNominatedDataManager());
                    ra.setUserId(ndm);
                    rolesAndPermissionsService.createRoleAssignment(ra);
                    logger.debug("NDMS + '" + ndm + "'");
                }
            }
        }
    }

    public void addBillingInfo(CreateVault createVault, Vault vault) {
        BillingInfo billinginfo =  new BillingInfo();
        billinginfo.setAmountBilled(new BigDecimal(0));
        billinginfo.setAmountToBeBilled(new BigDecimal(0));
        billinginfo.setVault(vault);
        String billingType = createVault.getBillingType();

        PendingVault.Billing_Type enumBT = PendingVault.Billing_Type.valueOf(billingType);
        billinginfo.setBillingType(enumBT);

        if (enumBT.equals(PendingVault.Billing_Type.GRANT_FUNDING)) {
            billinginfo.setContactName(createVault.getGrantAuthoriser());
            billinginfo.setSchool(createVault.getGrantSchoolOrUnit());
            billinginfo.setSubUnit(createVault.getGrantSubunit());
            billinginfo.setProjectTitle(createVault.getProjectID());
        }

        if (enumBT.equals(PendingVault.Billing_Type.BUDGET_CODE)) {
            billinginfo.setContactName(createVault.getBudgetAuthoriser());
            billinginfo.setSchool(createVault.getBudgetSchoolOrUnit());
            billinginfo.setSubUnit(createVault.getBudgetSubunit());
        }

        if (enumBT.equals(PendingVault.Billing_Type.SLICE)) {
            billinginfo.setSliceID(createVault.getSliceID());
        }
        billingService.saveOrUpdateVault(billinginfo);
    }
}
