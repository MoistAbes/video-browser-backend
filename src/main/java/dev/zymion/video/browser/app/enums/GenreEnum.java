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
    UNKNOWN;


    public static GenreEnum fromTmdbName(String tmdbGenreName) {
        return switch (tmdbGenreName.toLowerCase()) {
            case "horror" -> HORROR;
            case "comedy" -> COMEDY;
            case "action" -> ACTION;
            case "adventure" -> ADVENTURE;
            case "mystery" -> MYSTERY;
            case "documentary" -> DOCUMENTARY;
            case "drama" -> DRAMA;
            case "fantasy" -> FANTASY;
            case "science fiction", "sci-fi" -> SCI_FI;
            case "romance" -> ROMANCE;
            case "thriller" -> THRILLER;
            case "animation", "anime", "cartoon" -> ANIME; // lub rozdzielić
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
            default -> "Unknown";
        };
    }
}

