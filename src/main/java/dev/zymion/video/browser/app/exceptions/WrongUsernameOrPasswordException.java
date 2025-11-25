package dev.zymion.video.browser.app.exceptions;

public class WrongUsernameOrPasswordException extends Exception{
    public WrongUsernameOrPasswordException() {
        super("Wrong username or password");
    }

    public WrongUsernameOrPasswordException(String message) {
        super(message);
    }

}
