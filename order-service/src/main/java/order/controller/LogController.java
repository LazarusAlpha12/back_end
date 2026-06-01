package order.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import order.entity.Historial;
import order.repository.HistorialRepositorio;

@RestController
@RequestMapping("/api/logs")
public class LogController {

    private final HistorialRepositorio historialRepo;

    public LogController(HistorialRepositorio historialRepo) {
        this.historialRepo = historialRepo;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<Map<String, Object>>> getLogs() {
        Page<Historial> page = historialRepo.findAll(
            PageRequest.of(0, 200, Sort.by(Sort.Direction.DESC, "fechaHora"))
        );
        List<Map<String, Object>> logs = page.getContent().stream()
            .map(h -> Map.<String, Object>of(
                "id", h.getId(),
                "timestamp", h.getFechaHora() != null ? h.getFechaHora().toString() : "",
                "tipo", h.getTipoEvento(),
                "usuarioId", h.getOperadorId() != null ? h.getOperadorId() : 0,
                "descripcion", h.getObservacion() != null ? h.getObservacion() : ""
            ))
            .collect(Collectors.toList());
        return ResponseEntity.ok(logs);
    }
}
