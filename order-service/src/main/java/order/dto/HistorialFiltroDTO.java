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

    // Constructor manual para que JPA lo encuentre
    public HistorialFiltroDTO(Long id, String tipoEvento, LocalDateTime fechaHora, String operadorNombre) {
        this.id = id;
        this.tipoEvento = tipoEvento;
        this.fechaHora = fechaHora;
        this.operadorNombre = operadorNombre;
    }
}
