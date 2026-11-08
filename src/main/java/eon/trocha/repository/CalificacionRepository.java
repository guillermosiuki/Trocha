package eon.trocha.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import eon.trocha.model.Calificacion;

public interface CalificacionRepository extends JpaRepository<Calificacion, Long> {
    List<Calificacion> findByReporteId(Long reporteId);
    boolean existsByReporteIdAndUsuarioId(Long reporteId, Long usuarioId);
    long countByReporteIdAndTipo(Long reporteId, String tipo);
}