package dev.zymion.video.browser.app.config.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {


    /*
    Plan jest taki że :
    status czy user online czy nie to po prostu jesli sie połączy z websocketem to jest online jak sie rozłączy to jest offline
    jesli chodzi o status co ogląda to:
    jak wejdzie na szczegóły filmu to wysyłamy pierwsza informacje ze ogląda ogólnie np strager things
    potem np po odpaleniu juz konkretnego filmu odcinka wysyłamy heartbeat co iles tam sekund przesylajac juz dokladnie co oglada
    jesli heartbeat nie przeslle info w 30 sek uznajemy ze uzytkownik przestal ogladac
     */

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic"); // kanały do subskrypcji
        config.setApplicationDestinationPrefixes("/app"); // prefix do wysyłania wiadomości
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws") // endpoint WebSocketa
                .setAllowedOrigins("*") // dla testów lokalnych
                .withSockJS(); // fallback dla przeglądarek bez ws
    }




}
