package org.datavaultplatform.common.model.dao;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

public class DaoMethodsTest {

  @Test
  void extractDaoMethods() throws IOException, ClassNotFoundException {

    Class[] classes = getClasses("org.datavaultplatform.common.model.dao.custom");
    int totalMethodCount = 0;
    List<Class> interfaces = Stream.of(classes)
        .filter(claz -> claz.isInterface())
        .filter(claz -> claz.getSimpleName().endsWith("DAO"))
        .sorted(Comparator.comparing(Class::getSimpleName))
        .collect(Collectors.toList());
    int count = 1;
    for(Class inter : interfaces){
      System.out.printf("%3d %s%n",count++,inter.getName());
      List<Method> interMethods = getMethodNames(inter);
      int icount=1;
      for(Method interMethod : interMethods){
        totalMethodCount++;
        System.out.printf("\ttotal[%3d]  [%3d] [%s] %n",totalMethodCount,icount++,interMethod.getName());
      }
    }
    System.out.println("--------------------------------------------");
    System.out.printf("total method count,%d%n",totalMethodCount);
    System.out.println("--------------------------------------------");
  }

  List<Method> getMethodNames(Class<?> claz) {
    return Stream.of(claz.getDeclaredMethods())
        .filter(m -> java.lang.reflect.Modifier.isPublic(m.getModifiers()))
        .sorted(Comparator.comparing(Method::getName))
        .collect(Collectors.toList());
  }

  private static Class[] getClasses(String packageName)
      throws ClassNotFoundException, IOException {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    assert classLoader != null;
    String path = packageName.replace('.', '/');
    Enumeration resources = classLoader.getResources(path);
    List<File> dirs = new ArrayList<>();
    while (resources.hasMoreElements()) {
      URL resource = (URL) resources.nextElement();
      dirs.add(new File(resource.getFile()));
    }
    ArrayList<Class<?>> classes = new ArrayList<>();
    for (File directory : dirs) {
      classes.addAll(findClasses(directory, packageName));
    }
    return classes.toArray(new Class[classes.size()]);
  }
  private static List<Class<?>> findClasses(File directory, String packageName) throws ClassNotFoundException {
    List classes = new ArrayList();
    if (!directory.exists()) {
      return classes;
    }
    File[] files = directory.listFiles();
    for (File file : files) {
      if (file.isDirectory()) {
        assert !file.getName().contains(".");
        classes.addAll(findClasses(file, packageName + "." + file.getName()));
      } else if (file.getName().endsWith(".class")) {
        classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
      }
    }
    return classes;
  }
}
