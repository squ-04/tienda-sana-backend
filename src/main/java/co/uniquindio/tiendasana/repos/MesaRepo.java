package co.uniquindio.tiendasana.repos;

import co.uniquindio.tiendasana.dto.mesadtos.MesasTotalDTO;
import co.uniquindio.tiendasana.model.documents.Mesa;
import co.uniquindio.tiendasana.model.enums.EstadoMesa;
import co.uniquindio.tiendasana.model.enums.Localidad; // Asegúrate de importar Localidad
import co.uniquindio.tiendasana.utils.MesaConstantes;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Repository
public class MesaRepo {
    private final Sheets sheetsService;

    @Value("${google.sheets.spreadsheet-id}")
    private String spreadsheetId;

    // Hoja principal para la vista del cliente o una lista general de mesas disponibles para reservar.
    private final String SHEET_NAME_CLIENTE = MesaConstantes.HOJA_CLIENTE;
    // Hoja canónica donde se definen TODAS las mesas y se actualiza su estado maestro.
    private final String SHEET_NAME_PRINCIPAL = MesaConstantes.HOJA_PRINCIPAL;


    public MesaRepo(Sheets sheetsService) {
        this.sheetsService = sheetsService;
    }

    /**
     * Obtiene todas las mesas de la hoja principal (SHEET_NAME_PRINCIPAL).
     * Este método es interno y es llamado por otros que sí especifican la hoja.
     */
    private List<Mesa> obtenerTodasMesasDeHoja(String nombreHoja) throws IOException {
        // Lee todas las filas desde A2 hasta la última columna definida en MesaConstantes.COL_REGISTRO_FINAL
        String rango = nombreHoja + "!A2:" + MesaConstantes.COL_REGISTRO_FINAL;
        ValueRange respuesta = sheetsService.spreadsheets().values().get(spreadsheetId, rango).execute();
        List<List<Object>> valores = respuesta.getValues();
        return mapearFilasMesas(valores, nombreHoja); // Pasa el nombre de la hoja para logging/contexto
    }

    /**
     * Obtiene las mesas para la vista del cliente con paginación.
     * Lee de SHEET_NAME_CLIENTE.
     */
    public MesasTotalDTO obtenerMesasClientePaginado(int pagina, int cantidadElementos) throws IOException {
        int totalMesas = contarMesasExistentes(SHEET_NAME_CLIENTE, MesaConstantes.CANT_MESAS); // Contar de la hoja cliente
        List<List<Object>> filas = obtenerFilasPaginadasDeHoja(SHEET_NAME_CLIENTE, pagina, cantidadElementos, totalMesas);
        List<Mesa> mesas = mapearFilasMesas(filas, SHEET_NAME_CLIENTE);
        return new MesasTotalDTO(totalMesas, mesas);
    }

    /**
     * Cuenta las mesas existentes en una hoja específica.
     * @param nombreHoja El nombre de la hoja.
     * @param celdaContador La celda que contiene el total (ej. "!L3").
     * @return El total de mesas.
     * @throws IOException Si hay error de comunicación.
     */
    private int contarMesasExistentes(String nombreHoja, String celdaContador) throws IOException {
        // celdaContador ya viene con "!" (ej. "!L3") según MesaConstantes
        String rango = nombreHoja + celdaContador;
        ValueRange response = sheetsService.spreadsheets().values().get(spreadsheetId, rango).execute();
        List<List<Object>> values = response.getValues();
        if (values == null || values.isEmpty() || values.get(0).isEmpty() || values.get(0).get(0) == null) {
            System.err.println("Advertencia: Celda de contador de mesas (" + rango + ") está vacía o no es accesible. Asumiendo 0.");
            return 0;
        }
        try {
            return Integer.parseInt(values.get(0).get(0).toString());
        } catch (NumberFormatException e) {
            System.err.println("Error al parsear contador de mesas, valor: '" + values.get(0).get(0).toString() + "' en celda " + rango + ". Asumiendo 0.");
            return 0;
        }
    }

