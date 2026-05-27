package order.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ubicaciones")
@Data
@NoArgsConstructor        // constructor vacío (requerido por JPA)
public class Ubicacion {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String direccion;

    @Column(name = "ubicacion_lat")
    private Double ubicacionLat;   // opcional, si el evento incluye ubicación geoespacial

    @Column(name = "ubicacion_lng")
    private Double ubicacionLng;
}