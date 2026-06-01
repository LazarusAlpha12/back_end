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
public class HistorialResponseDTO {
    Long pedidoId;
    String tipoEvento;
    EstadoPedido estado;
    LocalDateTime fechaHora;
    String observacion;
}
