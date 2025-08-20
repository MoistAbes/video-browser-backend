package dev.zymion.video.browser.app.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;

@Setter
@Component
@ConfigurationProperties(prefix = "app.paths")
public class AppPathProperties {


    private String videoFolder;

    @Getter
    private String subtitleFolder;

    // Gettery i settery
    public Path getVideoFolder() {
        return Paths.get(videoFolder);
    }

}
