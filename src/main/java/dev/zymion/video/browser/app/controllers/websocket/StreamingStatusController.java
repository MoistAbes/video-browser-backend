package dev.zymion.video.browser.app.controllers.websocket;

import dev.zymion.video.browser.app.models.entities.user.UserStatusEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class StreamingStatusController {


    @MessageMapping("/status") // klient wysyła na /app/status
    @SendTo("/topic/status")   // serwer broadcastuje na /topic/status
    public UserStatusEntity broadcastStatus(UserStatusEntity status) {
        return status;
    }


}
