package org.datavaultplatform.webapp.controllers.admin;

import org.datavaultplatform.common.model.PermissionModel;
import org.datavaultplatform.common.model.RoleModel;
import org.datavaultplatform.common.model.RoleType;
import org.datavaultplatform.webapp.services.RestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.datavaultplatform.webapp.model.RoleViewModel;

import java.util.*;
//import java.util.stream.Collectors;


@Controller
public class AdminRolesController {

    private static final Logger logger = LoggerFactory.getLogger(AdminRolesController.class);
    private RestService restService;

    public void setRestService(RestService restService) {
        this.restService = restService;
    }

    private void setCommonModel(ModelMap model) {


        List<RoleModel> roles = restService.getEditableRoles();

        if (roles.isEmpty()) {
            logger.info("getRolesListing: roles has no items in rest fetch");
        }

        model.addAttribute("roles", roles);


        if (!model.containsAttribute("errormessage")) {
            model.addAttribute("errormessage", "");
        }


    }


    @RequestMapping(value = "/admin/roles", method = RequestMethod.GET)
    public String getRolesListing(ModelMap model) {

        try {
                setCommonModel(model);

        } catch (Exception e) {

            logger.error("Exception in getRolesListing ", e);
            model.addAttribute("errormessage", "<strong>Error loading roles:</strong>" + e.getMessage());

        } finally {
            return "admin/roles/index";
        }
    }

    @RequestMapping(value = "/admin/roles/create", method = RequestMethod.POST)
    public String addRole(@ModelAttribute RoleModel role, ModelMap model, @RequestParam String action) throws Exception {

        setCommonModel(model);
        return "admin/roles/index";
    }

    private RoleModel getRoleById(long roleid) {

        Optional<RoleModel> optional = restService.getEditableRoles().stream().filter(x -> x.getId().equals(roleid)).findFirst();
        RoleModel role = optional.isPresent() ? optional.get() : null;

        return role;

    }


    @RequestMapping(value = "/admin/roles/save", method = RequestMethod.POST)
    @ResponseBody
    public void save(@RequestParam(value="id") int id,
                     @RequestParam(value="name") String name,
                     @RequestParam(value="type") String type,
                     @RequestParam(value="description",required=false) String description,
                     @RequestParam(value="permissions[]", required=false) String[] permissions) throws Exception {

        try {

            for (String p: permissions ) {
                    logger.info("permission " + p);
            }

            logger.info("saving now: " + id + "name " + name);

            switch (id) {
                case 0:
                    //create new;
                    createNewRole(name, type, description, permissions);
                    break;

                default:
                    saveRole(id, name, type, description, permissions);
                    break;
            }

        } catch(Exception e) {
            logger.error("Error Saving Role ", e);
            throw e;
        }
    }

    private void saveRole(int id, String name, String type, String description, String[] permissions) throws Exception
    {
        RoleModel role = getRoleById(id);

        if (role == null || role.getId() == 0)  {
            logger.error("saveRole: Role can not be found on edit");
            throw new Exception("Roles can not be found on edit action ");
        }

        List<PermissionModel> pms = getSelectedPermissions(type, permissions);

        role.setPermissions(pms);
        role.setName(name);
        role.setDescription(description);
        role.setType(RoleType.valueOf(type));

        restService.updateRole(role);

    }


    private void createNewRole(String name, String type, String description, String[] permissions)
    {

        RoleModel role = new RoleModel();

        List<PermissionModel> pms = getSelectedPermissions(type, permissions);

        role.setPermissions(pms);
        role.setName(name);
        role.setDescription(description);
        role.setType(RoleType.valueOf(type));

        restService.createRole(role);

    }

    private List<PermissionModel> getSelectedPermissions(String type, String[] permIds )
    {
        List<PermissionModel> pms = getPermissionsByType(type);

        ArrayList<PermissionModel> result = new ArrayList<>();

        if (pms == null) return
                new ArrayList<>();

        for (String id : permIds)
        {
            Optional<PermissionModel> optional = pms.stream().filter(x -> x.getId().equals(id)).findFirst();
            PermissionModel perm = optional.isPresent() ? optional.get() : null;

            if (perm != null)
                result.add(perm);

        }
          return result;
    }

    private List<PermissionModel> getPermissionsByType(String type) {

        List<PermissionModel> model;

        switch (type.toUpperCase()) {
            case "VAULT":
                model = restService.getVaultPermissions();
                break;
            case "SCHOOL":
                model = restService.getSchoolPermissions();
                break;
            default:
                model = null;

        }

        return model;
    }



    @RequestMapping(value = "/admin/roles/delete/{id}", method = {RequestMethod.GET, RequestMethod.POST})
    public String deleteRole(ModelMap model, @PathVariable("id") long roleid, RedirectAttributes redirectAttributes) throws Exception {

        try {

            logger.info("Role has been requested to be deleted id:" + roleid);

            RoleModel role = getRoleById(roleid);

            if (role == null) {
                model.addAttribute("errormessage", "Roles does not exist Role Id:" + roleid);
                 setCommonModel(model);
                 return "/admin/roles/index";

            } else {

                if (role.getAssignedUserCount() > 0) {
                    model.addAttribute("errormessage", "Can't delete a role that has users");
                    setCommonModel(model);
                    return "/admin/roles/index";
                } else {
                    //attempt to delete role
                    restService.deleteRole(roleid);
                }
            }


        } catch (Exception e) {
            String msg = "Error trying to delete  role " + roleid + " Error " + e.getMessage();
            logger.error(msg, e);

            model.addAttribute("errormessage", msg);

            setCommonModel(model);
            return "/admin/roles/index";

        }
            return "redirect:/admin/roles/";
    }



    // This will add all roles which are not selected to the role.permissions for FE display.
    private void populatePermissions(RoleViewModel roleVm) {

        List<PermissionModel> pms;

        RoleModel role = roleVm.getRole();

        switch (role.getType()) {
            case VAULT:
                pms = restService.getVaultPermissions();
                break;

            case SCHOOL:
                pms = restService.getSchoolPermissions();
                break;
            default:
                return;
        }

        //add
        pms.forEach(p -> {
            if (!role.getPermissions().stream().anyMatch(x -> x.getId().equals(p.getId()))) {
                roleVm.addPermission(p);
            }
        });


    }


    @RequestMapping(value = "/admin/roles/{id}", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<RoleViewModel> getRole(@PathVariable("id") long roleid) throws Exception {

        RoleModel role = getRoleById(roleid);



        if (role == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        RoleViewModel roleVm =  new RoleViewModel();
        roleVm.setRole(role);

        populatePermissions(roleVm);

        return ResponseEntity.ok(roleVm);
    }

    @RequestMapping(value="/admin/roles/getvaultpermissions", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<List<PermissionModel>> getPermissionsVault() throws Exception
    {
        List<PermissionModel> permissions = restService.getVaultPermissions();

        return ResponseEntity.ok(permissions);
    }

    @RequestMapping(value="/admin/roles/getschoolpermissions", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<List<PermissionModel>> getPermissionsSchool() throws Exception
    {
        List<PermissionModel> permissions = restService.getSchoolPermissions();

        return ResponseEntity.ok(permissions);
    }



}
