package eon.trocha.security;

import eon.trocha.repository.UsuarioRepository;
import eon.trocha.service.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String auth = request.getHeader("Authorization");

        if (auth != null && auth.startsWith("Bearer ")) {
            String token = auth.substring(7);
            if (jwtUtil.esTokenValido(token)) {
                String username = jwtUtil.extraerUsername(token);
                usuarioRepository.findByUsername(username).ifPresent(u -> {
                    var authToken = new UsernamePasswordAuthenticationToken(
                            u.getUsername(),
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + u.getRol().toUpperCase()))
                    );
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                });
            }
        }

        chain.doFilter(request, response);
    }
}