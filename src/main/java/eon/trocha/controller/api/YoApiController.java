package eon.trocha.controller.api;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import eon.trocha.repository.UsuarioRepository;

@RestController
public class YoApiController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping("/api/yo")
    public ResponseEntity<?> yo(Authentication authentication) {
        return usuarioRepository.findByUsername(authentication.getName())
                .<ResponseEntity<?>>map(u -> ResponseEntity.ok(Map.of(
                        "username", u.getUsername(),
                        "rol", u.getRol(),
                        "reputacion", u.getReputacion() == null ? 0 : u.getReputacion())))
                .orElse(ResponseEntity.status(401).build());
    }
}