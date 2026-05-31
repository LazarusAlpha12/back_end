package user.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import user.dto.PersonaRequestDTO;
import user.dto.PersonaResponseDTO;
import user.entity.Persona;
import user.repository.PersonaRepository;

@Service
@Transactional
public class PersonaService {

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private PersonaResponseDTO convertToDTO(Persona persona) {
        return new PersonaResponseDTO(
            persona.getId(),
            persona.getNombre(),
            persona.getApellido(),
            persona.getEmail(),
            persona.getRol()
        );
    }

    public List<PersonaResponseDTO> listarTodos() {
        return personaRepository.findAll().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    public PersonaResponseDTO obtenerPorId(Long id) {
        Persona persona = personaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Persona no encontrada con ID: " + id));
        return convertToDTO(persona);
    }

    public PersonaResponseDTO crear(PersonaRequestDTO request) {
        if (personaRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("El email ya se encuentra registrado: " + request.getEmail());
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new RuntimeException("La contraseña es obligatoria para la creación");
        }

        Persona persona = new Persona();
        persona.setNombre(request.getNombre());
        persona.setApellido(request.getApellido());
        persona.setEmail(request.getEmail());
        persona.setPassword(passwordEncoder.encode(request.getPassword()));
        persona.setRol(request.getRol());

        Persona guardada = personaRepository.save(persona);
        return convertToDTO(guardada);
    }

    public PersonaResponseDTO actualizar(Long id, PersonaRequestDTO request) {
        Persona persona = personaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Persona no encontrada con ID: " + id));

        // Validar si cambia de email que no esté duplicado
        if (!persona.getEmail().equalsIgnoreCase(request.getEmail()) && personaRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("El email ya se encuentra registrado por otro usuario: " + request.getEmail());
        }

        persona.setNombre(request.getNombre());
        persona.setApellido(request.getApellido());
        persona.setEmail(request.getEmail());
        persona.setRol(request.getRol());

        // Si viene contraseña nueva, encriptarla
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            persona.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        Persona guardada = personaRepository.save(persona);
        return convertToDTO(guardada);
    }

    public void eliminar(Long id) {
        if (!personaRepository.existsById(id)) {
            throw new RuntimeException("Persona no encontrada con ID: " + id);
        }
        personaRepository.deleteById(id);
    }
}