    /**
     * Obtiene filas paginadas de una hoja específica.
     */
    private List<List<Object>> obtenerFilasPaginadasDeHoja(String nombreHoja, int pagina, int cantidad, int cantidadTotal) throws IOException {
        if (cantidadTotal == 0) return new ArrayList<>();
        int cantidadPaginas = (int) Math.ceil((double) cantidadTotal / cantidad);

        if (pagina < 0 || pagina >= cantidadPaginas) {
            // En lugar de lanzar excepción, podríamos devolver una lista vacía o la última/primera página.
            // Por ahora, mantenemos la excepción para señalar un uso incorrecto de la paginación.
            System.err.println("Página solicitada (" + pagina + ") fuera de rango. Total páginas: " + cantidadPaginas);
            // throw new IllegalArgumentException("La página solicitada ("+ pagina +") no existe. Total páginas: " + cantidadPaginas);
            return new ArrayList<>(); // Devolver vacío si la página está fuera de rango
        }
        int filaInicio = 2 + (pagina * cantidad);
        int filaFin = filaInicio + cantidad - 1;

        // El rango debe usar COL_REGISTRO_FINAL de MesaConstantes, que debería ser "I" para 9 columnas
        String rango = nombreHoja + "!A" + filaInicio + ":" + MesaConstantes.COL_REGISTRO_FINAL + filaFin;
        ValueRange respuesta = sheetsService.spreadsheets().values().get(spreadsheetId, rango).execute();
        return (respuesta.getValues() != null) ? respuesta.getValues() : new ArrayList<>();
    }

    /**
     * Mapea una lista de filas (List<List<Object>>) a una lista de objetos Mesa.
     * @param filas Las filas obtenidas de Google Sheets.
     * @param contextoHoja Nombre de la hoja para logging.
     * @return Lista de objetos Mesa.
     */
    private List<Mesa> mapearFilasMesas(List<List<Object>> filas, String contextoHoja) {
        List<Mesa> mesas = new ArrayList<>();
        if (filas == null) return mesas;

        for (List<Object> row : filas) {
            if (row == null || row.isEmpty()) continue;
            try {
                Mesa mesa = mapearMesa(row, contextoHoja);
                if (mesa != null && mesa.getId() != null && !mesa.getId().equals("-")) { // Solo añadir mesas con ID válido
                    mesas.add(mesa);
                }
            } catch (Exception e) { // Captura más genérica para cualquier error de parseo en la fila
                System.err.println("❌ Error al procesar fila de Mesa desde hoja '" + contextoHoja + "': " + row + "\n" + e.getMessage());
                // e.printStackTrace(); // Descomentar para debug más detallado
            }
        }
        return mesas;
    }

    /**
     * Mapea una única fila (List<Object>) a un objeto Mesa.
     * Este método asume 9 columnas según la imagen:
     * A(0):Nombre, B(1):Estado, C(2):Capacidad, D(3):Localidad, E(4):precioReserva,
     * F(5):Imagen, G(6):Softr Record ID (ID Mesa), H(7):idReserva, I(8):idGestorReserva
     * @param row La fila de datos.
     * @param contextoHoja Nombre de la hoja para logging.
     * @return El objeto Mesa mapeado, o null si la fila es inválida (ej. sin ID).
     */
    public Mesa mapearMesa(List<Object> row, String contextoHoja) {
        if (row == null || row.size() < 7) { // Se necesitan al menos 7 columnas para el ID de la mesa.
            System.err.println("Fila de Mesa inválida o demasiado corta en hoja '" + contextoHoja + "': " + row);
            return null;
        }

        // Columna G (índice 6) es el ID de la Mesa (Softr Record ID)
        String id = (row.get(6) != null) ? row.get(6).toString() : null;
        if (id == null || id.trim().isEmpty() || id.equals("-")) {
            System.err.println("ID de Mesa (columna G) es nulo, vacío o '-' en hoja '" + contextoHoja + "'. Fila: " + row + ". Omitiendo mesa.");
            return null; // ID es fundamental
        }

        String nombre = (row.get(0) != null) ? row.get(0).toString() : "Mesa " + id.substring(0, Math.min(id.length(), 5)); // Nombre por defecto si falta

        EstadoMesa estado = EstadoMesa.DISPONIBLE; // Valor por defecto
        if (row.size() > 1 && row.get(1) != null && !row.get(1).toString().isEmpty() && !row.get(1).toString().equals("-")) {
            try {
                estado = EstadoMesa.fromEstado(row.get(1).toString());
            } catch (IllegalArgumentException e) {
                System.err.println("Advertencia: Estado de mesa inválido '" + row.get(1).toString() + "' para mesa ID " + id + " en hoja '" + contextoHoja + "'. Usando DISPONIBLE.");
            }
        }

        int capacidad = (row.size() > 2 && row.get(2) != null && row.get(2).toString().matches("\\d+")) ? Integer.parseInt(row.get(2).toString()) : 0;

        String localidadStr = (row.size() > 3 && row.get(3) != null) ? row.get(3).toString() : "";
        // La conversión a Enum Localidad se hace en el builder de Mesa

        float precioReserva = (row.size() > 4 && row.get(4) != null && row.get(4).toString().matches("-?\\d*\\.?\\d+")) ? Float.parseFloat(row.get(4).toString()) : 0.0f;
        String imagen = (row.size() > 5 && row.get(5) != null) ? row.get(5).toString() : "";

        String idReserva = (row.size() > 7 && row.get(7) != null) ? row.get(7).toString() : "-";
        String idGestorReserva = (row.size() > 8 && row.get(8) != null) ? row.get(8).toString() : "-";

        return Mesa.builder()
                .id(id)
                .nombre(nombre)
                .estado(estado) // Pasa el Enum EstadoMesa
                .localidad(localidadStr) // Pasa el String, el constructor/builder de Mesa maneja la conversión a Enum Localidad
                .precioReserva(precioReserva)
                .capacidad(capacidad)
                .imagen(imagen)
                .idReserva(idReserva)
                .idGestorReserva(idGestorReserva)
                .build();
    }


