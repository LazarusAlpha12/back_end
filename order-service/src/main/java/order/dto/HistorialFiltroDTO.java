package order.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HistorialFiltroDTO {
    private Long id;
    private String tipoEvento;
    private LocalDateTime fechaHora;
    private String operadorNombre;
}
