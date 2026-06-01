package order.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import order.dto.HistorialFiltroDTO;
import order.dto.HistorialResponseDTO;
import order.entity.EstadoPedido;
import order.service.HistorialService;


@RestController
@RequestMapping("/api/pedidos")
public class HistorialController {

    @Autowired
    private HistorialService historialService;
    
    // Obtiene el historial completo de un pedido
    @GetMapping("operador/{pedidoId}/historial")
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('OPERADOR_LOGISTICO')")
    public ResponseEntity<List<HistorialFiltroDTO>> obtenerHistorial(@PathVariable Long pedidoId) {
        List<HistorialFiltroDTO> historial = historialService.obtenerHistorialPorPedido(pedidoId);
        return ResponseEntity.ok(historial);
    }

    // Obtiene el historial de un pedido
    // aplicando filtros especializados
    @GetMapping("/{pedidoId}/historial")
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('OPERADOR_LOGISTICO')")
    public ResponseEntity<Page<HistorialResponseDTO>> obtenerHistorial( 
        @PathVariable Long pedidoId,
        @RequestParam(required = false) String tipoEvento,
        @RequestParam(required = false) EstadoPedido estado,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
        @RequestParam(required = false) String observacion, 
        @PageableDefault(size = 10, sort = "fechaHora", direction = Direction.ASC) Pageable pageable) 
    {
        Page<HistorialResponseDTO> historial = historialService.obtenerHistorialPorPedido(pedidoId, tipoEvento, estado, 
            fechaDesde, fechaHasta, observacion, pageable);
        return ResponseEntity.ok(historial);
    }
    
}
