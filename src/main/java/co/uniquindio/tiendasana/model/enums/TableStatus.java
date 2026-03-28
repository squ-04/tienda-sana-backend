package co.uniquindio.tiendasana.model.enums;

/**
 * Estado operativo de una mesa en el módulo administrativo (Mongo).
 * Estado operativo de mesa en el panel admin (independiente del flujo de reservas del cliente en Mongo).
 */
public enum TableStatus {
    AVAILABLE,
    RESERVED,
    OCCUPIED
}
