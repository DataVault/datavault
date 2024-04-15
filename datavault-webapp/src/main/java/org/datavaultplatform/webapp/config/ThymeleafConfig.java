package org.datavaultplatform.webapp.config;

import nz.net.ultraq.thymeleaf.LayoutDialect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.extras.springsecurity5.dialect.SpringSecurityDialect;
import org.thymeleaf.spring5.SpringTemplateEngine;

import javax.annotation.PostConstruct;

@Configuration
public class ThymeleafConfig {

    @Autowired
    SpringTemplateEngine engine;

    @PostConstruct
    void setupThymeleafLayout() {
        this.engine.addDialect(new LayoutDialect());
    }


}
