package eon.trocha.controller.api;

import eon.trocha.model.ReporteAccesibilidad;
import eon.trocha.repository.ReporteAccesibilidadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

import eon.trocha.dto.ReporteRequest;
import eon.trocha.model.Lugar;
import eon.trocha.model.Usuario;
import eon.trocha.repository.LugarRepository;
import eon.trocha.repository.UsuarioRepository;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.Authentication;

import eon.trocha.dto.ReporteResponse;
import eon.trocha.repository.CalificacionRepository;

import java.util.Set;

@RestController
@RequestMapping("/api/reportes")
public class ReporteApiController {

    private static final Set<String> ESTADOS = Set.of("pendiente", "aprobado", "rechazado");
    private static final Set<String> PASILLOS = Set.of("adecuado", "estrecho", "no_aplica");
    
    @Autowired private ReporteAccesibilidadRepository reporteRepository;
    @Autowired private LugarRepository lugarRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private CalificacionRepository calificacionRepository;

    @GetMapping
    public List<ReporteResponse> listar() {
        return reporteRepository.findAll().stream()
                .filter(r -> !"rechazado".equals(r.getEstado()))
                .map(this::aDto).toList();
    }

    @PostMapping
    public ResponseEntity<?> crear(Authentication authentication, @RequestBody ReporteRequest req) {

        Usuario usuario = usuarioRepository.findByUsername(authentication.getName()).orElse(null);
        Lugar lugar = req.getLugarId() == null ? null : lugarRepository.findById(req.getLugarId()).orElse(null);

        if (usuario == null || lugar == null) {
            return ResponseEntity.badRequest().body("Usuario o lugar no válido");
        }
        if (req.getPuntuacion() == null || req.getPuntuacion() < 1 || req.getPuntuacion() > 10) {
            return ResponseEntity.badRequest().body("La puntuación debe ser de 1 a 10");
        }
        if (req.getComentario() != null && req.getComentario().length() > 1000) {
            return ResponseEntity.badRequest().body("El comentario es demasiado largo (máximo 1000 caracteres)");
        }
        if (req.getAnchoPasillos() != null && !PASILLOS.contains(req.getAnchoPasillos())) {
            return ResponseEntity.badRequest().body("Valor de pasillos no válido");
        }
        if (reporteRepository.existsByLugarIdAndUsuarioId(lugar.getId(), usuario.getId())) {
            return ResponseEntity.status(409).body("Ya enviaste una reseña de este lugar");
        }

        ReporteAccesibilidad reporte = new ReporteAccesibilidad();
        reporte.setLugar(lugar);
        reporte.setUsuario(usuario);
        reporte.setPuntuacion(req.getPuntuacion());
        reporte.setTieneRampa(req.getTieneRampa());
        reporte.setTieneBanoAccesible(req.getTieneBanoAccesible());
        reporte.setAnchoPasillos(req.getAnchoPasillos());
        reporte.setPersonalCapacitado(req.getPersonalCapacitado());
        reporte.setComentario(req.getComentario());

        return ResponseEntity.ok(aDto(reporteRepository.save(reporte)));
    }
    
    @GetMapping("/lugar/{lugarId}")
    public List<ReporteResponse> porLugar(@PathVariable Long lugarId) {
        return reporteRepository.findByLugarId(lugarId).stream()
                .filter(r -> !"rechazado".equals(r.getEstado()))
                .map(this::aDto).toList();
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstado(@PathVariable Long id, @RequestParam String estado) {
        if (!ESTADOS.contains(estado)) {
            return ResponseEntity.badRequest().body("Estado inválido");
        }
        return reporteRepository.findById(id)
                .<ResponseEntity<?>>map(r -> {
                    r.setEstado(estado);
                    return ResponseEntity.ok(aDto(reporteRepository.save(r)));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    private ReporteResponse aDto(ReporteAccesibilidad r) {
        ReporteResponse d = new ReporteResponse();
        d.setId(r.getId());
        d.setLugarId(r.getLugar().getId());
        d.setAutor(r.getUsuario().getUsername());
        d.setFechaHora(r.getFechaHora());
        d.setTieneRampa(r.getTieneRampa());
        d.setTieneBanoAccesible(r.getTieneBanoAccesible());
        d.setAnchoPasillos(r.getAnchoPasillos());
        d.setPersonalCapacitado(r.getPersonalCapacitado());
        d.setComentario(r.getComentario());
        d.setEstado(r.getEstado());
        d.setPuntuacion(r.getPuntuacion());
        d.setConfirmaciones(calificacionRepository.countByReporteIdAndTipo(r.getId(), "confirma"));
        d.setDesmentidos(calificacionRepository.countByReporteIdAndTipo(r.getId(), "desmiente"));
        return d;
    }
}