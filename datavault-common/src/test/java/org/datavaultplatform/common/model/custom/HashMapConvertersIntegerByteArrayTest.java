package org.datavaultplatform.common.model.custom;

import java.util.Arrays;
import java.util.HashMap;

public class HashMapConvertersIntegerByteArrayTest extends BaseHashMapConverterTest<Integer, byte[]> {

    public static final byte[] BA_1 = new byte[]{Byte.MIN_VALUE, 1, 0, 1, Byte.MAX_VALUE};
    public static final byte[] BA_2 = new byte[]{Byte.MIN_VALUE, 1, 0, 2, Byte.MAX_VALUE};
    public static final byte[] BA_3 = new byte[]{Byte.MIN_VALUE, 1, 0, 3, Byte.MAX_VALUE};
    public static final byte[] BA_4 = new byte[]{Byte.MIN_VALUE, 1, 0, 4, Byte.MAX_VALUE};
    public static final byte[] BA_5 = new byte[]{Byte.MIN_VALUE, 1, 0, 5, Byte.MAX_VALUE};

    @Override
    HashMapConverter<Integer, byte[]> getConverter() {
        return new HashMapConverter.IntegerByteArray();
    }

    @Override
    HashMap<Integer, byte[]> getProperties() {
        HashMap<Integer, byte[]> properties = new HashMap<>();
        properties.put(random.nextInt(), BA_1);
        properties.put(random.nextInt(), BA_2);
        properties.put(random.nextInt(), BA_3);
        properties.put(random.nextInt(), BA_4);
        properties.put(random.nextInt(), BA_5);
        return properties;
    }

    boolean areValuesEqual(byte[] v1, byte[] v2) {
        return Arrays.equals(v1, v2);
    }
}
