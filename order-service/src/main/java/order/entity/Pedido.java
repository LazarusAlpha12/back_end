package order.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "pedidos")
@Data                     // genera getters, setters, toString, equals, hashCode
@NoArgsConstructor        // constructor vacío (requerido por JPA)
public class Pedido {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Version // Para evitar condiciones de carrera
    private Integer version; //Esta clase se modifica constantemente

    @Column(nullable = false)
    private String origen;

    @Column(nullable = false)
    private String destino; 

    @Column(nullable = false)
    private String descripcion;
    
    @Enumerated(EnumType.STRING)   // ← guarda el nombre del enum ("PENDIENTE", etc.)
    @Column(nullable = false)
    private EstadoPedido estado;

    @Column(name = "cliente_id", nullable=false)
    private Long clienteId;

    @Column(name = "repartidor_id")
    private Long repartidorId;
    
    @Column(name = "fecha_creacion", updatable = false)
    @CreationTimestamp
    private LocalDateTime fechaCreacion;
}