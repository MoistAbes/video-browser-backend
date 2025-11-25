package dev.zymion.video.browser.app.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class VideoService {


    //ToDO
    //skrypt to konwersji napisow srt na vtt
//    ffmpeg -sub_charenc windows-1250 -i napisy.srt napisy.vtt
//    ffmpeg -sub_charenc UTF-8 -i napisy.srt napisy.vtt --->>> to mi dobrze polskie napisy przerobilo


    //ToDO skrypt do zmiany formatu audio na obslugiwany
//    ffmpeg -i input.mkv -c:v copy -c:a aac -b:a 192k output.mkv

    //ToDO polecenie do robienie zdjec co 10 min przez pierwsze 30 min
//    ffmpeg -i Everest.mp4 -vf "fps=1/300" -t 1800 -q:v 2 output_%03d.jpg
    //ffmpeg -i "Demon slayer - Infinity train.mp4" -t 00:30:00 -vf "fps=1/600" -qscale:v 2 thumbnails/thumb_%03d.jpg




}
