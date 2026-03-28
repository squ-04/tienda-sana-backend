package co.uniquindio.tiendasana.repos;

import co.uniquindio.tiendasana.exceptions.ProductoParseException;
import co.uniquindio.tiendasana.model.documents.VentaProducto;
import co.uniquindio.tiendasana.model.mongo.VentaProductoDocument;
import co.uniquindio.tiendasana.model.vo.DetalleVentaProducto;
import co.uniquindio.tiendasana.model.vo.Pago;
import co.uniquindio.tiendasana.repos.mongo.VentaProductoDocumentRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Repository
public class VentaProductoRepo {

    private final VentaProductoDocumentRepository mongo;

    public VentaProductoRepo(VentaProductoDocumentRepository mongo) {
        this.mongo = mongo;
    }

    public List<VentaProducto> obtenerVentas() throws IOException {
        List<VentaProducto> ventas = obtenerVentasSimples();
        asignarDetalles(ventas);
        return ventas;
    }

    public void asignarDetalles(List<VentaProducto> ventas) {
        for (VentaProducto v : ventas) {
            mongo.findById(v.getId()).ifPresent(doc -> {
                List<DetalleVentaProducto> p = doc.getProductos();
                v.setProductos(p != null ? new ArrayList<>(p) : new ArrayList<>());
            });
        }
    }

    public List<VentaProducto> filtrar(Predicate<VentaProducto> expresion) throws IOException, ProductoParseException {
        return obtenerVentas().stream().filter(expresion).collect(Collectors.toList());
    }

    public List<VentaProducto> obtenerVentasSimples() {
        return mongo.findAll(Sort.by("fecha").ascending()).stream()
                .map(this::toVentaSimple)
                .collect(Collectors.toList());
    }

    public VentaProducto mapearVenta(List<Object> row) {
        String id = row.get(0).toString();
        String emailUsuario = row.get(1).toString();
        LocalDateTime fecha = LocalDateTime.parse(row.get(2).toString());
        String totalString = row.get(3).toString();
        float total = totalString.matches("\\d+(\\.\\d+)?") ? Float.parseFloat(totalString) : 0.0f;
        String promocionId = row.get(4).toString();
        String codigoPasarela = row.get(5).toString();

        Pago pago = null;
        if (row.size() > 6 && row.get(6) != null) {
            pago = Pago.builder()
                    .id(row.get(6) != null ? row.get(6).toString() : "-")
                    .currency(row.size() > 7 && row.get(7) != null ? row.get(7).toString() : "-")
                    .paymentType(row.size() > 8 && row.get(8) != null ? row.get(8).toString() : "-")
                    .statusDetail(row.size() > 9 && row.get(9) != null ? row.get(9).toString() : "-")
                    .authorizationCode(row.size() > 10 && row.get(10) != null ? row.get(10).toString() : "-")
                    .date(row.size() > 11 && !row.get(11).toString().equals("-") ? LocalDateTime.parse(row.get(11).toString()) : null)
                    .transactionValue(row.size() > 12 && !row.get(12).toString().equals("-") ? Float.parseFloat(row.get(12).toString()) : 0.0f)
                    .status(row.size() > 13 && row.get(13) != null ? row.get(13).toString() : "-")
                    .build();
        }

        return VentaProducto.builder()
                .id(id)
                .emailUsario(emailUsuario)
                .fecha(fecha)
                .total(total)
                .promocionId(promocionId)
                .codigoPasarela(codigoPasarela)
                .pago(pago)
                .build();
    }

    public List<Object> mapearVentaInverso(VentaProducto venta) {
        Pago pago = venta.getPago();
        return Arrays.asList(
                venta.getId(),
                venta.getEmailUsario(),
                venta.getFecha() != null ? venta.getFecha().toString() : "-",
                "" + venta.getTotal(),
                venta.getPromocionId(),
                venta.getCodigoPasarela(),
                pago != null ? pago.getId() : "-",
                pago != null ? pago.getCurrency() : "-",
                pago != null ? pago.getPaymentType() : "-",
                pago != null ? pago.getStatusDetail() : "-",
                pago != null ? pago.getAuthorizationCode() : "-",
                pago != null && pago.getDate() != null ? pago.getDate().toString() : "-",
                pago != null ? "" + pago.getTransactionValue() : "0",
                pago != null ? pago.getStatus() : "-"
        );
    }

    public List<VentaProducto> filtrarVentasSimple(Predicate<VentaProducto> expresion) throws IOException, ProductoParseException {
        List<VentaProducto> ventas = obtenerVentasSimples();
        List<VentaProducto> filtradas = ventas.stream().filter(expresion).collect(Collectors.toList());
        asignarDetalles(filtradas);
        return filtradas;
    }

    public int contarVentasExistentes() {
        return (int) mongo.count();
    }

    public VentaProducto guardarVentaProductoSimple(VentaProducto venta) throws IOException {
        Optional<VentaProductoDocument> opt = mongo.findById(venta.getId());
        if (opt.isPresent()) {
            VentaProductoDocument existing = opt.get();
            existing.setEmailUsuario(venta.getEmailUsario());
            existing.setFecha(venta.getFecha());
            existing.setTotal(venta.getTotal());
            existing.setPromocionId(venta.getPromocionId());
            existing.setCodigoPasarela(venta.getCodigoPasarela());
            existing.setPago(venta.getPago());
            mongo.save(existing);
        } else {
            mongo.save(toDocument(venta, false));
        }
        return venta;
    }

    public VentaProducto guardarVentaProducto(VentaProducto venta) throws IOException {
        mongo.save(toDocument(venta, true));
        return venta;
    }

