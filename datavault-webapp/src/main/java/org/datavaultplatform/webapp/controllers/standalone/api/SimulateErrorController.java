package org.datavaultplatform.webapp.controllers.standalone.api;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.webapp.controllers.auth.ValidationExceptionHandler;
import org.datavaultplatform.webapp.exception.EntityNotFoundException;
import org.datavaultplatform.webapp.exception.ForbiddenException;
import org.datavaultplatform.webapp.exception.InvalidUunException;
import org.datavaultplatform.webapp.model.test.EmailInfo;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/test")
@Profile("standalone")
public class SimulateErrorController {

  @GetMapping("/oops")
  public String throwError(){
      throw new RuntimeException("SimulatedError");
  }

  @GetMapping("/forbidden")
  public String forbidden() {
    throw new ForbiddenException();
  }

  @GetMapping("/entity-not-found")
  public String entityNotFound() {
    throw new EntityNotFoundException(String.class, "id-101");
  }

  @GetMapping(value = "/invalid-uun")
  public String invalidUUN() throws InvalidUunException {
    throw new InvalidUunException("blah");
  }

  /**
   * an invalid email address will cause a BindException to be handled by ValidationExceptionHandler
   * @see ValidationExceptionHandler
   */
  @PostMapping(value = "/email", consumes = MediaType.APPLICATION_JSON_VALUE)
  public EmailInfo email(@RequestBody @Valid EmailInfo info) {
    return info;
  }

}
