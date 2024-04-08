package org.datavaultplatform.broker.controllers;


import jakarta.servlet.http.HttpServletRequest;
import org.datavaultplatform.broker.services.PausedStateService;
import org.datavaultplatform.common.dto.PausedStateDTO;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/admin")
public class PauseStateController {

    private final PausedStateService service;

    public PauseStateController(PausedStateService service) {
        this.service = service;
    }

    @GetMapping("/paused/state")
    public PausedStateDTO isPaused() {
        return  new PausedStateDTO(service.getCurrentState());
    }

    @GetMapping(value = {"/paused/history", "/paused/history/{limit}"})
    public List<PausedStateDTO> showPauseHistory(HttpServletRequest request, @PathVariable Optional<Integer> limit) {
        int actualLimit = limit.orElse(10);
        return service.getRecentEntries(actualLimit).stream().map(PausedStateDTO::create).toList();
    }

    @PostMapping("/paused/toggle")
    public ResponseEntity<Void>  togglePaused(Authentication auth) {
        boolean hasIsAdminRole = auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).anyMatch(name -> name.equals("ROLE_ADMIN"));
        Assert.isTrue(hasIsAdminRole, "USER DOES NOT HAVE 'IS_ADMIN' role");
        service.toggleState();
        HttpHeaders headers = new HttpHeaders();
        return new ResponseEntity<>(headers,HttpStatus.OK);
    }
}
