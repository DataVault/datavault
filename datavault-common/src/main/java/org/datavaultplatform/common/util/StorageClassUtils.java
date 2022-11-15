package org.datavaultplatform.common.util;

import java.lang.reflect.Constructor;
import java.util.Map;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.storage.impl.SFTPFileSystem;

@Slf4j
public class StorageClassUtils {

  @SneakyThrows
  public static <T> T createStorage(String storageClassName, Map<String,String> properties, Class<T> clazz, StorageClassNameResolver storageClassNameResolver) {
    log.info("Storage : class[{}], properties[{}]", storageClassName, properties);
    String resolvedClassName = storageClassNameResolver.resolveStorageClassName(storageClassName);
    Class<?> storageClass = Class.forName(resolvedClassName);
     if (!clazz.isAssignableFrom(storageClass)) {
      String msg = String.format("The class [%s] does not inherit from [%s]",
          storageClassName, clazz.getName());
      throw new IllegalArgumentException(msg);
    }
    Class<? extends T> subClass = storageClass.asSubclass(clazz);
    Constructor<? extends T> constructor = subClass.getConstructor(String.class, Map.class);
    T result = constructor.newInstance(storageClassName, properties);
    return result;
  }
}
