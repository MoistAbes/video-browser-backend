package dev.zymion.video.browser.app.services;

import dev.zymion.video.browser.app.enums.StructureTypeEnum;
import dev.zymion.video.browser.app.repositories.show.ShowStructureRepository;
import org.springframework.stereotype.Service;

@Service
public class ShowStructureService {

    private final ShowStructureRepository showStructureRepository;


    public ShowStructureService(ShowStructureRepository showStructureRepository) {
        this.showStructureRepository = showStructureRepository;
    }


    public Long findIdByName(StructureTypeEnum name) {
        return this.showStructureRepository.findIdByName(name);
    }

}
