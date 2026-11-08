package eon.trocha.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reportes_accesibilidad")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReporteAccesibilidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "lugar_id", nullable = false)
    private Lugar lugar;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora = LocalDateTime.now();

    @Column(name = "tiene_rampa")
    private Boolean tieneRampa;

    @Column(name = "tiene_bano_accesible")
    private Boolean tieneBanoAccesible;

    @Column(name = "ancho_pasillos")
    private String anchoPasillos; // "adecuado", "estrecho", "no_aplica"

    @Column(name = "personal_capacitado")
    private Boolean personalCapacitado;

    @Column(columnDefinition = "TEXT")
    private String comentario;

    @Column(name = "foto_url")
    private String fotoUrl;

    @Column(nullable = false)
    private String estado = "pendiente"; // pendiente, aprobado, rechazado

    private Integer puntuacion; // 1 a 10
}