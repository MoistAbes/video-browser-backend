package dev.zymion.video.browser.app.services.helper;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;

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

    // Required setters for Spring to inject values
    public void setVideoFolder(String videoFolder) {
        this.videoFolder = videoFolder;
    }

    public void setSubtitleFolder(String subtitleFolder) {
        this.subtitleFolder = subtitleFolder;
    }

}
