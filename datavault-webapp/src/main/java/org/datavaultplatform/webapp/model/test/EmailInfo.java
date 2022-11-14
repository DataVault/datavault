package org.datavaultplatform.webapp.model.test;

import javax.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class EmailInfo {

  @Email(message = "Not a valid Email Address")
  private String email;

}
