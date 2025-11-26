package dev.zymion.video.browser.app.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private final Map<String, Integer> requestCounts = new ConcurrentHashMap<>();
    private final Map<String, Long> timestamps = new ConcurrentHashMap<>();

    private static final int MAX_REQUESTS = 5;
    private static final long TIME_WINDOW_MS = 15 * 60 * 1000L; // 15 minut

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if ("/auth/login".equals(request.getRequestURI())) {

            String clientIp = request.getRemoteAddr();
            long now = System.currentTimeMillis();

            timestamps.putIfAbsent(clientIp, now);
            requestCounts.putIfAbsent(clientIp, 0);

            long firstRequestTime = timestamps.get(clientIp);

            if (now - firstRequestTime > TIME_WINDOW_MS) {
                requestCounts.put(clientIp, 0);
                timestamps.put(clientIp, now);
            }

            int requestCount = requestCounts.get(clientIp);

            if (requestCount >= MAX_REQUESTS) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.getWriter().write("Too many requests - please try again later.");
                return;
            }

            requestCounts.put(clientIp, requestCount + 1);
        }

        filterChain.doFilter(request, response);
    }
}
