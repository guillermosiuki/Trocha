package eon.trocha.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import eon.trocha.dto.LoginRequest;
import eon.trocha.dto.RegistroRequest;
import eon.trocha.model.Usuario;
import eon.trocha.repository.UsuarioRepository;

import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

@Service
public class AuthService {

    private static final Pattern USUARIO = Pattern.compile("^[A-Za-z0-9_.]{3,30}$");
    private static final Pattern CORREO = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    @Autowired
    private JwtUtil jwtUtil;

    public String login(LoginRequest request) {
        String nombreUsuario = request.getUsername();
        String clave = request.getPassword();
        if (nombreUsuario == null || clave == null) {
            throw new IllegalArgumentException("Usuario o contraseña incorrectos");
        }

        Usuario usuario = usuarioRepository.findByUsername(nombreUsuario.trim())
                .orElseThrow(() -> new IllegalArgumentException("Usuario o contraseña incorrectos"));

        if (!passwordEncoder.matches(clave, usuario.getPasswordHash())) {
            throw new IllegalArgumentException("Usuario o contraseña incorrectos");
        }

        return jwtUtil.generarToken(usuario.getUsername());
    }

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Usuario registrar(RegistroRequest request) {
        String nombre = request.getNombre() == null ? "" : request.getNombre().trim();
        String username = request.getUsername() == null ? "" : request.getUsername().trim();
        String email = request.getEmail() == null ? null : request.getEmail().trim();
        String clave = request.getPassword();

        if (nombre.isEmpty() || nombre.length() > 80) {
            throw new IllegalArgumentException("Escribe tu nombre (máximo 80 caracteres)");
        }
        if (!USUARIO.matcher(username).matches()) {
            throw new IllegalArgumentException(
                    "El usuario debe tener de 3 a 30 caracteres: letras, números, punto o guion bajo");
        }
        // BCrypt solo usa los primeros 72 bytes de la contraseña
        if (clave == null || clave.length() < 8 || clave.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres (máximo 72)");
        }
        if (email != null && email.isEmpty()) {
            email = null;
        }
        if (email != null && (email.length() > 120 || !CORREO.matcher(email).matches())) {
            throw new IllegalArgumentException("El correo no es válido");
        }
        if (usuarioRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Ese usuario ya existe");
        }
        if (email != null && usuarioRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Ese correo ya está registrado");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setUsername(username);
        usuario.setEmail(email);
        usuario.setPasswordHash(passwordEncoder.encode(clave));
        usuario.setRol("usuario");
        usuario.setReputacion(0);

        return usuarioRepository.save(usuario);
    }
}