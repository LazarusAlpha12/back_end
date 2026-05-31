package user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import user.entity.Rol;

@Data
@NoArgsConstructor
public class PersonaRequestDTO {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    private String apellido;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe ser un formato de correo electrónico válido")
    private String email;

    private String password; // Opcional en actualizaciones, validado en creación en el servicio

    @NotNull(message = "El rol es obligatorio")
    private Rol rol;
}
