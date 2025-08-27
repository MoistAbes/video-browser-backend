package dev.zymion.video.browser.app.services.util;

import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class StringUtilService {


    public Optional<Integer> extractYearFromTitle(String title) {
        Pattern pattern = Pattern.compile("\\((\\d{4})\\)");
        Matcher matcher = pattern.matcher(title);

        if (matcher.find()) {
            return Optional.of(Integer.parseInt(matcher.group(1)));
        }

        return Optional.empty(); // brak roku w tytule
    }

    public String extractCleanTitle(String title) {
        return title.replaceAll("\\s*\\(\\d{4}\\)", "").trim();
    }





}
