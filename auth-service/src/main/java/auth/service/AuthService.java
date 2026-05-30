package auth.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import auth.dto.LoginRequest;
import auth.dto.LoginResponse;
import auth.dto.RegisterRequest;
import auth.entity.OperadorLogistico;
import auth.entity.Persona;
import auth.entity.Repartidor;
import auth.entity.Rol;
import auth.entity.TokenBlacklist;
import auth.repository.OperadorLogisticoRepository;
import auth.repository.PersonaRepository;
import auth.repository.RepartidorRepository;
import auth.repository.TokenBlacklistRepository;

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
    private final TokenBlacklistRepository blacklistRepository;
    private final RepartidorRepository repartidorRepository;
    private final OperadorLogisticoRepository operadorRepository;

    public AuthService(PersonaRepository personaRepository,
                       JwtService jwtService,
                       AuthenticationManager authenticationManager,
                       PasswordEncoder passwordEncoder,
                       TokenBlacklistRepository blacklistRepository,
                       RepartidorRepository repartidorRepository,
                       OperadorLogisticoRepository operadorRepository) {
        this.personaRepository = personaRepository;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.blacklistRepository = blacklistRepository;
        this.repartidorRepository = repartidorRepository;
        this.operadorRepository = operadorRepository;
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
                passwordEncoder.encode(request.password()),
                request.rol()
        );
        
        personaRepository.save(persona);

        // Guardar datos de extensión según el rol
        if (request.rol() == Rol.REPARTIDOR) {
            if (request.capacidad() == null) {
                throw new IllegalArgumentException("El campo 'capacidad' es obligatorio para repartidores");
            }
            repartidorRepository.save(new Repartidor(persona, request.capacidad(), request.disponibilidad()));
        }

        if (request.rol() == Rol.OPERADOR_LOGISTICO) {
            if (request.adminId() == null) {
                throw new IllegalArgumentException("El campo 'adminId' es obligatorio para operadores logísticos");
            }
            Persona admin = personaRepository.findById(request.adminId())
                    .filter(p -> p.getRol() == Rol.ADMINISTRADOR)
                    .orElseThrow(() -> new IllegalArgumentException("El adminId no corresponde a un administrador válido"));
            operadorRepository.save(new OperadorLogistico(persona, admin));
        }

        String token = jwtService.generateToken(persona);
        return buildResponse(token, persona);
    }

    // ── Logout ─────────────────────────────────────────────────────────────────

    public void logout(String authHeader) {
        String token = authHeader.substring(7); // quita "Bearer "
        String jti = jwtService.extractJti(token);
        blacklistRepository.save(new TokenBlacklist(jti, jwtService.extractExpiration(token)));
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
