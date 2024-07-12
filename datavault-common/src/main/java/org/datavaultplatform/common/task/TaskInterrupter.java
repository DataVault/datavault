package org.datavaultplatform.common.task;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.util.Utils;
import org.springframework.util.Assert;

import java.util.function.Predicate;

@Slf4j
public abstract class TaskInterrupter {

    private static final ThreadLocal<Checker> TL = ThreadLocal.withInitial(() -> null);

    public static synchronized void checkForInterrupt(Event event) {
        // extra checks to ensure that we can't interrupt a task in production
        if (!Utils.isRunningWithinTest()) {
            log.trace("no interrupt outside tests");
            return;
        }
        Checker checker = TL.get();
        if (checker == null) {
            log.trace("no checker - no interrupt after event[{}]", event);
            return;
        }
        if (checker.doInterruptAfterEvent(event)) {
            String msg = "TaskInterrupter label[%s] - interrupt after event[%s]".formatted(checker.label, event);
            log.info(msg);
            throw new TaskInterruptedException(msg);
        } else {
            log.info("no interrupt after event[{}]", event);
        }
    }

    public record Checker(Predicate<Event> predicate, String label) {
        public Checker {
            Assert.isTrue(predicate != null, "The predicate cannot be null");
            Assert.isTrue(StringUtils.isNotBlank(label), "The label cannot be null");
        }
        public boolean doInterruptAfterEvent(Event event) {
            return predicate.test(event);
        }
    }
    public static Checker getInterrupterCheck() {
        return TL.get();
    }

    public static void setInterrupterCheck(Checker checker) {
        TL.set(checker);
    }
    public static void clear() {
        TL.remove();
    }
}
