package dev.zymion.video.browser.app.config.security;

import dev.zymion.video.browser.app.services.StreamKeyService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtr weryfikujący dostęp do endpointów streamingowych.
 *
 * Dla każdego requestu do /stream/normal/**:
 *  - sprawdza obecność parametru authKey
 *  - weryfikuje klucz w Redisie
 *  - jeśli klucz jest niepoprawny lub wygasł → zwraca 401 Unauthorized
 */
@Component
@RequiredArgsConstructor
public class StreamAuthFilter extends OncePerRequestFilter {

    private final StreamKeyService streamKeyService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        System.out.println("STREAM FILTER IS RUNNING");

        // filtrujemy tylko endpointy /stream/normal/**
        if (path.startsWith("/stream/normal") || path.startsWith("/stream/convert")) {
            String authKey = request.getParameter("authKey");

            if (authKey == null || !streamKeyService.isKeyValid(authKey)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Unauthorized: invalid or expired stream key.");
                return; // przerywamy dalsze przetwarzanie requestu
            }
        }

        // jeśli klucz poprawny lub endpoint nie wymaga auth → kontynuujemy
        filterChain.doFilter(request, response);
    }
}