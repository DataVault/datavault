package org.datavaultplatform.common.model.custom;

import org.apache.commons.lang3.SerializationUtils;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class BaseHashMapConverterTest<K, V> {

  protected Random random = new Random();

  abstract HashMapConverter<K, V> getConverter();

  protected String randomString() {
    return UUID.randomUUID().toString();
  }

  abstract HashMap<K, V> getProperties();

  @Test
  void testEncodeDecode() {
    HashMap<K, V> properties = getProperties();
    byte[] encoded = HashMapConverter.encode(properties);
    HashMap<K, V> properties2 = HashMapConverter.decode(encoded);

    compareProperties(properties, properties2);
  }

  @Test
  void testConvertThereAndBack() {
    HashMap<K, V> properties = getProperties();
    HashMapConverter<K, V> converter = getConverter();
    byte[] encoded = converter.convertToDatabaseColumn(properties);
    HashMap<K, V> properties2 = converter.convertToEntityAttribute(encoded);
    compareProperties(properties, properties2);
  }

  private void compareProperties(HashMap<K, V> properties, HashMap<K, V> properties2) {
    assertEquals(properties.keySet(), properties2.keySet());

    for (K k : properties.keySet()) {
      V v1 = properties.get(k);
      V v2 = properties2.get(k);
      assertTrue(areValuesEqual(v1, v2));
    }
  }

  @Test
  void convertNullAndEmptyMaps() {
    HashMapConverter<K, V> converter = getConverter();
    byte[] converted1 = converter.convertToDatabaseColumn(null);
    byte[] converted2 = converter.convertToDatabaseColumn(new HashMap<>());
    byte[] converted3 = converter.convertToDatabaseColumn(new HashMap<>());
    assertEquals(0, converted1.length);
    assertEquals(82, converted2.length);
    assertEquals(82, converted3.length);
  }

  @Test
  void convertNullAndEmptyArrays() {

    HashMapConverter<K, V> converter = getConverter();
    HashMap<K, V> map1 = converter.convertToEntityAttribute(null);
    assertTrue(map1.isEmpty());

    HashMap<K, V> map2 = converter.convertToEntityAttribute(new byte[0]);
    assertTrue(map2.isEmpty());
  }

  boolean areValuesEqual(V v1, V v2) {
    return v1.equals(v2);
  }

  @Test
  void testDeSerializationFailure() throws Exception {
    byte[] data = SerializationUtils.serialize("hello world");
    HashMapConverter<K, V> converter = getConverter();
    RuntimeException ex = assertThrows(RuntimeException.class, () -> converter.convertToEntityAttribute(data));
    assertThat(ex).hasMessage("expected HashMap got java.lang.String");
  }
}
