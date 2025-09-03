package dev.zymion.video.browser.app.config;

import dev.zymion.video.browser.app.services.security.CustomUserDetails;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Aspect odpowiedzialny za logowanie wywołań metod w kontrolerach.
 *
 * Ten aspekt wykorzystuje mechanizm AOP (Aspect-Oriented Programming),
 * aby przechwytywać wywołania metod w pakiecie `controllers` przed ich wykonaniem
 * i logować informacje o użytkowniku (jeśli jest zalogowany) oraz nazwę metody.
 *
 * Dzięki temu można łatwo śledzić, które endpointy są wywoływane i przez kogo,
 * bez konieczności dodawania logów ręcznie w każdym kontrolerze.
 *
 * Można go rozszerzyć o dodatkowe funkcje, np. mierzenie czasu wykonania,
 * logowanie statusu odpowiedzi HTTP, itp.
 *
 * Użycie adnotacji @Aspect pozwala oddzielić logikę techniczną (np. logowanie)
 * od logiki biznesowej aplikacji.
 */
@Aspect
@Component
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    /*
     Dodatkowo mierzy czas wykonania metody (response time),
     co pozwala na analizę wydajności poszczególnych endpointów.
     */
    @Around("execution(* dev.zymion.video.browser.app.controllers..*(..))")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userInfo = "[anonymous]";
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails userDetails) {
            userInfo = String.format("[id: %d] | [username: %s]", userDetails.getId(), userDetails.getUsername());
        }

        Object result = joinPoint.proceed(); // wykonanie metody

        long duration = System.currentTimeMillis() - start;
        log.info("{} | {} | responseTime: {} ms", joinPoint.getSignature(), userInfo, duration);

        return result;
    }

}
