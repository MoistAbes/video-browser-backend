package dev.zymion.video.browser.app.config.websocket;

import dev.zymion.video.browser.app.services.UserInfoService;
import dev.zymion.video.browser.app.services.security.CustomUserDetails;
import dev.zymion.video.browser.app.services.security.SecurityUtilService;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;

@Component
@Log4j2
public class WebSocketEventListener {

    private final SecurityUtilService securityUtilService;
    private final UserInfoService userInfoService;

    public WebSocketEventListener(SecurityUtilService securityUtilService, UserInfoService userInfoService) {
        this.securityUtilService = securityUtilService;
        this.userInfoService = userInfoService;
    }

    @EventListener
    public void handleConnect(SessionConnectEvent event) {
        Principal principal = event.getUser(); // <-- tu jest użytkownik

        if (principal instanceof UsernamePasswordAuthenticationToken auth) {
            CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();
            Long userId = user.getId();

            log.info("user id: {} Connected to websocket: {}", userId, event);
            userInfoService.updateUserOnlineStatus(userId, true);
        }
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        Principal user = event.getUser();

        if (user instanceof UsernamePasswordAuthenticationToken auth) {
            CustomUserDetails details = (CustomUserDetails) auth.getPrincipal();
            Long userId = details.getId();

            log.info("user id: {} Disconnected from websocket: {}", userId, event);
            userInfoService.updateUserOnlineStatus(userId, false);
        }
        else {
            log.info("Unknown or unauthenticated user disconnected: {}", event);
        }
    }


}

