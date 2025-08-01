package dev.zymion.video.browser.app.models;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class VideoInfo {

    private Long id;
    private String title;
    private String filePath;
    private String type;
    private int length;
    private String iconFilePath;

}
