package dev.zymion.video.browser.app.config.websocket;


import dev.zymion.video.browser.app.services.security.SecurityUtilService;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@Log4j2
public class WebSocketEventListener {

    private final SecurityUtilService securityUtilService;

    public WebSocketEventListener(SecurityUtilService securityUtilService) {
        this.securityUtilService = securityUtilService;
    }


    @EventListener
    public void handleConnect(SessionConnectEvent event) {
        // oznacz użytkownika jako online
        Long userId = securityUtilService.getCurrentUserId();
        log.info("user id: {} Connected to websocket: {} | " ,userId ,event);

    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        // oznacz użytkownika jako offline
        Long userId = securityUtilService.getCurrentUserId();
        log.info("user id: {} Disconnected to websocket: {}",userId, event);

    }
}

