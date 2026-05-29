package auth.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "repartidores")
public class Repartidor {

    @Id
    private Long personaId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "persona_id")
    private Persona persona;

    @Column(nullable = false)
    private Integer capacidad;

    @Column(nullable = false)
    private Boolean disponibilidad = true;

    public Repartidor() {}

    public Repartidor(Persona persona, Integer capacidad, Boolean disponibilidad) {
        this.persona = persona;
        this.capacidad = capacidad;
        this.disponibilidad = disponibilidad != null ? disponibilidad : true;
    }

    public Long getPersonaId() { return personaId; }
    public Persona getPersona() { return persona; }
    public Integer getCapacidad() { return capacidad; }
    public Boolean getDisponibilidad() { return disponibilidad; }
    public void setDisponibilidad(Boolean disponibilidad) { this.disponibilidad = disponibilidad; }
    public void setCapacidad(Integer capacidad) { this.capacidad = capacidad; }
}
