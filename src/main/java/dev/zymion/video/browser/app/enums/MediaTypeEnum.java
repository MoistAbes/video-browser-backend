package dev.zymion.video.browser.app.enums;

public enum MediaTypeEnum {

    MOVIE,
    EPISODE,
    UNKNOWN;

    @Override
    public String toString() {
        return switch (this) {
            case MOVIE -> "Movie";
            case EPISODE -> "Episode";
            case UNKNOWN -> "Unknown";
        };
    }

}
