package auth.service;

import auth.dto.LoginRequest;
import auth.dto.LoginResponse;
import auth.dto.RegisterRequest;
import auth.entity.Persona;
import auth.repository.PersonaRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Lógica de negocio de autenticación.
 * Orquesta: validación de credenciales → generación de JWT → construcción de respuesta.
 */
@Service
public class AuthService {

    private final PersonaRepository personaRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    public AuthService(PersonaRepository personaRepository,
                       JwtService jwtService,
                       AuthenticationManager authenticationManager,
                       PasswordEncoder passwordEncoder) {
        this.personaRepository = personaRepository;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
    }

    // ── Login ──────────────────────────────────────────────────────────────────

    /**
     * Valida credenciales y devuelve un JWT si son correctas.
     * Si el email no existe o el password es incorrecto, AuthenticationManager
     * lanza BadCredentialsException (manejada en GlobalExceptionHandler → 401).
     */
    public LoginResponse login(LoginRequest request) {
        // Spring Security valida email + password contra la BD
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        // Si llegamos aquí, las credenciales son correctas
        Persona persona = personaRepository.findByEmail(request.email())
                .orElseThrow(); // imposible que falle si authenticate() pasó

        String token = jwtService.generateToken(persona);

        return buildResponse(token, persona);
    }

    // ── Register ───────────────────────────────────────────────────────────────

    /**
     * Crea un nuevo usuario y devuelve su JWT.
     * Útil para pruebas con Postman y para que el admin registre usuarios.
     */
    public LoginResponse register(RegisterRequest request) {
        if (personaRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Ya existe un usuario con el email: " + request.email());
        }

        Persona persona = new Persona(
                request.nombre(),
                request.apellido(),
                request.email(),
                passwordEncoder.encode(request.password()), // BCrypt
                request.rol()
        );

        personaRepository.save(persona);

        String token = jwtService.generateToken(persona);

        return buildResponse(token, persona);
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private LoginResponse buildResponse(String token, Persona persona) {
        return new LoginResponse(
                token,
                persona.getId(),
                persona.getNombre(),
                persona.getEmail(),
                persona.getRol().toFrontendValue() // "Administrador", "OperadorLogistico", etc.
        );
    }
}
