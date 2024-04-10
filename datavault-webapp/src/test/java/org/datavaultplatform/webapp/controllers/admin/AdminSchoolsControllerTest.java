package org.datavaultplatform.webapp.controllers.admin;

import io.vavr.collection.List;
import org.datavaultplatform.common.model.Group;
import org.datavaultplatform.common.model.RoleModel;
import org.datavaultplatform.webapp.services.ForceLogoutService;
import org.datavaultplatform.webapp.services.RestService;
import org.datavaultplatform.webapp.services.UserLookupService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.servlet.ModelAndView;

import java.security.Principal;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminSchoolsControllerTest {
    
    Group mGroup = Mockito.mock(Group.class);
    Principal mPrincipal = Mockito.mock(Principal.class);
    RestService mRestService = Mockito.mock(RestService.class);
    UserLookupService mUserLookupService = Mockito.mock(UserLookupService.class);
    ForceLogoutService mForceLogoutService = Mockito.mock(ForceLogoutService.class);
    
    AdminSchoolsController controller  = new AdminSchoolsController(mRestService, mUserLookupService, mForceLogoutService);

    @Test
    void testGetSchoolRoleAssignmentsPage() {
        RoleModel roleModel1 = new RoleModel();
        RoleModel roleModel2 = new RoleModel();
        
        roleModel1.setId(111L);
        roleModel1.setName("RoleModel1-name");

        roleModel2.setId(222L);
        roleModel2.setName("RoleModel2-name");

        when(mRestService.getGroup("school")).thenReturn(mGroup);

        when(mRestService.getSchoolRoles()).thenReturn(Arrays.asList(roleModel1, roleModel2));
        ModelAndView mav = controller.getSchoolRoleAssignmentsPage("school", mPrincipal);   
        
        verify(mRestService).getGroup("school");
    }
}