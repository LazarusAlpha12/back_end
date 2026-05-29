package order.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import order.entity.EstadoPedido;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PedidoUpdateDTO {
    
    private String origen;
    private String destino;
    private String descripcion;
    private EstadoPedido estado;     // solo ciertos roles pueden cambiar estado
    private Long repartidorId; // para asignar repartidor
    private Long clienteId;
}
