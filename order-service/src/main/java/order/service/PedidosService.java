package order.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import order.dto.PedidoRequestDTO;
import order.dto.PedidoResponseDTO;
import order.dto.PedidoUpdateDTO;
import order.dto.UbicacionRequestDTO;
import order.entity.EstadoPedido;
import order.entity.Historial;
import order.entity.Pedido;
import order.entity.Persona;
import order.entity.Ubicacion;
import order.repository.HistorialRepositorio;
import order.repository.PedidoRepositorio;
import order.repository.PersonaRepositorio;
import order.repository.UbicacionRepositorio;

@Service
@Transactional
public class PedidosService {

    @Autowired
    private PedidoRepositorio pedidoRepositorio;

    @Autowired
    private HistorialRepositorio historialRepositorio;

    @Autowired
    private PersonaRepositorio personaRepositorio; // para validar clienteId

    @Autowired
    private UbicacionRepositorio ubicacionRepositorio;

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
            repartidorNombre,
            pedido.getDescripcion()
        );
    }
    

    public PedidoResponseDTO crearPedidoLogistico(PedidoRequestDTO request) {

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
        evento.setOperadorId(SecurityUtils.getCurrentUserId());
        historialRepositorio.save(evento);
        return convertirADTO(saved);
    }

    public PedidoResponseDTO crearPedidoCliente(PedidoRequestDTO request) {

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
        evento.setOperadorId(null);
        historialRepositorio.save(evento);
        return convertirADTO(saved);
    }



    public Page<PedidoResponseDTO> listarPedidos(
        
        EstadoPedido estado, Long clienteId, Long repartidorId, Long id,
        LocalDate fechaDesde, LocalDate fechaHasta,
        LocalTime horaDesde, LocalTime horaHasta,
        String ubicacion, Pageable pageable) {

            Set<Long> pedidoIds = null;

            LocalDateTime fechaDesdeDateTime = fechaDesde != null 
            ? fechaDesde.atStartOfDay() : null;

            LocalDateTime fechaHastaDateTime = fechaHasta != null 
            ? fechaHasta.atTime(LocalTime.MAX) : null;

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");
            String horaDesdeStr = horaDesde != null ? horaDesde.format(fmt) : null;
            String horaHastaStr = horaHasta != null ? horaHasta.format(fmt) : null;

            if (ubicacion != null && !ubicacion.isBlank()) {
                pedidoIds = historialRepositorio.findPedidoIdsByUbicacionContaining(ubicacion);
                if (pedidoIds.isEmpty()) {
                    return Page.empty(pageable);
                }
            }

            return pedidoRepositorio.buscarConFiltros(
                estado, clienteId, repartidorId, id,
                fechaDesdeDateTime, fechaHastaDateTime, horaDesdeStr, horaHastaStr,
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

        // Construir mensaje de cambios para el historial
        StringBuilder cambios = new StringBuilder();

        if (dto.getOrigen() != null && !dto.getOrigen().equals(pedido.getOrigen())) {
            cambios.append("Origen: ").append(pedido.getOrigen()).append(" → ").append(dto.getOrigen()).append("; ");
            pedido.setOrigen(dto.getOrigen());
        }
        if (dto.getDestino() != null && !dto.getDestino().equals(pedido.getDestino())) {
            cambios.append("Destino: ").append(pedido.getDestino()).append(" → ").append(dto.getDestino()).append("; ");
            pedido.setDestino(dto.getDestino());
        }
        if (dto.getDescripcion() != null && !dto.getDescripcion().equals(pedido.getDescripcion())) {
            cambios.append("Descripción: ").append(pedido.getDescripcion()).append(" → ").append(dto.getDescripcion()).append("; ");
            pedido.setDescripcion(dto.getDescripcion());
        }
        if (dto.getClienteId() != null && !dto.getClienteId().equals(pedido.getClienteId())) {
            cambios.append("Cliente: ").append(pedido.getClienteId()).append(" → ").append(dto.getClienteId()).append("; ");
            pedido.setClienteId(dto.getClienteId());
        }
        if (dto.getRepartidorId() != null && !dto.getRepartidorId().equals(pedido.getRepartidorId())) {
            cambios.append("Repartidor: ").append(pedido.getRepartidorId()).append(" → ").append(dto.getRepartidorId()).append("; ");
            pedido.setRepartidorId(dto.getRepartidorId());
        }
        if (dto.getEstado() != null && !dto.getEstado().equals(pedido.getEstado())) {
            cambios.append("Estado: ").append(pedido.getEstado()).append(" → ").append(dto.getEstado()).append("; ");
            pedido.setEstado(dto.getEstado());
        }

        // Solo guardar si hubo cambios
        Pedido updated = pedidoRepositorio.save(pedido);

        if (cambios.length() > 0) {
            Historial evento = new Historial();
            evento.setPedido(updated);
            evento.setTipoEvento("ACTUALIZADO");
            evento.setObservacion(cambios.toString());
            evento.setEstado(updated.getEstado());
            evento.setOperadorId(SecurityUtils.getCurrentUserId()); // obtiene el userId del SecurityContextHolder
            evento.setFechaHora(LocalDateTime.now());
            historialRepositorio.save(evento);
        }

        return convertirADTO(updated);
    }

    // Cambiar estado (con registro de historial)
    public PedidoResponseDTO cambiarEstado(Long id, String nuevoEstado) {

        Pedido pedido = pedidoRepositorio.findById(id)
            .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));
        pedido.setEstado(EstadoPedido.valueOf(nuevoEstado));
        Pedido updated = pedidoRepositorio.save(pedido);

        Historial evento = new Historial();

        evento.setPedido(updated);
        evento.setTipoEvento("ESTADO_CAMBIADO");
        evento.setEstado(updated.getEstado());
        evento.setObservacion("Nuevo estado: " + nuevoEstado);
        evento.setOperadorId(SecurityUtils.getCurrentUserId());
        evento.setFechaHora(LocalDateTime.now());
        historialRepositorio.save(evento);

        return convertirADTO(updated);

    }

    // Asignar repartidor
    public PedidoResponseDTO asignarRepartidor(Long id, Long repartidorId) {
        
        // Validar que el repartidor exista (opcional)
        if (!personaRepositorio.existsById(repartidorId)) {
            throw new RuntimeException("Repartidor no encontrado con id: " + repartidorId);
        } else if (!personaRepositorio.getRolPersona(id).equals("REPARTIDOR")) 
        {
            throw new RuntimeException("No se puede asginar la persona con id: " + repartidorId);
        }

        Pedido pedido = pedidoRepositorio.findById(id)
            .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));
        pedido.setRepartidorId(repartidorId);

        Pedido updated = pedidoRepositorio.save(pedido);

        Historial evento = new Historial();
        evento.setPedido(updated);
        evento.setEstado(EstadoPedido.ASIGNADO);
        evento.setOperadorId(SecurityUtils.getCurrentUserId());
        evento.setTipoEvento("ASIGNADO");
        evento.setObservacion("Asignado a repartidor ID: " + repartidorId);
        evento.setFechaHora(LocalDateTime.now());
        historialRepositorio.save(evento);

        return convertirADTO(updated);

    }

    public void registrarUbicacion(Long pedidoId, UbicacionRequestDTO dto) {

        Pedido pedido = pedidoRepositorio.findById(pedidoId)
            .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        // Crear o buscar Ubicacion (se puede crear una nueva cada vez)
        Ubicacion ubicacion = new Ubicacion();

        ubicacion.setDireccion(dto.getDireccion());

        if (dto.getLatitud()!= null) ubicacion.setUbicacionLat(dto.getLatitud());
        if (dto.getLongitud() != null) ubicacion.setUbicacionLng(dto.getLongitud());

        ubicacionRepositorio.save(ubicacion);

        // Registrar historial

        Historial evento = new Historial();

        evento.setPedido(pedido);
        evento.setTipoEvento("UBICACION_ACTUALIZADA");
        evento.setUbicacion(ubicacion);
        evento.setOperadorId(SecurityUtils.getCurrentUserId());
        evento.setFechaHora(LocalDateTime.now());

        historialRepositorio.save(evento);

    }

    public order.dto.ReporteDTO obtenerReportes() {
        List<Pedido> pedidos = pedidoRepositorio.findAll();
        Map<String, Long> porEstado = new HashMap<>();
        long total = 0;
        for (Pedido p : pedidos) {
            String estado = p.getEstado().name();
            porEstado.merge(estado, 1L, Long::sum);
            total++;
        }
        return new order.dto.ReporteDTO(total, porEstado);
    }
}
