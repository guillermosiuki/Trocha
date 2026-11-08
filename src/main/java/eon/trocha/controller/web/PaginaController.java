package eon.trocha.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PaginaController {

    @GetMapping("/")
    public String home() { return "home"; }

    @GetMapping("/mapa")
    public String mapa() { return "mapa"; }

    @GetMapping("/login")
    public String login() { return "login"; }

    @GetMapping("/registro")
    public String registro() { return "registro"; }

    @GetMapping("/reportar")
    public String reportar() { return "reportar"; }

    @GetMapping("/lugar")
    public String lugar() { return "lugar"; }

    @GetMapping("/admin")
    public String admin() { return "admin"; }

    @GetMapping("/indice")
    public String indice() { return "indice"; }
}
