package order.service;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import order.dto.HistorialFiltroDTO;
import order.dto.HistorialResponseDTO;
import order.entity.EstadoPedido;
import order.repository.HistorialRepositorio;

@Service
public class HistorialService {
    
    @Autowired
    private HistorialRepositorio historialRepository;
    

    public List<HistorialFiltroDTO> obtenerHistorialPorPedido(Long pedidoId) {
        return historialRepository.findHistorialWithOperadorByPedidoId(pedidoId);
    }

    public Page<HistorialResponseDTO> obtenerHistorialPorPedido(
        Long pedidoId,
        String tipoEvento,
        EstadoPedido estado,
        LocalDate fechaDesde,   // controlador puede recibir LocalDate más fácil
        LocalDate fechaHasta,
        String observacion, Pageable page) {

        // Convertir fechas a LocalDateTime para la consulta
        LocalDateTime fechaDesdeDateTime = fechaDesde != null ? fechaDesde.atStartOfDay() : null;
        LocalDateTime fechaHastaDateTime = fechaHasta != null ? fechaHasta.atTime(LocalTime.MAX) : null;

        return historialRepository.filtrarHistorial(
                pedidoId, tipoEvento, estado,
                fechaDesdeDateTime, fechaHastaDateTime,
                observacion, page);
    }
}
