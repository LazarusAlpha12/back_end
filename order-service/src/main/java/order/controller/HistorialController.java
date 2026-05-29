package order.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import order.dto.HistorialFiltroDTO;
import order.service.HistorialService;

@RestController
@RequestMapping("/api/pedidos")
public class HistorialController {

    @Autowired
    private HistorialService historialService;
    
    @GetMapping("/{pedidoId}/historial")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<HistorialFiltroDTO>> obtenerHistorial(@PathVariable Long pedidoId) {
        List<HistorialFiltroDTO> historial = historialService.obtenerHistorialPorPedido(pedidoId);
        return ResponseEntity.ok(historial);
    }
}
