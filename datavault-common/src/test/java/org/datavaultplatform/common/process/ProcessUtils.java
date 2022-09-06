package org.datavaultplatform.common.process;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ProcessUtils {

  public static ProcessBuilder exec(Class clazz, List<String> jvmArgs, List<String> args) {
    String javaHome = System.getProperty("java.home");
    String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
    String classpath = System.getProperty("java.class.path");
    String className = clazz.getName();

    List<String> command = new ArrayList<>();
    command.add(javaBin);
    command.addAll(jvmArgs);
    command.add("-cp");
    command.add(classpath);
    command.add(className);
    command.addAll(args);

    return new ProcessBuilder(command);
  }
}
