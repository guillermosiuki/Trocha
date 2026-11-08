package eon.trocha.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.annotation.PostConstruct;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.locationtech.jts.geom.prep.PreparedGeometryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class GeoPanamaService {

    private static final Logger log = LoggerFactory.getLogger(GeoPanamaService.class);



    public record Ubicacion(String corregimiento, String distrito, String provincia) {}

    private record Zona(Geometry geometria, PreparedGeometry preparada, Envelope sobre, Ubicacion ubicacion) {}

    @Value("${geo.archivo:geo/corregimientos.geojson}")
    private String archivo;
    @Value("${geo.campo.corregimiento:ADM3_ES}")
    private String campoCorregimiento;
    @Value("${geo.campo.distrito:ADM2_ES}")
    private String campoDistrito;
    @Value("${geo.campo.provincia:ADM1_ES}")
    private String campoProvincia;
    // En grados (0.005 equivale a unos 550 m): tolera clics en la costa y en rellenos
    @Value("${geo.tolerancia:0.005}")
    private double tolerancia;

    private final GeometryFactory gf = new GeometryFactory();
    private final List<Zona> zonas = new ArrayList<>();

    @PostConstruct
    void cargar() {
        ClassPathResource recurso = new ClassPathResource(archivo);
        if (!recurso.exists()) {
            log.warn("No se encontró {}. No se podrán crear lugares hasta agregarlo.", archivo);
            return;
        }
        try (Reader lector = new InputStreamReader(recurso.getInputStream(), StandardCharsets.UTF_8)) {
            JsonObject raiz = JsonParser.parseReader(lector).getAsJsonObject();
            JsonArray features = raiz.getAsJsonArray("features");
            boolean primero = true;
            for (JsonElement el : features) {
                try {
                    JsonObject feature = el.getAsJsonObject();
                    JsonObject props = feature.getAsJsonObject("properties");
                    if (primero) {
                        log.info("Propiedades del GeoJSON: {}", props.keySet());
                        primero = false;
                    }
                    String corregimiento = texto(props, campoCorregimiento);
                    Geometry g = geometria(feature.get("geometry"));
                    if (corregimiento == null || g == null) continue;
                    Ubicacion u = new Ubicacion(corregimiento, texto(props, campoDistrito), texto(props, campoProvincia));
                    zonas.add(new Zona(g, PreparedGeometryFactory.prepare(g), g.getEnvelopeInternal(), u));
                } catch (RuntimeException e) {
                    log.warn("Se omitió una zona inválida: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("No se pudo leer {}: {}", archivo, e.getMessage());
        }
        if (zonas.isEmpty()) {
            log.warn("No se cargó ninguna zona. Revisa los nombres de campos (geo.campo.*) contra la línea 'Propiedades del GeoJSON'.");
        } else {
            log.info("Zonas cargadas: {}", zonas.size());
        }
    }

    public boolean disponible() {
        return !zonas.isEmpty();
    }

    public Optional<Ubicacion> ubicar(double lat, double lon) {
        Point p = gf.createPoint(new Coordinate(lon, lat));

        for (Zona z : zonas) {
            if (z.sobre().covers(lon, lat) && z.preparada().covers(p)) {
                return Optional.of(z.ubicacion());
            }
        }

        // Segunda pasada: la zona más cercana dentro de la tolerancia (costas)
        Zona mejor = null;
        double mejorDistancia = tolerancia;
        for (Zona z : zonas) {
            Envelope ampliado = new Envelope(z.sobre());
            ampliado.expandBy(tolerancia);
            if (!ampliado.covers(lon, lat)) continue;
            double d = z.geometria().distance(p);
            if (d <= mejorDistancia) {
                mejor = z;
                mejorDistancia = d;
            }
        }
        return mejor == null ? Optional.empty() : Optional.of(mejor.ubicacion());
    }

    private String texto(JsonObject props, String campo) {
        JsonElement v = props.get(campo);
        return (v == null || v.isJsonNull()) ? null : v.getAsString();
    }

    private Geometry geometria(JsonElement el) {
        if (el == null || el.isJsonNull()) return null;
        JsonObject g = el.getAsJsonObject();
        String tipo = g.get("type").getAsString();
        JsonArray c = g.getAsJsonArray("coordinates");
        if ("Polygon".equals(tipo)) return poligono(c);
        if ("MultiPolygon".equals(tipo)) {
            Polygon[] ps = new Polygon[c.size()];
            for (int i = 0; i < c.size(); i++) ps[i] = poligono(c.get(i).getAsJsonArray());
            return gf.createMultiPolygon(ps);
        }
        return null;
    }

    private Polygon poligono(JsonArray anillos) {
        LinearRing exterior = anillo(anillos.get(0).getAsJsonArray());
        LinearRing[] huecos = new LinearRing[anillos.size() - 1];
        for (int i = 1; i < anillos.size(); i++) huecos[i - 1] = anillo(anillos.get(i).getAsJsonArray());
        return gf.createPolygon(exterior, huecos);
    }

    private LinearRing anillo(JsonArray puntos) {
        Coordinate[] cs = new Coordinate[puntos.size()];
        for (int i = 0; i < puntos.size(); i++) {
            JsonArray p = puntos.get(i).getAsJsonArray();
            cs[i] = new Coordinate(p.get(0).getAsDouble(), p.get(1).getAsDouble());
        }
        return gf.createLinearRing(cs);
    }
}