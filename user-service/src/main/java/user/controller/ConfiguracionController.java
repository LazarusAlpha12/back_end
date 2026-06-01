package user.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import user.entity.Configuracion;
import user.repository.ConfiguracionRepository;

@RestController
@RequestMapping("/api/config")
public class ConfiguracionController {

    private final ConfiguracionRepository configRepo;

    public ConfiguracionController(ConfiguracionRepository configRepo) {
        this.configRepo = configRepo;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<Map<String, Object>>> getConfig() {
        List<Map<String, Object>> configs = configRepo.findAll().stream()
            .map(c -> Map.<String, Object>of(
                "id", c.getId().toString(),
                "clave", c.getClave(),
                "descripcion", c.getDescripcion() != null ? c.getDescripcion() : c.getClave(),
                "valor", c.getValor(),
                "tipo", c.getTipo()
            ))
            .collect(Collectors.toList());
        return ResponseEntity.ok(configs);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Map<String, String>> updateConfig(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Configuracion config = configRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Configuración no encontrada"));
        String nuevoValor = body.get("valor");
        if (nuevoValor != null) {
            config.setValor(nuevoValor);
            configRepo.save(config);
        }
        return ResponseEntity.ok(Map.of("status", "ok"));
    }
}
