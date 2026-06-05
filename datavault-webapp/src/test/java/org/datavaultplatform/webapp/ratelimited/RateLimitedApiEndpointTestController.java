package org.datavaultplatform.webapp.ratelimited;

import org.springframework.web.bind.annotation.*;

import java.time.Clock;
import java.time.Instant;

@RestController
@RequestMapping("/api")
public class RateLimitedApiEndpointTestController {

    private final Clock clock;

    public RateLimitedApiEndpointTestController(Clock clock) {
        this.clock = clock;
    }

    @SuppressWarnings("SameReturnValue")
    @RequestMapping("/hello")
    public String hello() {
        return "Hello World!";
    }

    @RequestMapping("/time")
    public TimeInfo time() {
        return new TimeInfo(clock.instant());
    }

    @RequestMapping("/protected/hello")
    public String protectedHello() {
        return hello();
    }

    @RequestMapping("/protected/greeting/{msg}")
    public String protectedGreeting(@PathVariable String msg) {
        return "hello " + msg;
    }

    @RequestMapping("/greeting/{msg}")
    public String greeting(@PathVariable String msg) {
        return "hello " + msg;
    }

    @RequestMapping("/protected/time")
    public TimeInfo protectedTime() {
        return time();
    }

    @RequestMapping("/limited/test")
    public String rateLimited(@RequestParam String msg) {
        return "test-[%s]".formatted(msg);
    }

    @RequestMapping("/protected/limited/{msg}")
    public String protectedRateLimited(@PathVariable String msg) {
        return msg;
    }

    public record TimeInfo(Instant time) {
    }

    @GetMapping("/vaults/isuun/{uun}")
    public String isUUN(@PathVariable String uun) {
        return "uun is [%s]".formatted(uun);
    }

    @GetMapping(value = "/vaults/autocompleteuun/{term}")
    public String autocompleteUUN(@PathVariable String term) {
        return "term is [%s]".formatted(term);
    }
}
