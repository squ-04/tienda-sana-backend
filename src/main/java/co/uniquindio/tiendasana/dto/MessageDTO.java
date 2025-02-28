package co.uniquindio.tiendasana.dto;

public record MessageDTO<T>(
        boolean error,

        T reply
) {
}
