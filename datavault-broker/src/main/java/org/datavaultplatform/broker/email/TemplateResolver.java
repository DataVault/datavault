package org.datavaultplatform.broker.email;


/* We need this to stick the "mail-templates/" prefix on the velocity template names
 * for example: test.vm -> mail-templates/test.vm
 */
public interface TemplateResolver {

  String resolve(String templateName);

}
