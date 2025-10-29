package dev.zymion.video.browser.app.enums;

public enum GenreEnum {

    ACTION,              // Action, Action & Adventure
    ADVENTURE,           // Adventure
    ANIMATION,           // Animation
    COMEDY,              // Comedy
    CRIME,               // Crime
    DOCUMENTARY,         // Documentary
    DRAMA,               // Drama
    FAMILY,              // Family
    FANTASY,             // Fantasy, Sci-Fi & Fantasy
    HISTORY,             // History
    HORROR,              // Horror
    KIDS,                // Kids
    MUSIC,               // Music
    MYSTERY,             // Mystery
    NEWS,                // News
    REALITY,             // Reality
    ROMANCE,             // Romance
    SCI_FI,              // Science Fiction
    SOAP,                // Soap
    TALK,                // Talk
    THRILLER,            // Thriller
    TV_MOVIE,            // TV Movie
    WAR,                 // War, War & Politics
    WESTERN,             // Western
    UNKNOWN;             // fallback

    public static GenreEnum fromTmdbName(String tmdbGenreName) {
        return switch (tmdbGenreName.toLowerCase()) {
            case "action", "action & adventure" -> ACTION;
            case "adventure" -> ADVENTURE;
            case "animation" -> ANIMATION;
            case "comedy" -> COMEDY;
            case "crime" -> CRIME;
            case "documentary" -> DOCUMENTARY;
            case "drama" -> DRAMA;
            case "family" -> FAMILY;
            case "fantasy", "sci-fi & fantasy" -> FANTASY;
            case "history" -> HISTORY;
            case "horror" -> HORROR;
            case "kids" -> KIDS;
            case "music" -> MUSIC;
            case "mystery" -> MYSTERY;
            case "news" -> NEWS;
            case "reality" -> REALITY;
            case "romance" -> ROMANCE;
            case "science fiction", "sci-fi" -> SCI_FI;
            case "soap" -> SOAP;
            case "talk" -> TALK;
            case "thriller" -> THRILLER;
            case "tv movie" -> TV_MOVIE;
            case "war", "war & politics" -> WAR;
            case "western" -> WESTERN;
            default -> UNKNOWN;
        };
    }



}


