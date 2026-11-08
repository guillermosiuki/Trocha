package eon.trocha.controller.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import eon.trocha.service.IndiceService;

@RestController
@RequestMapping("/api/indice")
public class IndiceApiController {

    @Autowired
    private IndiceService indiceService;

    @GetMapping("/corregimientos")
    public List<IndiceService.IndiceZona> corregimientos() {
        return indiceService.porCorregimiento();
    }
}