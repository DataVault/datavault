package org.datavaultplatform.webapp.controllers.standalone.fm;

import javax.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/test")
@Profile("standalone")
public class HelloController {

  private static final Logger LOG = LoggerFactory.getLogger(HelloController.class);

  @GetMapping("/hello")
  public String hello(Model model,
      @RequestParam(value="name", required=false, defaultValue="World") String name, HttpSession session) {
    String msg = String.format("SESSION ID[%s][%s]\n",session.getId(),session.isNew());
    LOG.info(msg);
    model.addAttribute("name", name);
    return "test/hello";
  }

}
