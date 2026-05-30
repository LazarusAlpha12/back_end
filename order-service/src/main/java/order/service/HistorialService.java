package order.service;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import order.dto.HistorialFiltroDTO;
import order.repository.HistorialRepositorio;

@Service
public class HistorialService {
    
    @Autowired
    private HistorialRepositorio historialRepository;

    public List<HistorialFiltroDTO> obtenerHistorialPorPedido(Long pedidoId) {
        return historialRepository.findHistorialWithOperadorByPedidoId(pedidoId);
    }
}
