package eon.trocha.dto;

import lombok.Data;

@Data
public class ReporteRequest {
    private Long lugarId;
    private Boolean tieneRampa;
    private Boolean tieneBanoAccesible;
    private String anchoPasillos;
    private Boolean personalCapacitado;
    private String comentario;
    private Integer puntuacion;
}