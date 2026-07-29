package org.datavaultplatform.webapp.controllers;

import lombok.SneakyThrows;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FaviconController implements FaviconControllerApi {

  @Override
  @GetMapping(value = "/favicon.ico", produces = IMAGE_X_ICON)
  @SneakyThrows
  public String favicon() {
    return "forward:/resources/favicon.ico";
  }

}
