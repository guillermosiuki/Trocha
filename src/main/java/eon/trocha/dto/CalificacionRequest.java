package eon.trocha.dto;

import lombok.Data;

@Data
public class CalificacionRequest {
    private Long reporteId;
    private String tipo; // "confirma" o "desmiente"
}