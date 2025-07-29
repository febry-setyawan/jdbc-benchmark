package id.co.hibank.benchmark.jdbc.util;

import java.util.function.Supplier;

import org.springframework.util.StopWatch;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TimerUtil {

    public static long time(Runnable action) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        action.run();
        stopWatch.stop();
        return stopWatch.getTotalTimeMillis();
    }

    public static <T> T time(Supplier<T> supplier) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        T result = supplier.get();
        stopWatch.stop();
        long millis = stopWatch.getTotalTimeMillis();
        log.debug("JDBC action completed in {} ms", millis); // optional log
        return result;
    }
}
