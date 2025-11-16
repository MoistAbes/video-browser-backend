package dev.zymion.video.browser.app.config.adnotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
    Customowa adnotacja mozemy dac ja nad metode ktorej nie chcemy zeby byla logowana
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SkipLogging {
}
