package dev.zymion.video.browser.app.projections;

public interface ShowRootPathProjection {
    Long getId();
    String getName();      // z ShowEntity.name
    String getRootPath();  // z MediaItemEntity.rootPath
}
