package org.datavaultplatform.common.timeout;


import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 Base class : used for testing junit test/integration-test timeouts.
 <p>
 {@link org.datavaultplatform.common.timeout.TimeoutTest}
 <p>
 {@link org.datavaultplatform.common.timeout.TimeoutIT}
 */
public abstract class BaseTimeoutTest {

    static {

        Map<String, Object> junitProps = new HashMap<>();
        System.getProperties().forEach((name, value) -> {
            if (name instanceof String nameStr) {
                if (StringUtils.startsWith(nameStr, "junit")) {
                    junitProps.put(nameStr, value);
                }
            }
        });
        System.out.println("START junit properties");
        List<String> keys = new ArrayList<>(junitProps.keySet());
        int count = keys.size();
        for (int i = 0; i < count; i++) {
            String key = keys.get(i);
            int index = i + 1;
            Object value = junitProps.get(key);
            System.out.printf("junit property [%02d/%02d] name[%s] value[%s]%n", index, count, key, value);
        }
        System.out.println("END junit properties");

    }

    private final int perIterationSleep;
    private final int numberOfIterations;

    protected BaseTimeoutTest(int perIterationSleep, int numberOfIterations) {
        this.perIterationSleep = perIterationSleep;
        this.numberOfIterations = numberOfIterations;
    }

    @Test
    @SneakyThrows
    public void testTimeout() {
        long start = System.currentTimeMillis();
        try {
            System.out.println("START");
            for (int i = 0; i < numberOfIterations; i++) {
                System.out.printf("Sleeping %d secs [%02d/20]%n", perIterationSleep, i + 1);
                TimeUnit.SECONDS.sleep(perIterationSleep);
            }
            System.out.println("NOT TIMED OUT");
        } finally {
            long diff = System.currentTimeMillis() - start;
            System.out.printf("FIN took [%d]ms%n", diff);
        }
    }
}
