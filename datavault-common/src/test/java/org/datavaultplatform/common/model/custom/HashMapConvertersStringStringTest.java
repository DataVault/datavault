package org.datavaultplatform.common.model.custom;

import java.util.HashMap;

public class HashMapConvertersStringStringTest extends BaseHashMapConverterTest<String, String> {

    @Override
    HashMapConverter<String, String> getConverter() {
        return new HashMapConverter.StringString();
    }

    @Override
    HashMap<String, String> getProperties() {
        HashMap<String, String> properties = new HashMap<>();
        for (int i = 0; i < 5; i++) {
            String key = randomString();
            String value = randomString();
            properties.put(key, value);
        }
        return properties;
    }
}
