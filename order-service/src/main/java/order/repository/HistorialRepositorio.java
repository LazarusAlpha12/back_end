package order.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import order.dto.HistorialFiltroDTO;
import order.dto.HistorialResponseDTO;
import order.entity.EstadoPedido;
import order.entity.Historial;

@Repository
public interface HistorialRepositorio extends JpaRepository<Historial, Long>{
    // Historial de un pedido (todos los eventos)
    List<Historial> findByPedidoId(Long pedidoId);

    // Con paginación
    Page<Historial> findByPedidoId(Long pedidoId, Pageable pageable);

    @Query("SELECT new order.dto.HistorialResponseDTO(h.pedido.id, " +
       "h.tipoEvento, h.estado, h.fechaHora, h.observacion) " +
       "FROM Historial h WHERE h.pedido.id = :pedidoId " +
       "AND (:tipoEvento IS NULL OR h.tipoEvento = :tipoEvento) " +
       "AND (:estado IS NULL OR h.estado = :estado) " +
       "AND (:fechaDesde IS NULL OR h.fechaHora >= :fechaDesde) " +
       "AND (:fechaHasta IS NULL OR h.fechaHora <= :fechaHasta) " +
       "AND (:observacion IS NULL OR h.observacion LIKE CONCAT('%', :observacion, '%'))")
    Page<HistorialResponseDTO> filtrarHistorial(
        @Param("pedidoId") Long pedidoId,
        @Param("tipoEvento") String tipoEvento,
        @Param("estado") EstadoPedido estado,
        @Param("fechaDesde") LocalDateTime fechaDesde,
        @Param("fechaHasta") LocalDateTime fechaHasta,
        @Param("observacion") String observacion, 
        Pageable page);

    // Obtener eventos con datos de operador (usando JOIN) devolviendo DTO
    @Query("SELECT new order.dto.HistorialFiltroDTO(" +
           "h.id, h.tipoEvento, h.fechaHora, p.nombre) " +
           "FROM Historial h " +
           "LEFT JOIN Persona p ON h.operadorId = p.id " +
           "WHERE h.pedido.id = :pedidoId")
    List<HistorialFiltroDTO> findHistorialWithOperadorByPedidoId(@Param("pedidoId") Long pedidoId);

    /**
     * Devuelve los IDs de los pedidos que tienen al menos un evento de historial
     * asociado a una ubicación cuya dirección contenga el texto dado (case-insensitive).
     */
    @Query("SELECT DISTINCT h.pedido.id FROM Historial h " +
           "JOIN h.ubicacion u " +
           "WHERE LOWER(u.direccion) LIKE LOWER(CONCAT('%', :ubicacion, '%'))")
    Set<Long> findPedidoIdsByUbicacionContaining(@Param("ubicacion") String ubicacion);
}