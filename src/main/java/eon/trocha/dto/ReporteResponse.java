package eon.trocha.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReporteResponse {
    private Long id;
    private Long lugarId;
    private String autor;
    private LocalDateTime fechaHora;
    private Boolean tieneRampa;
    private Boolean tieneBanoAccesible;
    private String anchoPasillos;
    private Boolean personalCapacitado;
    private String comentario;
    private Integer puntuacion;
    private String estado;
    private long confirmaciones;
    private long desmentidos;
}