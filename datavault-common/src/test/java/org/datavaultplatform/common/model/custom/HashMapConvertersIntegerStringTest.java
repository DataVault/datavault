package org.datavaultplatform.common.model.custom;

import java.util.HashMap;

public class HashMapConvertersIntegerStringTest extends BaseHashMapConverterTest<Integer, String> {

    @Override
    HashMapConverter<Integer, String> getConverter() {
        return new HashMapConverter.IntegerString();
    }

    @Override
    HashMap<Integer, String> getProperties() {
        HashMap<Integer, String> properties = new HashMap<>();
        for (int i = 0; i < 5; i++) {
            Integer key = random.nextInt();
            String value = randomString();
            properties.put(key, value);
        }
        return properties;
    }
}
