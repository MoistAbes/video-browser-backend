package dev.zymion.video.browser.app.services.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class SecurityUtilService {

    /**
     * Metoda do wyciagania danych z autoryzacji
     * w tym wypadku id aktualnego usera
     * @return
     */
    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            return ((CustomUserDetails) authentication.getPrincipal()).getId();
        }
        throw new IllegalStateException("Brak zalogowanego użytkownika lub niepoprawny typ principal.");
    }


}
