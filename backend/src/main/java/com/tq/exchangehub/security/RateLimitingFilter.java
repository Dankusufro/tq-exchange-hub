package com.tq.exchangehub.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.ConsumptionProbe;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.logstash.logback.argument.StructuredArguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RateLimitingFilter.class);
    private static final String[] SKIPPED_PATHS = {
        "/actuator/prometheus",
        "/actuator/health",
        "/actuator/health/**",
        "/v3/api-docs/**",
        "/swagger-ui.html",
        "/swagger-ui/**"
    };
    private static final long RATE_LIMIT_CAPACITY = 100;
    private static final Duration REFILL_PERIOD = Duration.ofMinutes(1);

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final AntPathMatcher matcher = new AntPathMatcher();
    private final Counter allowedRequests;
    private final Counter limitedRequests;

    public RateLimitingFilter(MeterRegistry meterRegistry) {
        this.allowedRequests = Counter
                .builder("security.rate_limiting.requests")
                .tag("outcome", "allowed")
                .description("Total number of requests allowed by the security rate limiter")
                .register(meterRegistry);
        this.limitedRequests = Counter
                .builder("security.rate_limiting.requests")
                .tag("outcome", "limited")
                .description("Total number of requests blocked by the security rate limiter")
                .register(meterRegistry);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        for (String pattern : SKIPPED_PATHS) {
            if (matcher.match(pattern, path)) {
                return true;
            }
        }
        return "OPTIONS".equalsIgnoreCase(request.getMethod());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String clientKey = resolveClientKey(request);
        Bucket bucket = buckets.computeIfAbsent(clientKey, key -> newBucket());

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        if (!probe.isConsumed()) {
            limitedRequests.increment();
            long waitForRefillNs = probe.getNanosToWaitForRefill();
            long retryAfterSeconds = (long) Math.ceil(waitForRefillNs / 1_000_000_000.0);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setHeader("Retry-After", String.valueOf(Math.max(retryAfterSeconds, 1)));
            response.setHeader("X-Rate-Limit-Retry-After-Seconds", String.valueOf(Math.max(retryAfterSeconds, 1)));
            LOGGER.warn("Rate limit exceeded",
                    StructuredArguments.keyValue("clientIp", clientKey),
                    StructuredArguments.keyValue("retryAfterSeconds", retryAfterSeconds));
            return;
        }

        allowedRequests.increment();
        response.setHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
        filterChain.doFilter(request, response);
    }

    private Bucket newBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(RATE_LIMIT_CAPACITY)
                .refillIntervally(RATE_LIMIT_CAPACITY, REFILL_PERIOD)
                .build();
        return Bucket4j.builder().addLimit(limit).build();
    }

    private String resolveClientKey(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
