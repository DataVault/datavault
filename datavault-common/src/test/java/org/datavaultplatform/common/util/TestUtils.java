package org.datavaultplatform.common.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TestUtils {
    public static <T extends Comparable<T>> List<T> sort(List<T> values) {
        List<T> result = new ArrayList<>(values);
        Collections.sort(result);
        return result;
    }
}
