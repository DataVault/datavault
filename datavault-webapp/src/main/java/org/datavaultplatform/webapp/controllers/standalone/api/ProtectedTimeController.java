package org.datavaultplatform.webapp.controllers.standalone.api;

import java.time.LocalDateTime;
import org.datavaultplatform.test.Time;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
@Profile("standalone")
public class ProtectedTimeController {

  @ResponseBody
  @RequestMapping("/protected/time")
  public Time getTime(){
      return new Time(LocalDateTime.now());
  }
}
