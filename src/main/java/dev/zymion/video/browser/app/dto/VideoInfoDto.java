package dev.zymion.video.browser.app.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class VideoInfoDto {

    private Long id;
    private String title;
    private String iconFileName;
    private String rootPath;
    private String type;
    private String category;

}
