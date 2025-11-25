package dev.zymion.video.browser.app.services.security;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class LoginAttemptService {

    private final int MAX_ATTEMPTS = 5;
    private final long LOCK_TIME_DURATION = 15; // Czas blokady w minutach
    private final RedisTemplate<String, String> redisTemplate;

    public LoginAttemptService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // Metoda pomocnicza do tworzenia klucza w Redis
    private String getKey(String username) {
        return "login:attempts:" + username;
    }

    public void loginSucceeded(String username) {
        String key = getKey(username);
        redisTemplate.delete(key); // Usuń klucz po udanym logowaniu
    }

    public void loginFailed(String username) {
        String key = getKey(username);
        // Zwiększ licznik o 1. Jeśli klucz nie istnieje, zostanie utworzony z wartością 1.
        Long attempts = redisTemplate.opsForValue().increment(key);

        // Ustaw czas wygaśnięcia klucza przy pierwszej nieudanej próbie
        if (attempts != null && attempts == 1) {
            redisTemplate.expire(key, LOCK_TIME_DURATION, TimeUnit.MINUTES);
        }
    }

    public boolean isBlocked(String username) {
        String key = getKey(username);
        String attemptsStr = redisTemplate.opsForValue().get(key);

        if (attemptsStr == null) {
            return false; // Klucz nie istnieje, więc nie jest zablokowany
        }

        int attempts = Integer.parseInt(attemptsStr);
        return attempts >= MAX_ATTEMPTS;
    }
}
