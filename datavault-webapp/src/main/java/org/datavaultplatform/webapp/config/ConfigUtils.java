package org.datavaultplatform.webapp.config;

import java.util.function.Predicate;
import java.util.stream.Stream;
import org.springframework.core.env.Environment;

public class ConfigUtils {

  public static boolean isStandalone(Environment env) {
    return Stream.of(env.getActiveProfiles())
         .anyMatch(Predicate.isEqual("standalone"));
  }

}
