package co.uniquindio.tiendasana.services.implementations;

import co.uniquindio.tiendasana.dto.MesaDTO;
import co.uniquindio.tiendasana.dto.gestorReservasdtos.BorrarMesaGestorDTO;
import co.uniquindio.tiendasana.model.documents.GestorReservas;
import co.uniquindio.tiendasana.model.documents.Mesa;
import co.uniquindio.tiendasana.model.enums.EstadoMesa;
import co.uniquindio.tiendasana.model.enums.Localidad; // Asegúrate de importar Localidad
import co.uniquindio.tiendasana.repos.GestorReservasRepo;
import co.uniquindio.tiendasana.repos.MesaRepo;
import co.uniquindio.tiendasana.services.interfaces.GestorReservasService;
// import co.uniquindio.tiendasana.services.interfaces.MesaService; // No parece usarse directamente aquí
import co.uniquindio.tiendasana.utils.GestorReservaConstantes;
import lombok.AllArgsConstructor; // Lombok se encarga del constructor
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Lazy; // Para dependencias circulares si fuera necesario


import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
// @AllArgsConstructor // Lombok generará el constructor con GestorReservasRepo y MesaRepo
public class GestorReservasServiceImp implements GestorReservasService {

    private final GestorReservasRepo gestorReservasRepo;
    private final MesaRepo mesaRepo; // Necesario para actualizar el estado en la hoja principal de mesas

    // Constructor para inyección de dependencias
    public GestorReservasServiceImp(GestorReservasRepo gestorReservasRepo, @Lazy MesaRepo mesaRepo) {
        this.gestorReservasRepo = gestorReservasRepo;
        this.mesaRepo = mesaRepo;
    }

    /**
     * Crea un gestor de reservas si no existe para el email dado, o devuelve el existente.
     * @param emailUsuario Email del usuario dueño del gestor de reservas.
     * @return El GestorReservas creado o existente.
     * @throws IOException Error al acceder a la base de datos.
     */
    @Override
    public GestorReservas crearGestorReservas(String emailUsuario) throws IOException {
        if (emailUsuario == null || emailUsuario.trim().isEmpty()) {
            throw new IllegalArgumentException("El email del usuario no puede ser nulo o vacío.");
        }
        Optional<GestorReservas> gestorReservasObtenido =
                gestorReservasRepo.obtenerPorEmail(emailUsuario);
        if (gestorReservasObtenido.isPresent()) {
            return gestorReservasObtenido.get();
        } else {
            GestorReservas gestorReservas = GestorReservas.builder()
                    .id(UUID.randomUUID().toString())
                    .emailUsuario(emailUsuario)
                    .fecha(LocalDateTime.now())
                    .mesas(new ArrayList<>()) // Inicializar la lista de mesas
                    .build();
            gestorReservasRepo.guardar(gestorReservas); // Guarda el Gestor en su propia hoja
            return gestorReservas;
        }
    }

