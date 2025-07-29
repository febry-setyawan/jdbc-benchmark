package id.co.hibank.benchmark.jdbc.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import jakarta.annotation.PostConstruct;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.sql.SQLException;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeoutException;

@Configuration
public class JdbcConfig {

    @Bean
    public RetryRegistry retryRegistry() {
        RetryConfig config = RetryConfig.custom()
            .maxAttempts(3)
            .waitDuration(Duration.ofMillis(300))
            .retryExceptions(SQLException.class, TimeoutException.class)
            .build();
        return RetryRegistry.of(config);
    }

    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
            .failureRateThreshold(50)
            .waitDurationInOpenState(Duration.ofSeconds(10))
            .slidingWindowSize(10)
            .recordExceptions(SQLException.class, TimeoutException.class)
            .build();
        return CircuitBreakerRegistry.of(config);
    }

    @Bean
    public TimeLimiterRegistry timeLimiterRegistry() {
        TimeLimiterConfig config = TimeLimiterConfig.custom()
            .timeoutDuration(Duration.ofSeconds(3))
            .build();
        return TimeLimiterRegistry.of(config);
    }

    @Bean
    public ScheduledExecutorService scheduledExecutorService() {
        ThreadFactory namedThreadFactory = runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName("resilience4j-scheduler");
            thread.setDaemon(true);
            return thread;
        };
        return Executors.newScheduledThreadPool(4, namedThreadFactory);
    }

    @PostConstruct
    public void logInitialization() {
        System.out.println("[JdbcConfig] Resilience4j beans initialized successfully.");
    }
}