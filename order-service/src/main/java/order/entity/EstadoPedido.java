package order.entity;

public enum EstadoPedido {
        PENDIENTE,
        ASIGNADO,
        EN_TRANSITO,
        ENTREGADO,
        CANCELADO;

    /**
     * Devuelve el string que el frontend espera recibir leer 
     * Mapeo explícito para evitar discrepancias silenciosas.
     */
    public String toFrontendValue() {
        return switch (this) {
            case PENDIENTE -> "PENDIENTE";
            case ASIGNADO -> "ASIGNADO";
            case ENTREGADO -> "ENTREGADO";
            case EN_TRANSITO -> "EN_TRANSITO";
            case CANCELADO -> "CANCELADO";
        };
    }
}