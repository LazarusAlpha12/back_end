package order.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import order.dto.PedidoRequestDTO;
import order.dto.PedidoResponseDTO;
import order.dto.PedidoUpdateDTO;
import order.entity.EstadoPedido;
import order.entity.Historial;
import order.entity.Pedido;
import order.entity.Persona;
import order.repository.HistorialRepositorio;
import order.repository.PedidoRepositorio;
import order.repository.PersonaRepositorio;

@Service
@Transactional
public class PedidosService {

    @Autowired
    private PedidoRepositorio pedidoRepositorio;

    @Autowired
    private HistorialRepositorio historialRepositorio;

    @Autowired
    private PersonaRepositorio personaRepositorio; // para validar clienteId

    private PedidoResponseDTO convertirADTO(Pedido pedido) {

        // Obtener nombres de cliente y repartidor (se puede hacer con consultas adicionales)
        String clienteNombre = "";
        if (pedido.getClienteId() != null) {
            clienteNombre = personaRepositorio.findById(pedido.getClienteId())
                .map(Persona::getNombre).orElse("");
        }
        String repartidorNombre = "";
        if (pedido.getRepartidorId() != null) {
            repartidorNombre = personaRepositorio.findById(pedido.getRepartidorId())
                .map(Persona::getNombre).orElse("");
        }

        return new PedidoResponseDTO(
            pedido.getId(),
            pedido.getOrigen(),
            pedido.getDestino(),
            pedido.getEstado(),
            pedido.getFechaCreacion(),
            pedido.getClienteId(),
            clienteNombre,
            pedido.getRepartidorId(),
            repartidorNombre
        );
    }
    

    public PedidoResponseDTO crearPedido(PedidoRequestDTO request) {
        // Validar que el cliente existe
        if (!personaRepositorio.existsById(request.getClienteId())) {
            throw new RuntimeException("Cliente no encontrado");
        }
        // Crear entidad
        Pedido pedido = new Pedido();
        pedido.setOrigen(request.getOrigen());
        pedido.setDestino(request.getDestino());
        pedido.setDescripcion(request.getDescripcion());
        pedido.setClienteId(request.getClienteId());
        pedido.setEstado(EstadoPedido.PENDIENTE);
        pedido.setFechaCreacion(LocalDateTime.now());
        Pedido saved = pedidoRepositorio.save(pedido);
        // Registrar evento de historial
        Historial evento = new Historial();
        evento.setPedido(saved);
        evento.setTipoEvento("CREADO");
        evento.setEstado(saved.getEstado());
        evento.setFechaHora(LocalDateTime.now());
        //Obtener operadorId desde el contexto de seguridad (si quieres guardarlo)
        //evento.setOperadorId(obtenerIdUsuarioActual());
        historialRepositorio.save(evento);
        return convertirADTO(saved);
    }

    public Page<PedidoResponseDTO> listarPedidos(
        
        String estado, Long clienteId, Long repartidorId, Long id,
        LocalDate fechaDesde, LocalDate fechaHasta,
        LocalTime horaDesde, LocalTime horaHasta,
        String ubicacion, Pageable pageable) {

            Set<Long> pedidoIds = null;

            if (ubicacion != null && !ubicacion.isBlank()) {
                pedidoIds = historialRepositorio.findPedidoIdsByUbicacionContaining(ubicacion);
                if (pedidoIds.isEmpty()) {
                    return Page.empty(pageable);
                }
            }

            return pedidoRepositorio.buscarConFiltros(
                estado, clienteId, repartidorId, id,
                fechaDesde, fechaHasta, horaDesde, horaHasta,
                pedidoIds, pageable);
    }


    public PedidoResponseDTO obtenerPedido(Long id) {
        Pedido pedido = pedidoRepositorio.findById(id)
            .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));
        return convertirADTO(pedido);
    }

    public PedidoResponseDTO actualizarPedido(Long id, PedidoUpdateDTO dto) {
        Pedido pedido = pedidoRepositorio.findById(id)
            .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));
        if (dto.getOrigen() != null) pedido.setOrigen(dto.getOrigen());
        if (dto.getDestino() != null) pedido.setDestino(dto.getDestino());
        if (dto.getDescripcion() != null) pedido.setDescripcion(dto.getDescripcion());
        Pedido updated = pedidoRepositorio.save(pedido);
        // Opcional: registrar evento en historial
        return convertirADTO(updated);
    }
}