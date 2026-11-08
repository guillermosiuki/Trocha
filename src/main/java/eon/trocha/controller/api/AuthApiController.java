package eon.trocha.controller.api;

import eon.trocha.dto.LoginRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import eon.trocha.dto.RegistroRequest;
import eon.trocha.model.Usuario;
import eon.trocha.service.AuthService;

import org.springframework.dao.DataIntegrityViolationException;

@RestController
@RequestMapping("/api/auth")
public class AuthApiController {

    @Autowired
    private AuthService authService;

    @PostMapping("/registro")
    public ResponseEntity<?> registrar(@RequestBody RegistroRequest request) {
        try {
            Usuario usuario = authService.registrar(request);
            return ResponseEntity.ok(java.util.Map.of("id", usuario.getId(), "username", usuario.getUsername()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(409).body("Ese usuario o correo ya existe");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            return ResponseEntity.ok(java.util.Map.of("token", authService.login(request)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).body("Usuario o contraseña incorrectos");
        }
    }
}