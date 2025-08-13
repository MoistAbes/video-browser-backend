package dev.zymion.video.browser.app.models.projections;

public interface ShowRootPathProjection {
    Long getId();
    String getName();      // z ShowEntity.name
    String getRootPath();  // z MediaItemEntity.rootPath
}
