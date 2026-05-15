package org.datavaultplatform.webapp.controllers;


import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Profile("trace")
@Service
public class SimpleRestService {

    private final RestTemplate template;

    public SimpleRestService(@Lazy RestTemplate template) {
        this.template = template;
    }

    public String get(String url) {
        return template.getForObject(url, String.class);
    }
}
