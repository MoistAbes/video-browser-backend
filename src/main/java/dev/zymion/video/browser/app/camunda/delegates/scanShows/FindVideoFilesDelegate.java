package dev.zymion.video.browser.app.camunda.delegates.scanShows;

import dev.zymion.video.browser.app.services.file.FileService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.List;

@Component("findVideoFilesDelegate")
public class FindVideoFilesDelegate implements JavaDelegate {

    private final FileService fileService;

    public FindVideoFilesDelegate(FileService fileService) {
        this.fileService = fileService;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        // Pobieramy katalog z konfiguracji procesu (jeśli jest przekazywany)
        String videoFolder = (String) execution.getVariable("videoFolder");

        // Jeśli nie ma zmiennej, możesz też ustawić default w kodzie
        if (videoFolder == null) {
            throw new IllegalStateException("Missing process variable: videoFolder");
        }

        // Znajdujemy wszystkie pliki video
        List<Path> files = fileService.findAllVideoFiles(Path.of(videoFolder));

        // Camunda nie umie zapisywać Path → konwertujemy do String
        List<String> filePaths = files.stream()
                .map(Path::toString)
                .toList();

        // zapis
        execution.setVariable("foundFiles", filePaths);
    }
}