    /**
     * "Borra" un gestor de reservas. Esto implica desvincular todas sus mesas en la hoja
     * "MesasAReservar" (marcando su idGestorReserva como "-") y luego, opcionalmente,
     * marcar el registro del GestorReservas como borrado en su propia hoja.
     * Las mesas en la hoja principal de mesas también se actualizan a DISPONIBLE.
     * @param emailUsuario Email del usuario dueño del gestor de reservas.
     * @return ID del gestor de reservas "borrado".
     * @throws IOException Error al acceder a la base de datos.
     */
    @Override
    public String borrarGestorReservas(String emailUsuario) throws IOException {
        GestorReservas gestorReservas = obtenerGestorReservas(emailUsuario);
        if (gestorReservas == null) {
            throw new IOException("No se encontró un gestor de reservas para el email: " + emailUsuario);
        }

        // Obtener mesas actualmente asociadas a este gestor desde la hoja "MesasAReservar"
        List<Mesa> mesasAsociadasAlGestor = mesaRepo.obtenerPorGestorReserva(gestorReservas.getId(), GestorReservaConstantes.HOJA_MESA);

        for (Mesa mesa : mesasAsociadasAlGestor) {
            // 1. Actualizar la mesa en la hoja "MesasAReservar" para desvincularla
            // Esto podría implicar marcarla como "borrada" o simplemente quitar el idGestorReserva
            // La implementación actual de gestorReservasRepo.eliminarMesaDeHojaGestor es un placeholder.
            // Una forma más directa sería tener un método para actualizar la fila de la mesa en SHEET_NAME_MESAS_GESTOR.
            // Por ahora, asumimos que la lógica de "liberar" la mesa en la hoja principal es suficiente.
            // y que al borrar el gestor, estas mesas ya no se considerarán vinculadas.

            // 2. Actualizar la mesa en la hoja principal de mesas (MesaRepo) a DISPONIBLE
            try {
                Optional<Mesa> mesaOptional = mesaRepo.obtenerMesaPorIdOriginal(mesa.getId()); // Necesitas un método para obtener por el ID original de la mesa
                Mesa mesaPrincipal = mesaOptional.orElse(null);
                if (mesaPrincipal != null) {
                    mesaPrincipal.setIdGestorReserva("-"); // Desvincular del gestor
                    mesaPrincipal.setEstado(EstadoMesa.DISPONIBLE); // Ponerla como disponible
                    // mesaPrincipal.setIdReserva("-"); // Si también estaba en una reserva activa, desvincularla
                    mesaRepo.actualizar(mesaPrincipal); // Actualiza en la hoja principal de mesas
                }
            } catch (Exception e) {
                System.err.println("Error al actualizar estado de mesa principal " + mesa.getId() + ": " + e.getMessage());
            }
        }

        // Opcional: Marcar el GestorReservas como borrado en su propia hoja
        // gestorReservasRepo.marcarComoBorrado(gestorReservas.getId()); // Necesitarías implementar este método

        return gestorReservas.getId();
    }

    /**
     * Obtiene el gestor de reservas de un usuario, si existe.
     * @param emailUsuario Email del usuario de la cuenta.
     * @return Gestor de reservas encontrado, o null si no existe.
     * @throws IOException Error al acceder a la base de datos.
     */
    @Override
    public GestorReservas obtenerGestorReservas(String emailUsuario) throws IOException {
        if (emailUsuario == null || emailUsuario.trim().isEmpty()) {
            return null;
        }
        Optional<GestorReservas> gestorReservas = gestorReservasRepo.obtenerPorEmail(emailUsuario);
        return gestorReservas.orElse(null);
    }

