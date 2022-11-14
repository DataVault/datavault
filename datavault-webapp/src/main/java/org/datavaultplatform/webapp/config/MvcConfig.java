package org.datavaultplatform.webapp.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

  @Autowired
  Environment env;

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry
        .addResourceHandler("/resources/**")
        .addResourceLocations("classpath:/resources/");
  }

  @Override
  public void addViewControllers (ViewControllerRegistry registry) {

    if(ConfigUtils.isStandalone(env)) {
      //For standalone only : mapping urls directly to views
      mapUrlDirectToView(registry, "/index", "index");
      mapUrlDirectToView(registry, "/secure", "secure");
    }
  }

  private void mapUrlDirectToView(ViewControllerRegistry registry, String urlPath, String viewName ){
    ViewControllerRegistration r = registry.addViewController(urlPath);
    r.setViewName(viewName);
    //setting status code
    r.setStatusCode(HttpStatus.OK);
  }

}
