package dev.zymion.video.browser.app.enums;

public enum CategoryEnum {

    MOVIE,
    SHOW,
    SERIES,
    ANIME,
    CARTOON,
    UNKNOWN;

    @Override
    public String toString() {
        return switch (this) {
            case MOVIE -> "Movie";
            case SHOW -> "Show";
            case SERIES -> "Series";
            case ANIME -> "Anime";
            case CARTOON -> "Cartoon";
            case UNKNOWN -> "Unknown";
        };
    }
}
