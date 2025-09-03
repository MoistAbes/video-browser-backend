package dev.zymion.video.browser.app.enums;

public enum MediaTypeEnum {
    MOVIE,
    TV,
    UNKNOWN;

    @Override
    public String toString() {
        return switch (this) {
            case MOVIE -> "Movie";
            case TV -> "TV";
            default -> "Unknown";
        };
    }

    public static MediaTypeEnum fromTmdbType(String tmdbGenreName) {
        return switch (tmdbGenreName.toLowerCase()) {
            case "movie" -> MOVIE;
            case "tv" -> TV;
            default -> UNKNOWN;
        };
    }
}

