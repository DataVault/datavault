package org.datavaultplatform.webapp.controllers.admin;

import org.datavaultplatform.common.dto.PausedDepositStateDTO;
import org.datavaultplatform.common.model.RoleName;
import org.datavaultplatform.webapp.services.RestService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@ConditionalOnBean(RestService.class)
@RequestMapping("/admin/paused/deposit")
public class AdminPausedDepositStateController implements AdminPausedDepositStateControllerApi {

    private final RestService service;

    public AdminPausedDepositStateController(RestService service) {
        this.service = service;
    }

    @Override
    @GetMapping(value = "/history", produces = MediaType.TEXT_HTML_VALUE)
    @PreAuthorize("hasRole('USER')")
    public ModelAndView showPausedDepositHistory(Authentication auth) {
        boolean hasIsAdminRole = auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).anyMatch(name -> name.equals(RoleName.ROLE_USER));
        Assert.isTrue(hasIsAdminRole, "USER DOES NOT HAVE 'USER' role");
        ModelAndView mav = new ModelAndView();
        mav.setViewName("admin/paused/deposit/history");
        List<PausedDepositStateDTO> pausedStates = service.getPausedDepositStateHistory(10);
        mav.getModel().put("pausedStates", pausedStates);
        return mav;
    }

    @SuppressWarnings("SameReturnValue")
    @Override
    @PreAuthorize("hasRole('IS_ADMIN')")
    @PostMapping("/toggle")
    public String toggleDepositPause(Authentication auth) {
        boolean hasIsAdminRole = auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).anyMatch(name -> name.equals(RoleName.ROLE_IS_ADMIN));
        Assert.isTrue(hasIsAdminRole, "USER DOES NOT HAVE 'IS_ADMIN' role");
        service.toggleDepositPausedState();
        return "redirect:/admin/paused/deposit/history";
    }
}
