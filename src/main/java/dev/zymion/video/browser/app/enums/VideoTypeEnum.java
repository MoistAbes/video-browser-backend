package dev.zymion.video.browser.app.enums;

public enum VideoTypeEnum {

    ANIME,
    MOVIE,
    SHOW,
    UNKNOWN;

    public static VideoTypeEnum fromString(String value) {
        if (value == null) return UNKNOWN;

        return switch (value.trim().toLowerCase()) {
            case "anime" -> ANIME;
            case "movie" -> MOVIE;
            case "show", "shows" -> SHOW; // obsługuje oba warianty
            default -> UNKNOWN;
        };
    }

}
