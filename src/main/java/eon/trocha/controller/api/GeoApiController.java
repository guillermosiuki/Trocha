package eon.trocha.controller.api;

import eon.trocha.service.GeoPanamaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/geo")
public class GeoApiController {

    @Autowired
    private GeoPanamaService geo;

    @GetMapping("/ubicar")
    public ResponseEntity<?> ubicar(@RequestParam double lat, @RequestParam double lon) {
        if (!geo.disponible()) {
            return ResponseEntity.status(503).body("Falta el archivo de límites de Panamá");
        }
        return geo.ubicar(lat, lon)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(404).body("Fuera de Panamá"));
    }
}