package co.uniquindio.tiendasana.model.enums;

import lombok.Getter;

@Getter
public enum Localidad {
    PASILLO ("Pasillo"),
    CENTRO ("Centro"),
    PATIO ("Patio"),
    SALON ("Salon");

    private final String localidad;

    /**
     * Metodo contructor de las categorias
     * @param categoria
     */
    Localidad(String categoria) {
        this.localidad = categoria;
    }

    /**
     * Metodo que dada una cadena retorna un valor del enum
     * @param localidad
     * @return
     */
    public static Localidad fromLocalidad(String localidad) {
        for (Localidad localidadMesa : Localidad.values()) {
            if (localidadMesa.getLocalidad().equalsIgnoreCase(localidad)) {
                return localidadMesa;
            }
        }
        throw new IllegalArgumentException("Locacalidad no valida: " + localidad);
    }
}
