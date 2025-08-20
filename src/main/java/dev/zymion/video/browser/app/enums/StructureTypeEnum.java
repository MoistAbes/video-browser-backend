package dev.zymion.video.browser.app.enums;

public enum StructureTypeEnum {

    SINGLE_MOVIE,       // jeden film
    MOVIE_COLLECTION,   // seria filmów
    SEASONAL_SERIES,    // serial z sezonami
    HYBRID,             // sezony + filmy
    UNKNOWN;


    @Override
    public String toString() {
        return switch (this) {
            case SINGLE_MOVIE -> "Single movie";
            case MOVIE_COLLECTION -> "Movie collection";
            case SEASONAL_SERIES -> "Seasonal series";
            case HYBRID -> "Hybrid";
            default -> "Unknown";
        };
    }

}
