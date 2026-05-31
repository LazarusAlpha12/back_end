package order.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import order.entity.EstadoPedido;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PedidoResponseDTO {

    private Long id;
    private String origen;
    private String destino;
    private EstadoPedido estado;
    private LocalDateTime fechaCreacion;
    private Long clienteId;
    private String clienteNombre;
    private Long repartidorId;
    private String repartidorNombre;
    private String descripcion;

     public PedidoResponseDTO(Long id, String origen, String destino,
                             EstadoPedido estado, LocalDateTime fechaCreacion,
                             Long clienteId, String clienteNombre,
                             Long repartidorId, String repartidorNombre) {
        this.id = id;
        this.origen = origen;
        this.destino = destino;
        this.estado = estado;
        this.fechaCreacion = fechaCreacion;
        this.clienteId = clienteId;
        this.clienteNombre = clienteNombre;
        this.repartidorId = repartidorId;
        this.repartidorNombre = repartidorNombre;
        // descripcion queda null por defecto
    }
}
