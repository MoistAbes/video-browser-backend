package dev.zymion.video.browser.app.config.redis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Konfiguracja Redisa dla aplikacji.
 *
 * Redis to szybka baza danych działająca w pamięci (in-memory key-value store),
 * którą wykorzystujemy do przechowywania danych tymczasowych.
 *
 * W tym projekcie służy głównie do przechowywania krótkotrwałych kluczy autoryzacyjnych
 * (tzw. tokenów streamowych) dla użytkowników oglądających wideo.
 *
 * Dzięki Redisowi możemy:
 *  - w prosty sposób generować klucze z czasem życia (TTL, np. 5 minut),
 *  - weryfikować je bardzo szybko przy każdym żądaniu streamu,
 *  - uniknąć konieczności odpytywania bazy danych lub używania JWT dla każdego requestu,
 *    co znacząco zmniejsza obciążenie serwera podczas streamingu.
 *
 * Ten bean tworzy obiekt StringRedisTemplate — uproszczony klient Redisa,
 * który pozwala na łatwe zapisywanie i odczytywanie kluczy typu String.
 */
@Configuration
public class RedisConfig {

    /**
     * Tworzy i udostępnia w kontekście Springa obiekt StringRedisTemplate.
     * Spring automatycznie podstawi RedisConnectionFactory skonfigurowane
     * na podstawie ustawień w pliku application.yml (np. host, port, hasło).
     */
    @Bean
    public StringRedisTemplate redisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }
}
