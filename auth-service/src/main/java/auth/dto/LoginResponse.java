package auth.dto;

/**
 * DTO de salida para POST /auth/login y POST /auth/register.
 * El campo 'rol' devuelve el string que el frontend espera (ej: "Administrador", "OperadorLogistico").
 * El campo 'token' es el JWT firmado con HMAC-SHA256.
 */
public record LoginResponse(
        String token,
        Long id,
        String nombre,
        String email,
        String rol
) {}
