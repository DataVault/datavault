package org.datavaultplatform.common.io;

import java.io.OutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

public class OutputAdapterTest {

  @Test
  void testAllMethodsAreOverridden() {
      Method[] methods = OutputStream.class.getDeclaredMethods();

      Comparator<Method> comparator = Comparator.comparing(Method::getName)
          .thenComparing(Method::getParameterCount)
          .thenComparing(m -> m.getReturnType().getName())
          .thenComparing(m -> Arrays.toString(m.getParameterTypes()));
      Arrays.sort(methods, comparator);

    System.out.println("----------------------------------------------------");
    AtomicInteger counter = new AtomicInteger(1);
    for(Method method : methods) {
      if(Modifier.isStatic(method.getModifiers())) {
        continue;
      }
      System.out.printf("ORIGINAL [%d][%s]%n", counter.getAndIncrement(), method);
    }
    System.out.println("----------------------------------------------------");
    Method[] adaptedMethods = OutputStreamAdapter.class.getDeclaredMethods();
    Arrays.sort(adaptedMethods, comparator);

    counter = new AtomicInteger(1);
    for(Method method : adaptedMethods) {
      if(Modifier.isStatic(method.getModifiers())) {
        continue;
      }
      System.out.printf("ADAPTED [%d][%s]%n", counter.getAndIncrement(), method);
    }
    System.out.println("----------------------------------------------------");
  }
}
