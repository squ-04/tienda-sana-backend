package co.uniquindio.tiendasana.repos;

import co.uniquindio.tiendasana.dto.mesadtos.MesasTotalDTO;
import co.uniquindio.tiendasana.model.documents.Mesa;
import co.uniquindio.tiendasana.model.enums.EstadoMesa;
import co.uniquindio.tiendasana.model.enums.Localidad;
import co.uniquindio.tiendasana.model.mongo.TableDocument;
import co.uniquindio.tiendasana.repos.mongo.TableDocumentRepository;
import co.uniquindio.tiendasana.repos.mongo.GestorReservaDocumentRepository;
import co.uniquindio.tiendasana.utils.MesaConstantes;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Repository
public class MesaRepo {

    private final TableDocumentRepository tableMongo;
    private final GestorReservaDocumentRepository gestorMongo;

    public MesaRepo(TableDocumentRepository tableMongo, GestorReservaDocumentRepository gestorMongo) {
        this.tableMongo = tableMongo;
        this.gestorMongo = gestorMongo;
    }

    private List<Mesa> obtenerTodasMesasDeHoja(String nombreHoja) throws IOException {
        if (MesaConstantes.HOJA_CLIENTE.equals(nombreHoja)) {
            return tableMongo.findByVisibleToClientTrueOrderByNombreAsc().stream()
                    .map(this::toMesa)
                    .collect(Collectors.toList());
        }
        if (MesaConstantes.HOJA_PRINCIPAL.equals(nombreHoja)) {
            return tableMongo.findAll().stream()
                    .map(this::toMesa)
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    public MesasTotalDTO obtenerMesasClientePaginado(int pagina, int cantidadElementos) throws IOException {
        List<TableDocument> all = tableMongo.findByVisibleToClientTrueOrderByNombreAsc();
        int totalMesas = all.size();
        if (totalMesas == 0) {
            return new MesasTotalDTO(0, new ArrayList<>());
        }
        int cantidadPaginas = (int) Math.ceil((double) totalMesas / cantidadElementos);
        if (pagina < 0 || pagina >= cantidadPaginas) {
            System.err.println("Página solicitada (" + pagina + ") fuera de rango. Total páginas: " + cantidadPaginas);
            return new MesasTotalDTO(totalMesas, new ArrayList<>());
        }
        int from = pagina * cantidadElementos;
        int to = Math.min(from + cantidadElementos, totalMesas);
        List<Mesa> slice = all.subList(from, to).stream().map(this::toMesa).collect(Collectors.toList());
        return new MesasTotalDTO(totalMesas, slice);
    }

    public Mesa mapearMesa(List<Object> row, String contextoHoja) {
        if (row == null || row.size() < 7) {
            System.err.println("Fila de Mesa inválida o demasiado corta en hoja '" + contextoHoja + "': " + row);
            return null;
        }
        String id = (row.get(6) != null) ? row.get(6).toString() : null;
        if (id == null || id.trim().isEmpty() || id.equals("-")) {
            System.err.println("ID de Mesa (columna G) es nulo, vacío o '-' en hoja '" + contextoHoja + "'. Fila: " + row + ". Omitiendo mesa.");
            return null;
        }
        String nombre = (row.get(0) != null) ? row.get(0).toString() : "Mesa " + id.substring(0, Math.min(id.length(), 5));
        EstadoMesa estado = EstadoMesa.DISPONIBLE;
        if (row.size() > 1 && row.get(1) != null && !row.get(1).toString().isEmpty() && !row.get(1).toString().equals("-")) {
            try {
                estado = EstadoMesa.fromEstado(row.get(1).toString());
            } catch (IllegalArgumentException e) {
                System.err.println("Advertencia: Estado de mesa inválido '" + row.get(1).toString() + "' para mesa ID " + id + " en hoja '" + contextoHoja + "'. Usando DISPONIBLE.");
            }
        }
        int capacidad = (row.size() > 2 && row.get(2) != null && row.get(2).toString().matches("\\d+")) ? Integer.parseInt(row.get(2).toString()) : 0;
        String localidadStr = (row.size() > 3 && row.get(3) != null) ? row.get(3).toString() : "";
        float precioReserva = (row.size() > 4 && row.get(4) != null && row.get(4).toString().matches("-?\\d*\\.?\\d+")) ? Float.parseFloat(row.get(4).toString()) : 0.0f;
        String imagen = (row.size() > 5 && row.get(5) != null) ? row.get(5).toString() : "";
        String idReserva = (row.size() > 7 && row.get(7) != null) ? row.get(7).toString() : "-";
        String idGestorReserva = (row.size() > 8 && row.get(8) != null) ? row.get(8).toString() : "-";
        return Mesa.builder()
                .id(id)
                .nombre(nombre)
                .estado(estado)
                .localidad(localidadStr)
                .precioReserva(precioReserva)
                .capacidad(capacidad)
                .imagen(imagen)
                .idReserva(idReserva)
                .idGestorReserva(idGestorReserva)
                .build();
    }

    public List<Object> mapearMesaInverso(Mesa mesa) {
        return List.of(
                mesa.getNombre() != null ? mesa.getNombre() : "-",
                mesa.getEstado() != null ? mesa.getEstado() : "-",
                String.valueOf(mesa.getCapacidad()),
                mesa.getLocalidad() != null ? mesa.getLocalidad().getLocalidad() : "-",
                String.valueOf((int) mesa.getPrecioReserva()),
                mesa.getImagen() != null ? mesa.getImagen() : "-",
                mesa.getId() != null ? mesa.getId() : "-",
                mesa.getIdReserva() != null ? mesa.getIdReserva() : "-",
                mesa.getIdGestorReserva() != null ? mesa.getIdGestorReserva() : "-"
        );
    }

    public List<Mesa> filtrarMesasDeHoja(Predicate<Mesa> expresion, String nombreHoja) throws IOException {
        return obtenerTodasMesasDeHoja(nombreHoja).stream()
                .filter(expresion)
                .collect(Collectors.toList());
    }

    public List<Mesa> obtenerPorGestorReserva(String idGestor, String nombreHojaMesasGestor) throws IOException {
        if (idGestor == null || idGestor.isEmpty()) {
            return new ArrayList<>();
        }
        return gestorMongo.findById(idGestor)
                .map(d -> d.getMesas() != null ? new ArrayList<>(d.getMesas()) : new ArrayList<Mesa>())
                .orElseGet(() -> new ArrayList<Mesa>());
    }

    public int obtenerIndiceMesaEnHojaPrincipal(String idMesa) {
        if (idMesa == null || idMesa.isEmpty()) {
            return -1;
        }
        List<TableDocument> all = tableMongo.findAll();
        for (int i = 0; i < all.size(); i++) {
            if (idMesa.equals(all.get(i).getId())) {
                return i;
            }
        }
        return -1;
    }

    public void actualizar(Mesa mesa) throws IOException {
        if (mesa == null || mesa.getId() == null || mesa.getId().isEmpty()) {
            throw new IllegalArgumentException("La mesa o su ID no pueden ser nulos para actualizar.");
        }
        if (!tableMongo.existsById(mesa.getId())) {
            throw new IOException("Registro de mesa no encontrado en hoja principal para ID: " + mesa.getId() + " para actualizar.");
        }
        tableMongo.save(toTableDocument(mesa, tableMongo.findById(mesa.getId()).orElseThrow().isVisibleToClient()));
    }

    public Optional<Mesa> obtenerMesaPorIdOriginal(String idMesa) throws IOException {
        if (idMesa == null || idMesa.isEmpty()) {
            return Optional.empty();
        }
        return tableMongo.findById(idMesa).map(this::toMesa);
    }

    public Optional<Mesa> obtenerPorIdDesdeHojaCliente(String id) throws IOException {
        if (id == null || id.isEmpty()) {
            return Optional.empty();
        }
        return tableMongo.findById(id)
                .filter(TableDocument::isVisibleToClient)
                .map(this::toMesa);
    }

    private Mesa toMesa(TableDocument d) {
        EstadoMesa estado = EstadoMesa.DISPONIBLE;
        try {
            estado = EstadoMesa.fromEstado(d.getEstado());
        } catch (IllegalArgumentException ignored) {
        }
        String loc = d.getLocalidad();
        if (loc == null || loc.isBlank()) {
            loc = Localidad.CENTRO.getLocalidad();
        }
        return Mesa.builder()
                .id(d.getId())
                .nombre(d.getNombre())
                .estado(estado)
                .localidad(loc)
                .precioReserva((float) d.getPrecioReserva())
                .capacidad(d.getCapacidad())
                .imagen(d.getImagen())
            .duracionReservaMinutos(normalizeDuracionReservaMinutos(d.getDuracionReservaMinutos()))
                .idReserva("-")
                .idGestorReserva("-")
                .build();
    }

    private TableDocument toTableDocument(Mesa mesa, boolean visibleToClient) {
        return TableDocument.builder()
                .id(mesa.getId())
                .nombre(mesa.getNombre())
                .estado(mesa.getEstado())
                .localidad(mesa.getLocalidad() != null ? mesa.getLocalidad().getLocalidad() : "")
                .precioReserva(mesa.getPrecioReserva())
                .capacidad(mesa.getCapacidad())
                .imagen(mesa.getImagen())
                .duracionReservaMinutos(normalizeDuracionReservaMinutos(mesa.getDuracionReservaMinutos()))
                .visibleToClient(visibleToClient)
                .build();
    }

    private int normalizeDuracionReservaMinutos(Integer duracionReservaMinutos) {
        if (duracionReservaMinutos == null || duracionReservaMinutos <= 0) {
            return TableDocument.DEFAULT_DURACION_RESERVA_MINUTOS;
        }
        return duracionReservaMinutos;
    }

    private int normalizeDuracionReservaMinutos(int duracionReservaMinutos) {
        if (duracionReservaMinutos <= 0) {
            return TableDocument.DEFAULT_DURACION_RESERVA_MINUTOS;
        }
        return duracionReservaMinutos;
    }
}
