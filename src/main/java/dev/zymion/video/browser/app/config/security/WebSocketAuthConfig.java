package dev.zymion.video.browser.app.config.security;

import dev.zymion.video.browser.app.services.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class WebSocketAuthConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    // Dobrze jest mieć logger do śledzenia prób połączeń

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor =
                        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                    String token = extractToken(accessor);

                    if (token != null) {
                        try {
                            String username = jwtService.extractUsername(token);
                            if (username != null && jwtService.isTokenValid(token, username)) {
                                UserDetails user = userDetailsService.loadUserByUsername(username);

                                UsernamePasswordAuthenticationToken authentication =
                                        new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

                                accessor.setUser(authentication);
                                log.info("User '{}' connected to WebSocket", username);
                                return message;
                            }
                        } catch (Exception e) {
                            // Logujemy błąd walidacji, ale nie rzucamy dalej, aby uniknąć stack trace w logach
                            log.warn("WebSocket connection failed due to invalid token: {}", e.getMessage());
                        }
                    }
                    
                    // Jeśli doszliśmy tutaj, token był nieprawidłowy lub go nie było.
                    // Rzucenie wyjątku jest jednym ze sposobów przerwania połączenia.
                    throw new IllegalArgumentException("Authentication failed. Invalid or missing JWT token.");
                }
                return message;
            }
            
            private String extractToken(StompHeaderAccessor accessor) {
                List<String> authHeaders = accessor.getNativeHeader("Authorization");
                if (authHeaders != null && !authHeaders.isEmpty()) {
                    String authHeader = authHeaders.getFirst();
                    if (authHeader != null && authHeader.startsWith("Bearer ")) {
                        return authHeader.substring(7);
                    }
                }
                return null;
            }
        });
    }
}
