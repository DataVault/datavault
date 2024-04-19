package org.datavaultplatform.common.model.custom;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HashMapConverterTest {

  final HashMapConverter converter = new HashMapConverter();


  private String random() {
    return UUID.randomUUID().toString();
  }

  @Test
  void testEncodeDecode() {
    HashMap<String,String> properties = new HashMap<>();
    for(int i=0;i<5;i++){
      String key = random();
      String value = random();
      properties.put(key,value);
    }
    byte[] encoded = HashMapConverter.encode(properties);
    HashMap<String,String> properties2 = HashMapConverter.decode(encoded);

    assertEquals(properties, properties2);
  }


  @Test
  void testConvertThereAndBack() {
    HashMap<String,String> properties = new HashMap<>();
    for(int i=0;i<5;i++){
      String key = random();
      String value = random();
      properties.put(key,value);
    }
    byte[] encoded = converter.convertToDatabaseColumn(properties);
    HashMap<String,String> properties2 = converter.convertToEntityAttribute(encoded);

    assertEquals(properties, properties2);
  }
  @Test
  void convertNullAndEmptyMaps() {
    byte[] converted1 = converter.convertToDatabaseColumn(null);
    byte[] converted2 = converter.convertToDatabaseColumn(new HashMap<>());
    byte[] converted3 = converter.convertToDatabaseColumn(new HashMap<>());
    assertEquals(0, converted1.length);
    assertEquals(82, converted2.length);
    assertEquals(82, converted3.length);
  }

  @Test
  void convertNullAndEmptyArrays() {

    HashMap<String,String> map1 = converter.convertToEntityAttribute(null);
    assertTrue(map1.isEmpty());

    HashMap<String,String> map2 = converter.convertToEntityAttribute(new byte[0]);
    assertTrue(map2.isEmpty());
  }
}
