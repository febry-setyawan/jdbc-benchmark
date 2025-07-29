package id.co.hibank.benchmark.jdbc.util;

import org.springframework.util.StopWatch;

public class TimerUtil {
    public static long time(Runnable action) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        action.run();
        stopWatch.stop();
        return stopWatch.getTotalTimeMillis();
    }
}
