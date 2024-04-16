package org.datavaultplatform.worker.retry;

import java.time.Duration;

@SuppressWarnings("UnnecessaryLocalVariable")
public class DurationHelper {

    public static String formatHoursMinutesSeconds(long ms) {
        Duration duration = Duration.ofMillis(ms);
        if(ms < 1000){
            String timeInMs = String.format("%03dms", ms);
            return timeInMs;
        }else{
            long HH = duration.toHours();
            long MM = duration.toMinutes();
            long SS = duration.getSeconds();
            String timeInHHMMSS = String.format("%02dh:%02dm:%02ds", HH, MM, SS);
            return timeInHHMMSS;
        }
    }
}
