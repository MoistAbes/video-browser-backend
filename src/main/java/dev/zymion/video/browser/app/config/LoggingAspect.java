package dev.zymion.video.browser.app.config;

import dev.zymion.video.browser.app.services.security.CustomUserDetails;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    @Before("execution(* dev.zymion.video.browser.app.controllers..*(..))")
    public void logBefore(JoinPoint joinPoint) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userInfo = "[anonymous]";
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails userDetails) {
            userInfo = String.format("[id: %d] | [username: %s]", userDetails.getId(), userDetails.getUsername());
        }

        log.info("{} | {}", joinPoint.getSignature(), userInfo);
    }
}
