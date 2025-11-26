package dev.zymion.video.browser.app.config.schedulers;

import dev.zymion.video.browser.app.config.security.RateLimitingFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RateLimitingCleanup {

    private final RateLimitingFilter filter;

    public RateLimitingCleanup(RateLimitingFilter filter) {
        this.filter = filter;
    }

    // czyści mapy co godzinę
    @Scheduled(fixedRate = 60 * 60 * 1000)
    public void cleanup() {
        filter.getRequestCounts().clear();
        filter.getTimestamps().clear();
        log.info("Rate limiting maps cleared.");
    }
}

