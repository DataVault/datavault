package org.datavaultplatform.common.util;


import org.awaitility.Awaitility;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

public class TestUtils {
    
    public static <T extends Comparable<T>> List<T> sort(List<T> values) {
        List<T> result = new ArrayList<>(values);
        Collections.sort(result);
        return result;
    }
    
    public static void waitUntil(Callable<Boolean> callable) {
        waitUntil(Duration.ofMinutes(5), callable);
    }
    
    public static void waitUntil(Duration duration, Callable<Boolean> callable) {
        Awaitility.await().atMost(duration).until(callable);
    }
    public static void waitUntil(Duration duration, Duration pollInterval, Callable<Boolean> callable) {
        Awaitility.await().atMost(duration).pollInterval(pollInterval).until(callable);
    }
}
