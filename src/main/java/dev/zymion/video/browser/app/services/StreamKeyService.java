package dev.zymion.video.browser.app.services;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

/**
 * Serwis odpowiedzialny za generowanie i walidację tymczasowych kluczy
 * autoryzacyjnych dla streamingu wideo.
 *
 * Klucze są przechowywane w Redisie z określonym czasem życia (TTL),
 * np. 1 godzina. Dzięki temu aplikacja może szybko weryfikować, czy
 * użytkownik ma prawo oglądać wideo – bez potrzeby używania JWT
 * przy każdym żądaniu strumienia.
 */
@Service
public class StreamKeyService {

    private final StringRedisTemplate redisTemplate;

    // Czas życia klucza – można potem przenieść do application.yml
    private static final Duration KEY_TTL = Duration.ofHours(1);

    public StreamKeyService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Generuje unikalny klucz autoryzacyjny i zapisuje go w Redisie.
     * @param userId Id użytkownika (opcjonalnie, można użyć do identyfikacji).
     * @return Wygenerowany klucz.
     */
    public String generateKey(Long userId) {
        String key = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set("stream-key:" + key, String.valueOf(userId), KEY_TTL);
        return key;
    }

    /**
     * Sprawdza, czy dany klucz istnieje w Redisie (czy jest jeszcze ważny).
     * @param key Klucz do weryfikacji.
     * @return true – jeśli istnieje, false – jeśli wygasł lub nie istnieje.
     */
    public boolean isKeyValid(String key) {
        return redisTemplate.hasKey("stream-key:" + key);
    }

    /**
     * (Opcjonalnie) usuwa klucz przed jego naturalnym wygaśnięciem.
     */
    public void revokeKey(String key) {
        redisTemplate.delete("stream-key:" + key);
    }
}
