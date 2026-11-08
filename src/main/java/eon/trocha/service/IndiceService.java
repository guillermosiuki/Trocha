package eon.trocha.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eon.trocha.model.Lugar;
import eon.trocha.model.ReporteAccesibilidad;
import eon.trocha.repository.ReporteAccesibilidadRepository;

@Service
public class IndiceService {

    public record IndiceZona(String corregimiento, String distrito, String provincia,
                             int lugares, int resenas, double indice) {}

    private static class Acumulado {
        double suma = 0;
        double pesos = 0;
        int resenas = 0;
        Set<Long> lugares = new HashSet<>();
    }

    @Autowired
    private ReporteAccesibilidadRepository reporteRepository;

    public List<IndiceZona> porCorregimiento() {
        Map<String, Acumulado> zonas = new HashMap<>();
        Map<String, String[]> nombres = new HashMap<>();

        for (ReporteAccesibilidad r : reporteRepository.findAll()) {
            if ("rechazado".equals(r.getEstado())) continue;

            Lugar l = r.getLugar();
            if (l.getCorregimiento() == null) continue;

            Double puntaje = accesibilidad(r);
            if (puntaje == null) continue;

            String clave = l.getProvincia() + "|" + l.getDistrito() + "|" + l.getCorregimiento();
            nombres.putIfAbsent(clave,
                    new String[] { l.getCorregimiento(), l.getDistrito(), l.getProvincia() });

            Acumulado a = zonas.computeIfAbsent(clave, k -> new Acumulado());
            double peso = "aprobado".equals(r.getEstado()) ? 1.0 : 0.5;
            a.suma += puntaje * peso;
            a.pesos += peso;
            a.resenas++;
            a.lugares.add(l.getId());
        }

        List<IndiceZona> resultado = new ArrayList<>();
        for (Map.Entry<String, Acumulado> e : zonas.entrySet()) {
            String[] n = nombres.get(e.getKey());
            Acumulado a = e.getValue();
            double indice = Math.round(a.suma / a.pesos * 1000.0) / 10.0;
            resultado.add(new IndiceZona(n[0], n[1], n[2], a.lugares.size(), a.resenas, indice));
        }
        resultado.sort((x, y) -> Double.compare(y.indice(), x.indice()));
        return resultado;
    }

    // Fracción (0 a 1) de criterios cumplidos; null si la reseña no marca ninguno
    private Double accesibilidad(ReporteAccesibilidad r) {
        int cumplidos = 0;
        int total = 0;

        if (r.getTieneRampa() != null) {
            total++;
            if (r.getTieneRampa()) cumplidos++;
        }
        if (r.getTieneBanoAccesible() != null) {
            total++;
            if (r.getTieneBanoAccesible()) cumplidos++;
        }
        if (r.getPersonalCapacitado() != null) {
            total++;
            if (r.getPersonalCapacitado()) cumplidos++;
        }
        if (r.getAnchoPasillos() != null && !"no_aplica".equals(r.getAnchoPasillos())) {
            total++;
            if ("adecuado".equals(r.getAnchoPasillos())) cumplidos++;
        }
        return total == 0 ? null : (double) cumplidos / total;
    }
}