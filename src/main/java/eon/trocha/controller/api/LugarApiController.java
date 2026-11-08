package eon.trocha.controller.api;

import eon.trocha.model.Lugar;
import eon.trocha.repository.LugarRepository;
import eon.trocha.service.GeoPanamaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/api/lugares")
public class LugarApiController {

    private static final Set<String> CATEGORIAS = Set.of(
            "restaurante", "banco", "salud", "transporte",
            "comercio", "educacion", "gasolinera", "otro");

    @Autowired
    private LugarRepository lugarRepository;

    @Autowired
    private GeoPanamaService geo;

    @GetMapping
    public List<Lugar> listar() {
        return lugarRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Lugar lugar) {
        if (lugar.getNombre() == null || lugar.getNombre().isBlank() || lugar.getNombre().length() > 120) {
            return ResponseEntity.badRequest().body("Nombre inválido");
        }
        if (lugar.getCategoria() == null || !CATEGORIAS.contains(lugar.getCategoria())) {
            return ResponseEntity.badRequest().body("Categoría inválida");
        }
        if ("otro".equals(lugar.getCategoria())) {
            String d = lugar.getDetalleCategoria();
            if (d == null || d.isBlank() || d.length() > 60) {
                return ResponseEntity.badRequest().body("Especifica qué tipo de lugar es");
            }
        } else {
            lugar.setDetalleCategoria(null);
        }
        if (lugar.getLatitud() == null || lugar.getLongitud() == null) {
            return ResponseEntity.badRequest().body("Ubicación inválida");
        }
        if (!geo.disponible()) {
            return ResponseEntity.status(503).body("Falta el archivo de límites de Panamá");
        }

        Optional<GeoPanamaService.Ubicacion> u = geo.ubicar(lugar.getLatitud(), lugar.getLongitud());
        if (u.isEmpty()) {
            return ResponseEntity.badRequest().body("La ubicación debe estar dentro de Panamá");
        }
        lugar.setCorregimiento(u.get().corregimiento());
        lugar.setDistrito(u.get().distrito());
        lugar.setProvincia(u.get().provincia());

        lugar.setId(null); // el cliente no decide el id
        return ResponseEntity.ok(lugarRepository.save(lugar));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Lugar> obtener(@PathVariable Long id) {
        return lugarRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/corregimiento/{corregimiento}")
    public List<Lugar> porCorregimiento(@PathVariable String corregimiento) {
        return lugarRepository.findByCorregimiento(corregimiento);
    }
}