package id.co.hibank.benchmark.jdbc.util;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.function.Supplier;

import org.springframework.stereotype.Component;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JdbcResilienceHelper {

    private final Retry retry;
    private final CircuitBreaker circuitBreaker;
    private final TimeLimiter timeLimiter;
    private final ScheduledExecutorService scheduler;

    public JdbcResilienceHelper(
        RetryRegistry retryRegistry,
        CircuitBreakerRegistry circuitBreakerRegistry,
        TimeLimiterRegistry timeLimiterRegistry,
        ScheduledExecutorService scheduler
    ) {
        this.retry = retryRegistry.retry("jdbc-retry");
        this.circuitBreaker = circuitBreakerRegistry.circuitBreaker("jdbc-cb");
        this.timeLimiter = timeLimiterRegistry.timeLimiter("jdbc-tl");
        this.scheduler = scheduler;
    }

    /**
     * Execute a supplier with retry, circuit breaker, time limiter, and fallback (sync/blocking).
     */
    public <T> T executeResilient(Supplier<T> supplier, Function<Throwable, T> fallback) {
        Supplier<T> timeLimitedSupplier = () -> {
            Future<T> future = scheduler.submit(supplier::get);
            try {
                long timeout = timeLimiter.getTimeLimiterConfig().getTimeoutDuration().toMillis();
                return future.get(timeout, TimeUnit.MILLISECONDS);
            } catch (TimeoutException ex) {
                // Melemparkan TimeoutException secara langsung
                throw new CompletionException("Operation timed out", ex); // Bungkus dalam CompletionException untuk konsistensi Future
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt(); // Pastikan thread diinterupsi
                // Melemparkan InterruptedException secara langsung
                throw new CompletionException("Thread was interrupted", ex); // Bungkus dalam CompletionException
            } catch (Exception e) {
                // Untuk pengecualian umum lainnya, bungkus dalam CompletionException
                throw new CompletionException("Unexpected failure during time-limited operation", e);
            }
        };

        return Decorators.ofSupplier(() -> TimerUtil.time(timeLimitedSupplier))
            .withRetry(retry)
            .withCircuitBreaker(circuitBreaker)
            .withFallback(List.of(Exception.class), fallback) // Fallback akan menangani Exception secara umum
            .get();
    }

    public <T> Optional<T> safeOptional(Supplier<T> supplier) {
        return Optional.ofNullable(executeResilient(supplier, ex -> {
            log.warn("JDBC operation failed: {}", ex.getMessage(), ex); // Sertakan 'ex' untuk stack trace lengkap
            return null;
        }));
    }

    public void runSafe(Runnable runnable) {
        executeResilient(() -> {
            runnable.run();
            return true;
        }, ex -> {
            log.error("Safe run failed", ex);
            return false;
        });
    }

    public <T> T safe(Supplier<T> supplier) {
        return executeResilient(supplier, ex -> {
            log.warn("JDBC operation failed: {}", ex.getMessage(), ex); // Sertakan 'ex' untuk stack trace lengkap
            return null;
        });
    }
}
