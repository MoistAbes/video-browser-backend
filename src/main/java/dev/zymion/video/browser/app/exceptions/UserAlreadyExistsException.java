package dev.zymion.video.browser.app.exceptions;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String username) {
        super("Istnieje już użytkownik o nazwie: " + username);
    }
}

