package eon.trocha.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import eon.trocha.model.Lugar;

public interface LugarRepository extends JpaRepository<Lugar, Long> {
    List<Lugar> findByCorregimiento(String corregimiento);
    List<Lugar> findByCategoria(String categoria);
}