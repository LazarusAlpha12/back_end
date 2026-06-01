package order.dto;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class HistorialFiltroDTO {
    private Long id;
    private String tipoEvento;
    private LocalDateTime fechaHora;
    private String operadorNombre;
    private String observacion;
    private Long pedidoId;

    public HistorialFiltroDTO(Long id, String tipoEvento, LocalDateTime fechaHora, String operadorNombre, String observacion, Long pedidoId) {
        this.id = id;
        this.tipoEvento = tipoEvento;
        this.fechaHora = fechaHora;
        this.operadorNombre = operadorNombre;
        this.observacion = observacion;
        this.pedidoId = pedidoId;
    }
}