    public int obtenerIndiceVenta(String id) {
        List<VentaProducto> ventas = obtenerVentasSimples();
        for (int i = 0; i < ventas.size(); i++) {
            if (ventas.get(i).getId().equals(id)) {
                return i;
            }
        }
        return -1;
    }

    public void actualizarVentaSimple(VentaProducto venta) throws IOException {
        if (!mongo.existsById(venta.getId())) {
            throw new IOException("Registro no encontrado");
        }
        VentaProductoDocument existing = mongo.findById(venta.getId()).orElseThrow(() -> new IOException("Registro no encontrado"));
        existing.setEmailUsuario(venta.getEmailUsario());
        existing.setFecha(venta.getFecha());
        existing.setTotal(venta.getTotal());
        existing.setPromocionId(venta.getPromocionId());
        existing.setCodigoPasarela(venta.getCodigoPasarela());
        existing.setPago(venta.getPago());
        mongo.save(existing);
    }

    public void actualizarVenta(VentaProducto venta) throws IOException {
        if (!mongo.existsById(venta.getId())) {
            throw new IOException("Registro no encontrado");
        }
        mongo.save(toDocument(venta, true));
    }

    public List<DetalleVentaProducto> obtenerDetallesVenta() throws IOException {
        return mongo.findAll().stream()
                .flatMap(d -> d.getProductos() != null ? d.getProductos().stream() : java.util.stream.Stream.empty())
                .collect(Collectors.toList());
    }

    public DetalleVentaProducto mapearDetalleVenta(List<Object> row) {
        String productoId = row.get(0).toString();
        int cantidad = Integer.parseInt(row.get(1).toString());
        float valor = Float.parseFloat(row.get(2).toString());
        String idVenta = row.get(3).toString();
        return DetalleVentaProducto.builder()
                .productoId(productoId)
                .cantidad(cantidad)
                .valor(valor)
                .ventaId(idVenta)
                .build();
    }

    public List<Object> mapearDetalleVentaInverso(DetalleVentaProducto detalle) {
        return Arrays.asList(
                detalle.getProductoId(),
                "" + detalle.getCantidad(),
                "" + (int) detalle.getValor(),
                detalle.getVentaId()
        );
    }

    public List<DetalleVentaProducto> filtrarDetalles(Predicate<DetalleVentaProducto> expresion) throws IOException {
        return obtenerDetallesVenta().stream().filter(expresion).collect(Collectors.toList());
    }

    public int contarDetallesExistintes() {
        try {
            return obtenerDetallesVenta().size();
        } catch (IOException e) {
            return 0;
        }
    }

    public void guardarDetalle(DetalleVentaProducto detalle) throws IOException {
        VentaProductoDocument doc = mongo.findById(detalle.getVentaId())
                .orElseThrow(() -> new IOException("Venta no encontrada"));
        List<DetalleVentaProducto> list = doc.getProductos() != null ? new ArrayList<>(doc.getProductos()) : new ArrayList<>();
        list.removeIf(d -> d.getProductoId().equals(detalle.getProductoId()) && d.getVentaId().equals(detalle.getVentaId()));
        list.add(detalle);
        doc.setProductos(list);
        mongo.save(doc);
    }

    public int obtenerIndiceDetalle(String idVenta, String productoId) {
        VentaProductoDocument doc = mongo.findById(idVenta).orElse(null);
        if (doc == null || doc.getProductos() == null) {
            return -1;
        }
        List<DetalleVentaProducto> detalles = doc.getProductos();
        for (int i = 0; i < detalles.size(); i++) {
            if (detalles.get(i).getProductoId().equals(productoId)
                    && detalles.get(i).getVentaId().equals(idVenta)) {
                return i;
            }
        }
        return -1;
    }

    public void actualizarDetalle(DetalleVentaProducto detalle) throws IOException {
        if (obtenerIndiceDetalle(detalle.getVentaId(), detalle.getProductoId()) < 0) {
            throw new IOException("Registro no encontrado");
        }
        guardarDetalle(detalle);
    }

    public List<Object> mapearBorrado() {
        return Arrays.asList("-", "0", "0", "-");
    }

    public void eliminarDetalle(DetalleVentaProducto detalle) throws IOException {
        VentaProductoDocument doc = mongo.findById(detalle.getVentaId())
                .orElseThrow(() -> new IOException("Registro no encontrado"));
        if (doc.getProductos() == null) {
            throw new IOException("Registro no encontrado");
        }
        boolean removed = doc.getProductos().removeIf(d ->
                d.getProductoId().equals(detalle.getProductoId()) && d.getVentaId().equals(detalle.getVentaId()));
        if (!removed) {
            throw new IOException("Registro no encontrado");
        }
        mongo.save(doc);
    }

    private VentaProducto toVentaSimple(VentaProductoDocument d) {
        return VentaProducto.builder()
                .id(d.getId())
                .emailUsario(d.getEmailUsuario())
                .fecha(d.getFecha())
                .total((float) d.getTotal())
                .promocionId(d.getPromocionId())
                .codigoPasarela(d.getCodigoPasarela())
                .pago(d.getPago())
                .productos(null)
                .build();
    }

    private VentaProductoDocument toDocument(VentaProducto v, boolean includeLineItems) {
        List<DetalleVentaProducto> productos = includeLineItems && v.getProductos() != null
                ? new ArrayList<>(v.getProductos())
                : new ArrayList<>();
        return VentaProductoDocument.builder()
                .id(v.getId())
                .emailUsuario(v.getEmailUsario())
                .fecha(v.getFecha())
                .total(v.getTotal())
                .promocionId(v.getPromocionId())
                .codigoPasarela(v.getCodigoPasarela())
                .pago(v.getPago())
                .productos(productos)
                .build();
    }
}
