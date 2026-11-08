package eon.trocha.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import eon.trocha.model.ReporteAccesibilidad;

public interface ReporteAccesibilidadRepository extends JpaRepository<ReporteAccesibilidad, Long> {
    List<ReporteAccesibilidad> findByLugarId(Long lugarId);
    List<ReporteAccesibilidad> findByEstado(String estado);
    boolean existsByLugarIdAndUsuarioId(Long lugarId, Long usuarioId);
}