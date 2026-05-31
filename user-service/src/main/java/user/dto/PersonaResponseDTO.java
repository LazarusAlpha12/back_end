package user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import user.entity.Rol;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PersonaResponseDTO {

    private Long id;
    private String nombre;
    private String apellido;
    private String email;
    private Rol rol;
}
