package eon.trocha.service;

import eon.trocha.model.Calificacion;
import eon.trocha.model.ReporteAccesibilidad;
import eon.trocha.model.Usuario;
import eon.trocha.repository.CalificacionRepository;
import eon.trocha.repository.ReporteAccesibilidadRepository;
import eon.trocha.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CalificacionService {

    private static final double UMBRAL = 2.0;

    @Autowired private CalificacionRepository calificacionRepository;
    @Autowired private ReporteAccesibilidadRepository reporteRepository;
    @Autowired private UsuarioRepository usuarioRepository;

    @Transactional
    public Calificacion calificar(String username, Long reporteId, String tipo) {
        if (!"confirma".equals(tipo) && !"desmiente".equals(tipo)) {
            throw new IllegalArgumentException("Tipo inválido");
        }

        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no válido"));
        ReporteAccesibilidad reporte = reporteRepository.findById(reporteId)
                .orElseThrow(() -> new IllegalArgumentException("Reporte no encontrado"));

        if (reporte.getUsuario().getId().equals(usuario.getId())) {
            throw new IllegalStateException("No puedes calificar tu propio reporte");
        }
        if (calificacionRepository.existsByReporteIdAndUsuarioId(reporteId, usuario.getId())) {
            throw new IllegalStateException("Ya calificaste este reporte");
        }

        Calificacion calificacion = new Calificacion();
        calificacion.setReporte(reporte);
        calificacion.setUsuario(usuario);
        calificacion.setTipo(tipo);
        calificacionRepository.save(calificacion);

        reevaluar(reporte);
        return calificacion;
    }

    // Peso del voto: 1.0 (reputación 0) hasta 1.5 (reputación 10 o más)
    private double peso(Usuario u) {
        int rep = u.getReputacion() == null ? 0 : u.getReputacion();
        return 1.0 + Math.min(Math.max(rep, 0), 10) / 20.0;
    }

    private void reevaluar(ReporteAccesibilidad reporte) {
        if (!"pendiente".equals(reporte.getEstado())) return; // lo ya decidido solo lo cambia un admin

        double confirman = 0, desmienten = 0;
        for (Calificacion c : calificacionRepository.findByReporteId(reporte.getId())) {
            double w = peso(c.getUsuario());
            if ("confirma".equals(c.getTipo())) confirman += w; else desmienten += w;
        }

        Usuario autor = reporte.getUsuario();
        int repActual = autor.getReputacion() == null ? 0 : autor.getReputacion();

        if (confirman >= UMBRAL && confirman > desmienten) {
            reporte.setEstado("aprobado");
            autor.setReputacion(repActual + 1);
        } else if (desmienten >= UMBRAL && desmienten > confirman) {
            reporte.setEstado("rechazado");
            autor.setReputacion(Math.max(0, repActual - 1));
        } else {
            return;
        }

        usuarioRepository.save(autor);
        reporteRepository.save(reporte);
    }
}