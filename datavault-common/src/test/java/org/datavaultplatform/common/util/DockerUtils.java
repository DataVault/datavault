package org.datavaultplatform.common.util;

import java.nio.file.Files;
import java.nio.file.Paths;

public class DockerUtils {

  public static boolean isRunningInsideDocker() {
    boolean result = insideDocker();
    System.out.printf("Inside Docker ? [%s]%n", result);
    System.out.flush();
    return result;
  }

  public static boolean isRunningOutsideDocker() {
    return !isRunningInsideDocker();
  }

  private static boolean insideDocker(){
    try{
      return Files.exists(Paths.get("/.dockerenv"));
    }catch(Exception ex){
      return false;
    }
  }

}
