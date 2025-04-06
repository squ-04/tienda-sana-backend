package co.uniquindio.tiendasana.dto.jwtdtos;

public record MessageDTO<T>(
        boolean error,

        T reply
) {
}
