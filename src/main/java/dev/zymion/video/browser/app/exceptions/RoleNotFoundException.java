package dev.zymion.video.browser.app.exceptions;

import dev.zymion.video.browser.app.enums.RoleEnum;

public class RoleNotFoundException extends RuntimeException {
    public RoleNotFoundException(RoleEnum name) {
        super("Nie istnieje rola o nazwie: " + name);
    }
}
