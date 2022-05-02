package org.datavaultplatform.broker.email;


/* We need this to stick the "email-templates/" prefix on the velocity template names
 * for example: test.vm -> email-templates/test.vm
 */
public interface TemplateResolver {

  String resolve(String templateName);

}
