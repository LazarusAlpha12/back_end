package order.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PedidoRequestDTO {

    private String origen;
    private String destino;
    private String descripcion;
    private Long clienteId;   // obligatorio
}
