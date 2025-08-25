package dev.zymion.video.browser.app.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ShowNotFoundException extends RuntimeException {
    public ShowNotFoundException() {
        super("User not found");
    }

    public ShowNotFoundException(String message) {
        super(message);
    }

}
