package auth.dto;

/**
 * DTO de entrada para el endpoint POST /auth/login.
 */
public record LoginRequest(String email, String password) {}
