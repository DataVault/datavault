package org.datavaultplatform.broker.config;

import java.util.function.Predicate;
import java.util.stream.Stream;
import org.springframework.core.env.Environment;

public class ConfigUtils {

  public static boolean isLocal(Environment env) {
    return Stream.of(env.getActiveProfiles())
        .anyMatch(Predicate.isEqual("local"));
  }
}
