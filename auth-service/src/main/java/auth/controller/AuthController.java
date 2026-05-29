package auth.controller;

import auth.dto.LoginRequest;
import auth.dto.LoginResponse;
import auth.dto.RegisterRequest;
import auth.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Endpoints públicos de autenticación.
 * Ambos endpoints están en /auth/** y son permitidos sin token (ver SecurityConfig).
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * POST /auth/login
     * Body: { "email": "admin@test.com", "password": "password123" }
     * Response: { "token": "eyJ...", "id": 1, "nombre": "Admin", "email": "...", "rol": "Administrador" }
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /auth/register
     * Permite crear usuarios para pruebas. En producción debería requerir rol ADMINISTRADOR.
     * Body: { "nombre": "...", "apellido": "...", "email": "...", "password": "...", "rol": "CLIENTE" }
     */
    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@RequestBody RegisterRequest request) {
        LoginResponse response = authService.register(request);
        return ResponseEntity.status(201).body(response);
    }

    /**
     * POST /auth/logout
     * Header: Authorization: Bearer <token>
     * Revoca el token añadiendo su JTI a la blacklist — no se podrá usar aunque no haya expirado.
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            @RequestHeader("Authorization") String authHeader) {
        authService.logout(authHeader);
        return ResponseEntity.ok(Map.of("message", "Sesión cerrada correctamente"));
    }
}
