package order.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "historial_movimiento")
@Getter                     // Como hay una relacion bidireccional, prefiero
@Setter                    // dejarlo con getter y setter para evitar bucles con @Data
@NoArgsConstructor        // constructor vacío (requerido por JPA)
public class Historial {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    /*
    Un pedido se asocia a varias instancias del historial
    */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    /*
    Una ubicación se puede asociar a varias instancias del historial
    */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ubicacion_id")
    private Ubicacion ubicacion;

    @Column(name = "operador_id")
    private Long operadorId;

    @Column(name = "tipo_evento", nullable = false, length = 50)
    private String tipoEvento;   // "CREADO", "ESTADO_CAMBIADO", "ASIGNADO", "UBICACION_ACTUALIZADA"

    @Enumerated(EnumType.STRING)   // ← guarda el nombre del enum ("PENDIENTE", etc.)
    @Column(nullable = false)
    private EstadoPedido estado;

    @Column(name = "observacion", length = 255)
    private String observacion;   // detalle adicional (ej. "estado cambiado de PENDIENTE a ASIGNADO")

    @CreationTimestamp
    @Column(name = "fecha_hora", updatable = false, nullable = false)
    private LocalDateTime fechaHora;
}