    /**
     * Mapea un objeto Mesa a una List<Object> para escribir en Google Sheets.
     * El orden debe coincidir con las 9 columnas esperadas.
     */
    public List<Object> mapearMesaInverso(Mesa mesa) {
        return Arrays.asList(
                mesa.getNombre() != null ? mesa.getNombre() : "-",                         // A: Nombre
                mesa.getEstado() != null ? mesa.getEstado() : "-",                         // B: Estado (como String del Enum)
                String.valueOf(mesa.getCapacidad()),                                       // C: Capacidad
                mesa.getLocalidad() != null ? mesa.getLocalidad().getLocalidad() : "-",     // D: Localidad (como String del Enum)
                String.valueOf((int) mesa.getPrecioReserva()),                             // E: precioReserva
                mesa.getImagen() != null ? mesa.getImagen() : "-",                         // F: Imagen referencial
                mesa.getId() != null ? mesa.getId() : "-",                                 // G: Softr Record ID (ID Mesa)
                mesa.getIdReserva() != null ? mesa.getIdReserva() : "-",                   // H: idReserva
                mesa.getIdGestorReserva() != null ? mesa.getIdGestorReserva() : "-"        // I: idGestorReserva
        );
    }

    /**
     * Obtiene mesas de una hoja específica y las filtra.
     * @param expresion Predicado para filtrar.
     * @param nombreHoja Nombre de la hoja de la cual leer.
     * @return Lista de mesas filtradas.
     * @throws IOException Si hay error.
     */
    public List<Mesa> filtrarMesasDeHoja(Predicate<Mesa> expresion, String nombreHoja) throws IOException {
        List<Mesa> mesas = obtenerTodasMesasDeHoja(nombreHoja);
        return mesas.stream()
                .filter(expresion)
                .collect(Collectors.toList());
    }

    /**
     * Método específico para obtener mesas por idGestorReserva desde una hoja específica.
     * Usado por GestorReservasService.
     * @param idGestor El ID del gestor de reservas.
     * @param nombreHojaMesasGestor El nombre de la hoja donde se almacenan las mesas del gestor (ej. "MesasAReservar").
     * @return Lista de mesas asociadas al gestor.
     * @throws IOException Si hay error.
     */
    public List<Mesa> obtenerPorGestorReserva(String idGestor, String nombreHojaMesasGestor) throws IOException {
        if (idGestor == null || idGestor.isEmpty()) {
            return new ArrayList<>();
        }
        // Lee todas las mesas de la hoja especificada ("MesasAReservar")
        List<Mesa> todasLasMesasDelGestor = obtenerTodasMesasDeHoja(nombreHojaMesasGestor);

        // Filtra por idGestorReserva
        return todasLasMesasDelGestor.stream()
                .filter(mesa -> mesa != null && idGestor.equals(mesa.getIdGestorReserva()))
                .collect(Collectors.toList());
    }


