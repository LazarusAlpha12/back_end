package order.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import order.dto.PedidoResponseDTO;
import order.entity.Pedido;

@Repository
public interface PedidoRepositorio extends JpaRepository<Pedido, Long>{

   @Query("SELECT new order.dto.PedidoResponseDTO(" +
      "p.id, p.origen, p.destino, p.estado, p.fechaCreacion, " +
      "c.id, c.nombre, " +
      "r.id, r.nombre) " +
      "FROM Pedido p " +
      "LEFT JOIN Persona c ON p.clienteId = c.id " +
      "LEFT JOIN Persona r ON p.repartidorId = r.id")
   List<PedidoResponseDTO> findAllPedidosConDetalles();

   @Query("SELECT new order.dto.PedidoResponseDTO(" +
      "p.id, p.origen, p.destino, p.estado, p.fechaCreacion, " +
      "c.id, c.nombre, " +
      "r.id, r.nombre) " +
      "FROM Pedido p " +
      "LEFT JOIN Persona c ON p.clienteId = c.id " +
      "LEFT JOIN Persona r ON p.repartidorId = r.id")
   Page<PedidoResponseDTO> findAllPedidosConDetalles(Pageable pageable);

   @Query("SELECT new order.dto.PedidoResponseDTO(" +
      "p.id, p.origen, p.destino, p.estado, p.fechaCreacion, " +
      "c.id, c.nombre, " +
      "r.id, r.nombre) " +
      "FROM Pedido p " +
       "LEFT JOIN Persona c ON p.clienteId = c.id " +
       "LEFT JOIN Persona r ON p.repartidorId = r.id " +
       "WHERE (:estado IS NULL OR p.estado = :estado) " +
       "AND (:clienteId IS NULL OR p.clienteId = :clienteId) " +
       "AND (:repartidorId IS NULL OR p.repartidorId = :repartidorId) " +
       "AND (:id IS NULL OR p.id = :id) " +
       "AND (:fechaDesde IS NULL OR p.fechaCreacion >= :fechaDesde) " +
       "AND (:fechaHasta IS NULL OR p.fechaCreacion <= :fechaHasta) " +
       "AND (:horaDesde IS NULL OR TIME(p.fechaCreacion) >= :horaDesde) " +
       "AND (:horaHasta IS NULL OR TIME(p.fechaCreacion) <= :horaHasta) " +
       "AND (:pedidoIds IS NULL OR p.id IN :pedidoIds)")
   Page<PedidoResponseDTO> buscarConFiltros(
      @Param("estado") String estado,
      @Param("clienteId") Long clienteId,
      @Param("repartidorId") Long repartidorId,
      @Param("id") Long id,
      @Param("fechaDesde") LocalDate fechaDesde,
      @Param("fechaHasta") LocalDate fechaHasta,
      @Param("horaDesde") LocalTime horaDesde,
      @Param("horaHasta") LocalTime horaHasta,
      Pageable pageable);
}