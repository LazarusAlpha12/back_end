package order.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "personas")
@Data
@NoArgsConstructor
public class Persona {

    @Id
    private Long id;
    private String nombre;
    private String apellido;
    private String email;
}
