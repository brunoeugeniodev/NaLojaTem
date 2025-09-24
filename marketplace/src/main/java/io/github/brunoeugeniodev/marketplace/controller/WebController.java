package io.github.brunoeugeniodev.marketplace.controller;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {
    @GetMapping("/")
    public String home() {
        return "index"; // Retorna o nome do arquivo HTML na pasta 'templates'
    }
}
