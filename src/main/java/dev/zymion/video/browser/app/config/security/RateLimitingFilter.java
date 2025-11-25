package dev.zymion.video.browser.app.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    //TRZEBA TUTAJ JESZCZE DODAC CZYSZCZENIE WPISÓW CO JAKIS CZAS BO POKI CO ZOSTAJA NA ZAWSZE A NIE MOZE TAK BYC
    //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

    // Mapy do śledzenia liczby żądań i czasu pierwszego requestu
    private final Map<String, Integer> requestCounts = new ConcurrentHashMap<>();
    private final Map<String, Long> timestamps = new ConcurrentHashMap<>();

    private static final int MAX_REQUESTS = 5;
    private static final long TIME_WINDOW_MS = 2 * 60 * 1000L; // 15 minut

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Tylko dla konkretnego endpointu
        if ("/auth/login".equals(request.getRequestURI())) {

            String clientIp = request.getRemoteAddr();
            long now = System.currentTimeMillis();

            // Sprawdź czy licznik powinien zostać zresetowany
            timestamps.putIfAbsent(clientIp, now);
            requestCounts.putIfAbsent(clientIp, 0);

            long firstRequestTime = timestamps.get(clientIp);

            if (now - firstRequestTime > TIME_WINDOW_MS) {
                // Reset licznika po 15 minutach
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

//@Component
//public class RateLimitingFilter extends OncePerRequestFilter {
//
//    // A thread-safe map to store the number of requests per client IP
//    private final Map<String, Integer> requestCounts = new ConcurrentHashMap<>();
//
//    // Define maximum allowed requests per minute
//    private static final int MAX_REQUESTS_PER_MINUTE = 5;
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
//            throws ServletException, IOException {
//
//        // Get the client's IP address
//        String clientIp = request.getRemoteAddr();
//
//        // Initialize the count if the IP is new, otherwise get the current count
//        requestCounts.putIfAbsent(clientIp, 0);
//        int requestCount = requestCounts.get(clientIp);
//
//        // If the count exceeds the limit, return a 429 Too Many Requests response
//        if (requestCount >= MAX_REQUESTS_PER_MINUTE) {
//            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
//            response.getWriter().write("Too many requests - please try again later.");
//            return;
//        }
//
//        // Otherwise, increment the request count and proceed with the request
//        requestCounts.put(clientIp, requestCount + 1);
//        filterChain.doFilter(request, response);
//    }
//}
