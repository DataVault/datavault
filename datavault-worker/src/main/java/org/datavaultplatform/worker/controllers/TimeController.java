package org.datavaultplatform.worker.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
public class TimeController {

    @Autowired
    private java.time.Clock clock;

    @GetMapping("/time")
    public TimeInfo getTime() {
        return new TimeInfo(LocalDateTime.now(clock));
    }
    public record TimeInfo(LocalDateTime time){

    }
}
