package auth.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "operadores_logisticos")
public class OperadorLogistico {

    @Id
    private Long personaId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "persona_id")
    private Persona persona;

    @ManyToOne
    @JoinColumn(name = "admin_id", nullable = false)
    private Persona admin;

    public OperadorLogistico() {}

    public OperadorLogistico(Persona persona, Persona admin) {
        this.persona = persona;
        this.admin = admin;
    }

    public Long getPersonaId() { return personaId; }
    public Persona getPersona() { return persona; }
    public Persona getAdmin() { return admin; }
    public void setAdmin(Persona admin) { this.admin = admin; }
}
