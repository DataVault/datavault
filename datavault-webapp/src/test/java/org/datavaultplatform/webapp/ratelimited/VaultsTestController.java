package org.datavaultplatform.webapp.ratelimited;

import org.springframework.web.bind.annotation.*;

@RestController
public class VaultsTestController {

    @GetMapping("/vaults/isuun/{uun}")
    public String isUUN(@PathVariable String uun) {
        return "uun is [%s]".formatted(uun);
    }

    @GetMapping(value = "/vaults/autocompleteuun/{term}")
    public String autocompleteUUN(@PathVariable String term) {
        return "term is [%s]".formatted(term);
    }
}
