package auth.entity;

/**
 * Enum de roles del sistema.
 * Los valores DEBEN coincidir exactamente con los strings usados en el frontend (constants/roles.js):
 *   ADMINISTRADOR    → "Administrador"
 *   OPERADOR_LOGISTICO → "OperadorLogistico"
 *   REPARTIDOR       → "Repartidor"
 *   CLIENTE          → "Cliente"
 */
public enum Rol {
    ADMINISTRADOR,
    OPERADOR_LOGISTICO,
    REPARTIDOR,
    CLIENTE;

    /**
     * Devuelve el string que el frontend espera recibir en el JWT.
     * Mapeo explícito para evitar discrepancias silenciosas.
     */
    public String toFrontendValue() {
        return switch (this) {
            case ADMINISTRADOR -> "ADMINISTRADOR";
            case OPERADOR_LOGISTICO -> "OPERADOR_LOGISTICO";
            case REPARTIDOR -> "REPARTIDOR";
            case CLIENTE -> "CLIENTE";
        };
    }
}