    /**
     * Encuentra el índice de una mesa en la hoja principal (SHEET_NAME_PRINCIPAL) por su ID.
     * @param idMesa El ID de la mesa a buscar.
     * @return El índice de la fila (0-based desde la primera fila de datos) o -1 si no se encuentra.
     */
    public int obtenerIndiceMesaEnHojaPrincipal(String idMesa) {
        if (idMesa == null || idMesa.isEmpty()) return -1;
        List<Mesa> mesas = null;
        try {
            // Siempre busca en la hoja principal para obtener el índice para actualizar
            mesas = obtenerTodasMesasDeHoja(SHEET_NAME_PRINCIPAL);
        } catch (IOException e) {
            System.err.println("Error al obtener mesas de la hoja principal para encontrar índice de mesa ID " + idMesa + ": " + e.getMessage());
            return -1;
        }
        if (mesas == null) return -1;

        for (int i = 0; i < mesas.size(); i++) {
            if (mesas.get(i) != null && idMesa.equals(mesas.get(i).getId())) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Actualiza una mesa en la hoja principal (SHEET_NAME_PRINCIPAL).
     * @param mesa El objeto Mesa con los datos actualizados.
     * @throws IOException Si la mesa no se encuentra o hay un error de comunicación.
     */
    public void actualizar(Mesa mesa) throws IOException {
        if (mesa == null || mesa.getId() == null || mesa.getId().isEmpty()) {
            throw new IllegalArgumentException("La mesa o su ID no pueden ser nulos para actualizar.");
        }
        int indice = obtenerIndiceMesaEnHojaPrincipal(mesa.getId());
        if (indice != -1) {
            // MesaConstantes.COL_REGISTRO_FINAL debe ser "I" para 9 columnas
            String range = SHEET_NAME_PRINCIPAL + "!A" + (2 + indice) + ":" + MesaConstantes.COL_REGISTRO_FINAL + (2 + indice);
            List<List<Object>> values = Arrays.asList(
                    mapearMesaInverso(mesa) // Este mapeador debe generar 9 columnas
            );

            ValueRange body = new ValueRange().setValues(values);
            UpdateValuesResponse result = sheetsService.spreadsheets().values()
                    .update(spreadsheetId, range, body)
                    .setValueInputOption("RAW")
                    .execute();
            System.out.println("Mesa ID " + mesa.getId() + " actualizada en hoja principal. Celdas: " + result.getUpdatedCells());
        } else {
            throw new IOException("Registro de mesa no encontrado en hoja principal para ID: " + mesa.getId() + " para actualizar.");
        }
    }

    /**
     * Obtiene una mesa por su ID original desde la hoja principal (SHEET_NAME_PRINCIPAL).
     * Este es el método que debe usar GestorReservasServiceImp.obtenerMesaPorIdOriginal.
     * @param idMesa El ID de la mesa.
     * @return Optional<Mesa>.
     * @throws IOException Si hay error.
     */
    public Optional<Mesa> obtenerMesaPorIdOriginal(String idMesa) throws IOException {
        if (idMesa == null || idMesa.isEmpty()) {
            return Optional.empty();
        }
        List<Mesa> mesasFiltradas = filtrarMesasDeHoja(mesa -> mesa != null && idMesa.equals(mesa.getId()), SHEET_NAME_PRINCIPAL);

        if (mesasFiltradas.isEmpty()) {
            return Optional.empty();
        }
        if (mesasFiltradas.size() > 1) {
            System.err.println("Advertencia: Múltiples mesas encontradas en hoja principal con el mismo ID: " + idMesa);
            // Devolver la primera o manejar el error según la lógica de negocio
        }
        return Optional.of(mesasFiltradas.get(0));
    }

    /**
     * Método de conveniencia para obtener una mesa por ID desde la hoja de cliente.
     * Usado por MesaServiceImp.obtenerMesa(id).
     * @param id El ID de la mesa.
     * @return Optional<Mesa>.
     * @throws IOException Si hay error.
     */
    public Optional<Mesa> obtenerPorIdDesdeHojaCliente(String id) throws IOException {
        if (id == null || id.isEmpty()) {
            return Optional.empty();
        }
        // Usa SHEET_NAME_CLIENTE que es MesaConstantes.HOJA_CLIENTE
        List<Mesa> mesasObtenidas = filtrarMesasDeHoja(mesa -> mesa != null && id.equals(mesa.getId()), SHEET_NAME_CLIENTE);

        if (mesasObtenidas.isEmpty()) {
            return Optional.empty();
        }
        if (mesasObtenidas.size() > 1) {
            System.err.println("Advertencia: Múltiples mesas encontradas en hoja cliente con el mismo ID: " + id);
        }
        return Optional.of(mesasObtenidas.get(0));
    }
}