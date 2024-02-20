package org.datavaultplatform.common.model.custom;

import jakarta.persistence.AttributeConverter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HashMapConverter<K,V> implements AttributeConverter<HashMap<K,V>,byte[]> {

  @Override
  public byte[] convertToDatabaseColumn(HashMap<K, V> customProperties) {
    return encode(customProperties);
  }

  @Override
  public HashMap<K, V> convertToEntityAttribute(byte[] dbData) {
    return decode(dbData);
  }

  @SneakyThrows
  public static <K, V> byte[] encode(HashMap<K, V> props) {
    byte[] encoded;
    try {
      if (props == null) {
        encoded = new byte[0];
      } else {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos)) {
          oos.writeObject(props);
          encoded = baos.toByteArray();
        }
      }
      log.info("encoded [{}] to [{}]bytes", props, encoded.length);
      return encoded;
    } catch (Exception ex) {
      log.error("problem trying to encode properties", ex);
      throw ex;
    }
  }

  @SneakyThrows
  public static <K, V> HashMap<K, V> decode(byte[] encoded) {
    HashMap<K, V> properties;
    try {
      if (encoded == null || encoded.length == 0) {
        properties = new HashMap<>();
        log.info("decoded [{}]bytes to [{}]", 0, properties);
      } else {
        try (ObjectInputStream ois = new ObjectInputStream(
            new ByteArrayInputStream(encoded))) {
          Object obj = ois.readObject();
          properties = (HashMap<K, V>) obj;
        }
        log.info("decoded [{}]bytes to [{}]", encoded.length, properties);
      }
      return properties;
    } catch (Exception ex) {
      log.error("problem trying to decode properties", ex);
      throw ex;
    }
  }

}
