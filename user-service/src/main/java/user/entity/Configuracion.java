package user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "configuracion")
@Getter
@Setter
@NoArgsConstructor
public class Configuracion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String clave;

    @Column(length = 255)
    private String descripcion;

    @Column(nullable = false, length = 255)
    private String valor;

    @Column(nullable = false, length = 20)
    private String tipo;

    public Configuracion(String clave, String descripcion, String valor, String tipo) {
        this.clave = clave;
        this.descripcion = descripcion;
        this.valor = valor;
        this.tipo = tipo;
    }
}