    /**
     * Agrega una mesa a la hoja de mesas de un gestor de reservas ("MesasAReservar").
     * También actualiza el estado de la mesa en la hoja principal de mesas a "OCUPADA"
     * y le asigna el idGestorReserva.
     * @param mesaDTO Datos de la mesa a agregar. El campo `idGestorReserva` del DTO debe contener el ID del Gestor.
     * @return Id de la mesa agregada.
     * @throws IOException Error al acceder a la base de datos.
     * @throws IllegalArgumentException si el idGestorReserva en mesaDTO es nulo o vacío.
     */
    @Override
    public String agregarMesaGestorReservas(MesaDTO mesaDTO) throws IOException {
        if (mesaDTO.idGestorReserva() == null || mesaDTO.idGestorReserva().trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del Gestor de Reservas es necesario para agregar una mesa.");
        }
        if (mesaDTO.id() == null || mesaDTO.id().trim().isEmpty()) {
            throw new IllegalArgumentException("El ID de la Mesa (Softr Record ID) es necesario.");
        }

        GestorReservas gestor = gestorReservasRepo.obtenerGetoresReserva().stream()
            .filter(g -> g != null && mesaDTO.idGestorReserva().equals(g.getId()))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Gestor de reservas no encontrado."));

        boolean mesaYaExiste = gestor.getMesas() != null && gestor.getMesas().stream()
            .anyMatch(m -> m != null && mesaDTO.id().equals(m.getId()));

        if (mesaYaExiste) {
            throw new IllegalStateException("Esta mesa ya fue agregada al gestor de reservas.");
        }

        System.out.println();
        // 1. Construir el objeto Mesa a partir del DTO.
        Mesa mesaParaGuardarEnHojaGestor = Mesa.builder()
                .id(mesaDTO.id()) // Este es el ID original de la mesa (Softr Record ID)
                .nombre(mesaDTO.nombre())
                .estado(EstadoMesa.DISPONIBLE) // Usar el estado que viene del DTO, podría ser "DISPONIBLE"
                .localidad(mesaDTO.localidad()) // El builder de Mesa debería manejar la conversión de String a Enum
                .precioReserva(mesaDTO.precioReserva())
                .capacidad(mesaDTO.capacidad())
                .imagen(mesaDTO.imagen())
                .idReserva(mesaDTO.idReserva() != null ? mesaDTO.idReserva() : "-") // Puede que aún no esté en una reserva
                .idGestorReserva(mesaDTO.idGestorReserva()) // El ID del gestor al que se vincula
                .build();

        // 2. Guardar la mesa en la hoja específica del gestor ("MesasAReservar")
        gestorReservasRepo.guardarMesaEnHojaGestor(mesaParaGuardarEnHojaGestor, mesaDTO.idGestorReserva());

        // 3. Actualizar la mesa en la hoja principal de Mesas (gestionada por MesaRepo)
        // para reflejar que ahora está asignada a un gestor y posiblemente ocupada.
        try {
            Optional<Mesa> mesaOptional = mesaRepo.obtenerMesaPorIdOriginal(mesaDTO.id());
            Mesa mesaPrincipal = mesaOptional.orElse(null);
            if (mesaPrincipal != null) {
                mesaPrincipal.setEstado(EstadoMesa.DISPONIBLE); // O el estado que indique que está en un "carrito de gestor"
                mesaPrincipal.setIdGestorReserva(mesaDTO.idGestorReserva());
                // mesaPrincipal.setIdReserva("-"); // Limpiar idReserva si solo está en el gestor y no en una reserva finalizada
                mesaRepo.actualizar(mesaPrincipal);
            } else {
                System.err.println("Advertencia: No se encontró la mesa principal con ID " + mesaDTO.id() + " para actualizar su estado.");
            }
        } catch (Exception e) {
            System.err.println("Error al actualizar la mesa principal con ID " + mesaDTO.id() + ": " + e.getMessage());
            // Considerar cómo manejar este error. ¿Debería deshacerse el guardado en la hoja del gestor?
        }

        return mesaParaGuardarEnHojaGestor.getId();
    }

