package user.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import user.dto.PersonaRequestDTO;
import user.dto.PersonaResponseDTO;
import user.service.PersonaService;

@RestController
@RequestMapping("/api/usuarios")
@Validated
public class PersonaController {

    @Autowired
    private PersonaService personaService;

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('ADMINISTRADOR') or hasRole('OPERADOR_LOGISTICO')")
    public ResponseEntity<List<PersonaResponseDTO>> listarTodos() {
        return ResponseEntity.ok(personaService.listarTodos());
    }

    @GetMapping("/repartidores")
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('OPERADOR_LOGISTICO')")
    public ResponseEntity<List<PersonaResponseDTO>> listarRepartidores() {
        return ResponseEntity.ok(personaService.listarRepartidores());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('ADMINISTRADOR') or #id == authentication.principal")
    public ResponseEntity<PersonaResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(personaService.obtenerPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('ADMINISTRADOR')")
    public ResponseEntity<PersonaResponseDTO> crear(@Valid @RequestBody PersonaRequestDTO request) {
        PersonaResponseDTO creada = personaService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('ADMINISTRADOR') or #id == authentication.principal")
    public ResponseEntity<PersonaResponseDTO> actualizar(@PathVariable Long id, @Valid @RequestBody PersonaRequestDTO request) {
        return ResponseEntity.ok(personaService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        personaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
