package org.datavaultplatform.broker.controllers;


import jakarta.servlet.http.HttpServletRequest;
import org.datavaultplatform.broker.services.PausedDepositStateService;
import org.datavaultplatform.common.dto.PausedDepositStateDTO;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/paused/deposit")
public class PausedDepositStateController {

    private final PausedDepositStateService service;

    public PausedDepositStateController(PausedDepositStateService service) {
        this.service = service;
    }

    @GetMapping("/state")
    public PausedDepositStateDTO isPaused() {
        return  new PausedDepositStateDTO(service.getCurrentState());
    }

    @GetMapping(value = {"/history", "history/{limit}"})
    public List<PausedDepositStateDTO> showPauseHistory(HttpServletRequest request, @PathVariable Optional<Integer> limit) {
        int actualLimit = limit.orElse(10);
        return service.getRecentEntries(actualLimit).stream().map(PausedDepositStateDTO::create).collect(Collectors.toList());
    }

    @PostMapping("/toggle")
    public ResponseEntity<Void>  togglePaused(Authentication auth) {
        boolean hasIsAdminRole = auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).anyMatch(name -> name.equals("ROLE_ADMIN"));
        Assert.isTrue(hasIsAdminRole, "USER DOES NOT HAVE 'IS_ADMIN' role");
        service.toggleState();
        HttpHeaders headers = new HttpHeaders();
        return new ResponseEntity<>(headers,HttpStatus.OK);
    }
}
