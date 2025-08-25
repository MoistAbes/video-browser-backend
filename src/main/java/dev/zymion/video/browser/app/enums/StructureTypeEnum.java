package dev.zymion.video.browser.app.enums;

import dev.zymion.video.browser.app.models.entities.show.ShowEntity;

public enum StructureTypeEnum {

    SINGLE_MOVIE,       // jeden film
    MOVIE_COLLECTION,   // seria filmów
    SEASONAL_SERIES,    // serial z sezonami
    HYBRID,             // sezony + filmy
    UNKNOWN;


    public static StructureTypeEnum fromShow(ShowEntity show) {
        boolean hasSeasons = !show.getSeasons().isEmpty();
        int movieCount = show.getMovies().size();

        if (!hasSeasons) {
            return movieCount == 1 ? SINGLE_MOVIE : MOVIE_COLLECTION;
        } else {
            return movieCount == 0 ? SEASONAL_SERIES : HYBRID;
        }
    }


    public static StructureTypeEnum safeValueOf(String value) {
        try {
            return StructureTypeEnum.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }


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
