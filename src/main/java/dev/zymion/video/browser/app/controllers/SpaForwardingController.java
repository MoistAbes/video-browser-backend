package dev.zymion.video.browser.app.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SpaForwardingController {
    @RequestMapping(value = { "/", "/{[path:[^\\.]*}" })
    public String forwardToIndex() {
        return "forward:/index.html";
    }
}