    /**
     * Elimina una mesa específica del gestor de reservas del usuario.
     * Esto implica actualizar la mesa en la hoja "MesasAReservar" (marcarla como desvinculada)
     * y actualizar la mesa en la hoja principal a "DISPONIBLE".
     * @param mesaBorrarDTO Datos de la mesa a eliminar (emailUsuario, mesaId).
     * @return Id de la mesa "eliminada" del gestor.
     * @throws IOException Error al acceder a la base de datos.
     */
    @Override
    public String borrarMesaGestorReservas(BorrarMesaGestorDTO mesaBorrarDTO) throws IOException {
        GestorReservas gestorReservas = obtenerGestorReservas(mesaBorrarDTO.emailUsuario());
        if (gestorReservas == null) {
            throw new IOException("Gestor de reservas no encontrado para el usuario: " + mesaBorrarDTO.emailUsuario());
        }

        // Lógica para "eliminar" o desvincular la mesa de la hoja SHEET_NAME_MESAS_GESTOR
        // Esto es un placeholder, ya que el repo actual no tiene una forma directa de eliminar una fila específica
        // de la hoja de mesas del gestor por idMesa e idGestor.
        // La forma más robusta sería leer todas las mesas de ese gestor,
        // filtrar la que no se quiere, y reescribir la lista filtrada.
        // O, si GestorReservasRepo.eliminarMesaDeHojaGestor se implementa para marcar la fila:
        gestorReservasRepo.eliminarMesaDeHojaGestor(mesaBorrarDTO.mesaId(), gestorReservas.getId());
        Mesa mesa=mesaRepo.obtenerMesaPorIdOriginal(mesaBorrarDTO.mesaId()).get();
        mesa.setIdGestorReserva("-");
        mesaRepo.actualizar(mesa);
        System.out.println("Intentando eliminar (lógicamente) mesa " + mesaBorrarDTO.mesaId() + " de la hoja del gestor " + gestorReservas.getId());


        // Actualizar la mesa en la hoja principal (MesaRepo) para marcarla como DISPONIBLE
        try {
            Optional<Mesa> mesaOptional = mesaRepo.obtenerMesaPorIdOriginal(mesaBorrarDTO.mesaId());
            Mesa mesaPrincipal = mesaOptional.orElse(null);
            if (mesaPrincipal != null) {
                mesaPrincipal.setIdGestorReserva("-"); // Desvincular del gestor
                mesaPrincipal.setEstado(EstadoMesa.DISPONIBLE);
                // mesaPrincipal.setIdReserva("-"); // Si estaba asociada a una reserva, también desvincular
                mesaRepo.actualizar(mesaPrincipal);
            } else {
                System.err.println("Advertencia: No se encontró la mesa principal con ID " + mesaBorrarDTO.mesaId() + " para actualizar su estado a DISPONIBLE.");
            }
        } catch (Exception e) {
            System.err.println("Error al actualizar la mesa principal con ID " + mesaBorrarDTO.mesaId() + " a DISPONIBLE: " + e.getMessage());
            // Considerar el manejo de esta excepción.
        }

        return mesaBorrarDTO.mesaId();
    }

    /**
     * Lista todas las mesas dentro del gestor de reservas de un usuario.
     * Las mesas se obtienen de la hoja "MesasAReservar" filtradas por el idGestorReserva.
     * @param emailUsuario Email del usuario.
     * @return Lista de MesaDTO.
     * @throws IOException Error al acceder a la base de datos.
     */
    @Override
    public List<MesaDTO> obtenerMesasGestorReservas(String emailUsuario) throws IOException {
        GestorReservas gestorReservas = obtenerGestorReservas(emailUsuario);
        if (gestorReservas == null) {
            System.out.println("No se encontró gestor de reservas para: " + emailUsuario + ", devolviendo lista vacía de mesas.");
            return new ArrayList<>();
        }
        // La lógica de asignarMesas en el repo ya carga las mesas en el objeto gestorReservas
        // si la llamada a mesaRepo.obtenerPorGestorReserva está bien implementada.

        if (gestorReservas.getMesas() == null) {
            System.out.println("El gestor de reservas para " + emailUsuario + " no tiene una lista de mesas inicializada.");
            return new ArrayList<>();
        }

        List<MesaDTO> datosMesas = new ArrayList<>();
        for (Mesa mesa : gestorReservas.getMesas()) {
            if (mesa != null) { // Añadir chequeo de nulidad para cada mesa
                datosMesas.add(new MesaDTO(
                                mesa.getId(),
                                mesa.getNombre(),
                                mesa.getEstado(), // Devuelve el String del enum
                                mesa.getLocalidad() != null ? mesa.getLocalidad().getLocalidad() : "-", // Devuelve el String del enum
                                mesa.getPrecioReserva(),
                                mesa.getCapacidad(),
                                mesa.getImagen(),
                        mesa.getDuracionReservaMinutos(),
                                mesa.getIdReserva(),
                                mesa.getIdGestorReserva()
                        )
                );
            }
        }
        return datosMesas;
    }
}