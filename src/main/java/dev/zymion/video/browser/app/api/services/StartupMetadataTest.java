package dev.zymion.video.browser.app.api.services;

import dev.zymion.video.browser.app.api.models.MovieMetadataDto;
import dev.zymion.video.browser.app.services.util.StringUtilService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.Optional;

//ToDO dla testow potem do usuniecia
@Component
public class StartupMetadataTest implements CommandLineRunner {

    private final MovieMetadataApiService metadataService;
    private final StringUtilService stringUtilService;

    public StartupMetadataTest(MovieMetadataApiService metadataService, StringUtilService stringUtilService) {
        this.metadataService = metadataService;
        this.stringUtilService = stringUtilService;
    }

    @Override
    public void run(String... args) {

//        String rawTitle = "Incepcja (2010)";
        String rawTitle = "oculus (2013)";
//        String rawTitle = "Demon slayer";
        String cleanTitle = stringUtilService.extractCleanTitle(rawTitle);
        Optional<Integer> yearOpt = stringUtilService.extractYearFromTitle(rawTitle);

        System.out.println("🔍 Testuję pobieranie metadanych dla: " + rawTitle);

        Optional<MovieMetadataDto> metadata = metadataService.fetchMetadata(cleanTitle, yearOpt, true);

        if (metadata.isPresent()) {

            System.out.println("✅ Znaleziono:");
            System.out.println("Tytuł: " + metadata.get().getTitle());
            System.out.println("Opis: " + metadata.get().getOverview());
            System.out.println("Gatunki : " + metadata.get().getGenreNames());
        } else {
            System.out.println("❌ Nie znaleziono metadanych dla: " + rawTitle);
        }
    }
}

