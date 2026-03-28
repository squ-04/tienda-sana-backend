package co.uniquindio.tiendasana.repos;

import co.uniquindio.tiendasana.model.documents.GestorReservas;
import co.uniquindio.tiendasana.model.documents.Mesa;
import co.uniquindio.tiendasana.model.mongo.GestorReservaDocument;
import co.uniquindio.tiendasana.repos.mongo.GestorReservaDocumentRepository;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Repository
public class GestorReservasRepo {

    private final GestorReservaDocumentRepository mongo;

    public GestorReservasRepo(GestorReservaDocumentRepository mongo) {
        this.mongo = mongo;
    }

    public List<GestorReservas> obtenerGetoresReserva() {
        return mongo.findAll().stream().map(this::toGestor).collect(Collectors.toList());
    }

    public int contarGestoresReservaExistintes() {
        return (int) mongo.count();
    }

    public int contarMesasEnHojaGestor() {
        return mongo.findAll().stream()
                .mapToInt(d -> d.getMesas() != null ? d.getMesas().size() : 0)
                .sum();
    }

    public GestorReservas mapearGestorReservas(List<Object> row) {
        if (row == null || row.size() < 3) {
            System.err.println("Fila de GestorReservas inválida o muy corta: " + row);
            return null;
        }
        String id = row.get(0).toString();
        if (id.equals("-")) {
            return null;
        }

        String fechaStr = row.get(1).toString();
        LocalDateTime fecha = null;
        if (!fechaStr.equals("-") && !fechaStr.isEmpty()) {
            try {
                fecha = LocalDateTime.parse(fechaStr);
            } catch (DateTimeParseException e) {
                System.err.println("Error parseando fecha para GestorReservas ID " + id + ": " + fechaStr);
            }
        }
        String emailUsuario = row.get(2).toString();

        return GestorReservas.builder()
                .id(id)
                .fecha(fecha)
                .emailUsuario(emailUsuario)
                .build();
    }

    public List<Object> mapearGestorReservasInverso(GestorReservas gestorReservas) {
        return Arrays.asList(
                gestorReservas.getId() != null ? gestorReservas.getId() : "-",
                gestorReservas.getFecha() != null ? gestorReservas.getFecha().toString() : "-",
                gestorReservas.getEmailUsuario() != null ? gestorReservas.getEmailUsuario() : "-"
        );
    }

    public List<Object> mapearMesaParaHojaGestorInverso(Mesa mesa, String idGestor) {
        return Arrays.asList(
                mesa.getNombre() != null ? mesa.getNombre() : "-",
                mesa.getEstado() != null ? mesa.getEstado() : "-",
                String.valueOf(mesa.getCapacidad()),
                mesa.getLocalidad() != null ? mesa.getLocalidad().getLocalidad() : "-",
                String.valueOf((int) mesa.getPrecioReserva()),
                mesa.getImagen() != null ? mesa.getImagen() : "-",
                mesa.getId() != null ? mesa.getId() : "-",
                mesa.getIdReserva() != null ? mesa.getIdReserva() : "-",
                idGestor != null ? idGestor : "-"
        );
    }

    public void guardarMesaEnHojaGestor(Mesa mesa, String idGestor) throws IOException {
        GestorReservaDocument doc = mongo.findById(idGestor)
                .orElseThrow(() -> new IOException("Gestor de reservas no encontrado: " + idGestor));
        if (mesa != null) {
            mesa.setIdGestorReserva(idGestor);
        }
        List<Mesa> mesas = doc.getMesas() != null ? new ArrayList<>(doc.getMesas()) : new ArrayList<>();
        if (mesa != null && mesa.getId() != null) {
            mesas.removeIf(m -> m != null && mesa.getId().equals(m.getId()));
            mesas.add(mesa);
        }
        doc.setMesas(mesas);
        mongo.save(doc);
    }

    public List<GestorReservas> filtrar(Predicate<GestorReservas> expresion) throws IOException {
        return obtenerGetoresReserva().stream().filter(expresion).collect(Collectors.toList());
    }

    public int obtenerIndiceGestor(String id) {
        if (id == null || id.isEmpty()) {
            return -1;
        }
        List<GestorReservas> gestoresReservas = obtenerGetoresReserva();
        for (int i = 0; i < gestoresReservas.size(); i++) {
            if (gestoresReservas.get(i) != null && id.equals(gestoresReservas.get(i).getId())) {
                return i;
            }
        }
        return -1;
    }

    public void actualizar(GestorReservas gestorReservas) throws IOException {
        if (!mongo.existsById(gestorReservas.getId())) {
            throw new IOException("Registro de GestorReservas no encontrado para ID: " + gestorReservas.getId());
        }
        GestorReservaDocument existing = mongo.findById(gestorReservas.getId()).orElseThrow();
        existing.setFecha(gestorReservas.getFecha());
        existing.setEmailUsuario(gestorReservas.getEmailUsuario());
        if (gestorReservas.getMesas() != null) {
            existing.setMesas(new ArrayList<>(gestorReservas.getMesas()));
        }
        mongo.save(existing);
    }

    public void guardar(GestorReservas gestorReservas) throws IOException {
        mongo.save(toDocument(gestorReservas));
    }

    public List<Object> mapearBorrado() {
        return Arrays.asList("-", "-", "-");
    }

    public Optional<GestorReservas> obtenerPorEmail(String email) throws IOException {
        if (email == null || email.isEmpty()) {
            return Optional.empty();
        }
        if (mongo.countByEmailUsuario(email) > 1) {
            System.err.println("Advertencia: Múltiples GestorReservas encontrados para el email: " + email);
        }
        return mongo.findFirstByEmailUsuarioOrderByFechaDesc(email).map(this::toGestor);
    }

    public void eliminarMesaDeHojaGestor(String idMesa, String idGestor) throws IOException {
        mongo.findById(idGestor).ifPresent(doc -> {
            if (doc.getMesas() != null) {
                doc.getMesas().removeIf(m -> m != null && idMesa.equals(m.getId()));
                mongo.save(doc);
            }
        });
    }

    private GestorReservas toGestor(GestorReservaDocument d) {
        return GestorReservas.builder()
                .id(d.getId())
                .fecha(d.getFecha())
                .emailUsuario(d.getEmailUsuario())
                .mesas(d.getMesas() != null ? new ArrayList<>(d.getMesas()) : new ArrayList<>())
                .build();
    }

    private GestorReservaDocument toDocument(GestorReservas g) {
        return GestorReservaDocument.builder()
                .id(g.getId())
                .fecha(g.getFecha())
                .emailUsuario(g.getEmailUsuario())
                .mesas(g.getMesas() != null ? new ArrayList<>(g.getMesas()) : new ArrayList<>())
                .build();
    }
}
