package dev.zymion.video.browser.app.enums;

public enum GenreEnum {

    HORROR,
    COMEDY,
    ACTION,
    ADVENTURE,
    MYSTERY,
    DOCUMENTARY,
    DRAMA,
    FANTASY,
    SCI_FI,
    ROMANCE,
    THRILLER,
    ANIME,
    CARTOON,
    MUSIC,
    HISTORY,
    WESTERN,
    FAMILY,
    WAR,
    CRIME,
    SOAP,
    REALITY,
    NEWS,
    TALK,
    TV_MOVIE,
    UNKNOWN;

    public static GenreEnum fromTmdbName(String tmdbGenreName) {
        return switch (tmdbGenreName.toLowerCase()) {
            case "horror" -> HORROR;
            case "comedy" -> COMEDY;
            case "action", "action & adventure" -> ACTION;
            case "adventure" -> ADVENTURE;
            case "mystery" -> MYSTERY;
            case "documentary" -> DOCUMENTARY;
            case "drama" -> DRAMA;
            case "fantasy", "sci-fi & fantasy" -> FANTASY;
            case "science fiction", "sci-fi" -> SCI_FI;
            case "romance" -> ROMANCE;
            case "thriller" -> THRILLER;
            case "animation", "anime", "cartoon", "kids" -> ANIME;
            case "music" -> MUSIC;
            case "history" -> HISTORY;
            case "western" -> WESTERN;
            case "family" -> FAMILY;
            case "war", "war & politics" -> WAR;
            case "crime" -> CRIME;
            case "soap" -> SOAP;
            case "reality" -> REALITY;
            case "news" -> NEWS;
            case "talk" -> TALK;
            case "tv movie" -> TV_MOVIE;
            default -> UNKNOWN;
        };
    }

    @Override
    public String toString() {
        return switch (this) {
            case HORROR -> "Horror";
            case COMEDY -> "Comedy";
            case ACTION -> "Action";
            case ADVENTURE -> "Adventure";
            case MYSTERY -> "Mystery";
            case DOCUMENTARY -> "Documentary";
            case DRAMA -> "Drama";
            case FANTASY -> "Fantasy";
            case SCI_FI -> "Sci-Fi";
            case ROMANCE -> "Romance";
            case THRILLER -> "Thriller";
            case ANIME -> "Anime";
            case CARTOON -> "Cartoon";
            case MUSIC -> "Music";
            case HISTORY -> "History";
            case WESTERN -> "Western";
            case FAMILY -> "Family";
            case WAR -> "War";
            case CRIME -> "Crime";
            case SOAP -> "Soap";
            case REALITY -> "Reality";
            case NEWS -> "News";
            case TALK -> "Talk";
            case TV_MOVIE -> "TV Movie";
            default -> "Unknown";
        };
    }
}


