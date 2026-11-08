package eon.trocha.controller.api;

import eon.trocha.dto.CalificacionRequest;
import eon.trocha.service.CalificacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/calificaciones")
public class CalificacionApiController {

    @Autowired private CalificacionService calificacionService;

    @PostMapping
    public ResponseEntity<?> crear(Authentication authentication, @RequestBody CalificacionRequest req) {
        try {
            calificacionService.calificar(authentication.getName(), req.getReporteId(), req.getTipo());
            return ResponseEntity.ok(Map.of("mensaje", "Calificación registrada"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(e.getMessage());
        }
    }


}