package dev.zymion.video.browser.app.models.projections;

import java.util.List;

public interface ShowRootPathProjection {
    Long getId();
    String getName();      // z ShowEntity.name
    String getRootPath();  // z MediaItemEntity.rootPath
    List<GenreProjection> getGenres();
}
