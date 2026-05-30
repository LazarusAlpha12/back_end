package order.controller;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import order.dto.PedidoRequestDTO;
import order.dto.PedidoResponseDTO;
import order.dto.PedidoUpdateDTO;
import order.dto.UbicacionRequestDTO;
import order.entity.EstadoPedido;
import order.service.PedidosService;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    @Autowired
    private PedidosService pedidoService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERADOR_LOGISTICO')")
    public ResponseEntity<Page<PedidoResponseDTO>> listarPedidos(
            @RequestParam(required = false) EstadoPedido estado,
            @RequestParam(required = false) Long clienteId,
            @RequestParam(required = false) Long repartidorId,
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime horaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime horaHasta,
            @RequestParam(required = false) String ubicacion,
            @PageableDefault(size = 10, sort = "fechaCreacion", direction = Direction.ASC) Pageable pageable) {

        Page<PedidoResponseDTO> pagina = pedidoService.listarPedidos(
                estado, clienteId, repartidorId, id,
                fechaDesde, fechaHasta, horaDesde, horaHasta,
                ubicacion, pageable);

        return ResponseEntity.ok(pagina);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERADOR_LOGISTICO') or hasRole('CLIENTE')")
    public ResponseEntity<PedidoResponseDTO> obtenerPedido(@PathVariable Long id) {
        PedidoResponseDTO dto = pedidoService.obtenerPedido(id);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/logistico")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERADOR_LOGISTICO')")
    public ResponseEntity<PedidoResponseDTO> crearPedidoLogistico(@RequestBody PedidoRequestDTO request) {
        PedidoResponseDTO created = pedidoService.crearPedidoLogistico(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/cliente")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<PedidoResponseDTO> crearPedidoCliente(@RequestBody PedidoRequestDTO request) {
        PedidoResponseDTO created = pedidoService.crearPedidoCliente(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERADOR_LOGISTICO')")
    public ResponseEntity<PedidoResponseDTO> actualizarPedido(@PathVariable Long id, @RequestBody PedidoUpdateDTO dto) {
        PedidoResponseDTO updated = pedidoService.actualizarPedido(id, dto);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('REPARTIDOR') or hasRole('ADMIN') or hasRole('OPERADOR_LOGISTICO')")
    public ResponseEntity<PedidoResponseDTO> cambiarEstado(@PathVariable Long id, @RequestParam String nuevoEstado) {
        PedidoResponseDTO updated = pedidoService.cambiarEstado(id, nuevoEstado);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/asignar")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERADOR_LOGISTICO')")
    public ResponseEntity<PedidoResponseDTO> asignarRepartidor(@PathVariable Long id, @RequestParam Long repartidorId) {
        PedidoResponseDTO updated = pedidoService.asignarRepartidor(id, repartidorId);
        return ResponseEntity.ok(updated);
    }

     // Registrar ubicación (repartidor)
    @PostMapping("/{id}/ubicacion")
    @PreAuthorize("hasRole('REPARTIDOR') or hasRole('ADMIN') or hasRole('OPERADOR_LOGISTICO')")
    public ResponseEntity<Void> registrarUbicacion(@PathVariable Long id, @RequestBody UbicacionRequestDTO dto) {
        pedidoService.registrarUbicacion(id, dto);
        return ResponseEntity.ok().build();
    }
}
