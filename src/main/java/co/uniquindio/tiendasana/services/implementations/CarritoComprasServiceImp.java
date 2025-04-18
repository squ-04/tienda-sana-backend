package co.uniquindio.tiendasana.services.implementations;

import co.uniquindio.tiendasana.dto.carritoCompras.AgregarDetalleCarritoDTO;
import co.uniquindio.tiendasana.dto.carritoCompras.BorrarDetalleCarritoDTO;
import co.uniquindio.tiendasana.dto.carritoCompras.EditarDetalleCarritoDTO;
import co.uniquindio.tiendasana.dto.carritoCompras.VistaItemCarritoDTO;
import co.uniquindio.tiendasana.exceptions.ProductoParseException;
import co.uniquindio.tiendasana.model.documents.CarritoCompras;
import co.uniquindio.tiendasana.model.documents.Producto;
import co.uniquindio.tiendasana.model.vo.DetalleCarrito;
import co.uniquindio.tiendasana.repos.CarritoComprasRepo;
import co.uniquindio.tiendasana.services.interfaces.CarritoComprasService;
import co.uniquindio.tiendasana.services.interfaces.ProductoService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class CarritoComprasServiceImp implements CarritoComprasService {
    private final ProductoService productoService;
    private final CarritoComprasRepo carritoComprasRepo;

    @Override
    public void borrarCarritoCompras(String idUsuario) {

    }

    @Override
    public List<DetalleCarrito> getItems() {
        return List.of();
    }

    @Override
    public CarritoCompras getCarritoCompras(String idUsuario) {
        return null;
    }

    @Override
    public String agregarDetalleCarrito(AgregarDetalleCarritoDTO addShoppingCarDetailDTO) throws IOException, ProductoParseException {
        CarritoCompras carritoCompras = crearCarritoCompras(addShoppingCarDetailDTO.idUsuario());
        Producto producto = productoService.getProducto(addShoppingCarDetailDTO.idProducto());

        DetalleCarrito detalleCarrito = new DetalleCarrito();
        detalleCarrito.setCantidad(addShoppingCarDetailDTO.cantidad());
        detalleCarrito.setProductoId(addShoppingCarDetailDTO.idProducto());
        detalleCarrito.setIdCarrito(carritoCompras.getId());
        detalleCarrito.setSubtotal(producto.getPrecioUnitario()*addShoppingCarDetailDTO.cantidad());

        List<DetalleCarrito> detalles = carritoCompras.getProductos();
        detalles.add(detalleCarrito);
        carritoCompras.setProductos(detalles);
        carritoComprasRepo.actualizarCarrito(carritoCompras);
        return carritoCompras.getId();
    }

    @Override
    public String editarDetalleCarrito(EditarDetalleCarritoDTO editarCarritoDetalleDTO) throws Exception {
        CarritoCompras carritoCompras = obtenerCarritoCompra(editarCarritoDetalleDTO.idUsuario());
        List<DetalleCarrito> detalles = carritoCompras.getProductos();
        detalles.forEach(e -> {
            try {
                Producto producto=productoService.getProducto(editarCarritoDetalleDTO.idProducto());
                if (e.getProductoId().equals(editarCarritoDetalleDTO.idProducto())) {
                    if (!producto.estaStockDisponible(editarCarritoDetalleDTO.cantidad())) {
                        throw new Exception("Cantidad de stock insuficiente");
                    } else {
                        e.setCantidad(editarCarritoDetalleDTO.cantidad());
                        e.setSubtotal(e.getCantidad()*producto.getPrecioUnitario());
                    }

                }

            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }

        });
        carritoCompras.setProductos(detalles);
        System.out.println("Antes de actualizar carrito");
        for (DetalleCarrito detalle:carritoCompras.getProductos()) {
            System.out.println(detalle.toString());
        }
        carritoComprasRepo.actualizarCarrito(carritoCompras);
        return carritoCompras.getId();
    }

    private CarritoCompras obtenerCarritoCompra(String idUsuario) throws Exception {
        Optional<CarritoCompras> shoppingCar = carritoComprasRepo.obtenerPorIdUsuario(idUsuario);
        if (shoppingCar.isEmpty()) {
            throw new Exception("No hay un carrito de compras para el usuario: " + idUsuario);
        }
        return shoppingCar.get();
    }

    @Override
    public String borrarCarritoCompras(BorrarDetalleCarritoDTO borrarDetalleCarritoDTO) throws Exception {
        CarritoCompras carritoCompras = obtenerCarritoCompra(borrarDetalleCarritoDTO.idUsuario());
        List<DetalleCarrito> detalles = carritoCompras.getProductos();
        List<DetalleCarrito> detallesEliminar=
                detalles.stream().filter(e -> e.getProductoId().equals(borrarDetalleCarritoDTO.idProducto()) &&
                carritoCompras.getIdUsuario().equals(borrarDetalleCarritoDTO.idUsuario())).collect(Collectors.toList());
        detalles.removeAll(detallesEliminar);
        carritoCompras.setProductos(detalles);
        carritoComprasRepo.eliminarDetalles(detallesEliminar);
        carritoComprasRepo.actualizarCarrito(carritoCompras);
        return carritoCompras.getId();
    }

    @Override
    public List<VistaItemCarritoDTO> listarDetallesCarrito(String emailUsuario) throws IOException {
        CarritoCompras shoppingCar = crearCarritoCompras(emailUsuario);
        List<DetalleCarrito> shoppingCarDetails = shoppingCar.getProductos();

        return shoppingCarDetails.stream()
                .map(this::convertToCarItemViewDTO)
                .flatMap(Optional::stream) // Descartar valores nulos si la conversión falló
                .collect(Collectors.toList());
    }

    private Optional<VistaItemCarritoDTO> convertToCarItemViewDTO(DetalleCarrito itemView) {
        try {

            Producto event = productoService.getProducto(itemView.getProductoId());

            return Optional.of(new VistaItemCarritoDTO(
                    event.getId(),
                    event.getNombre(),
                    event.getCategoria(),
                    event.getPrecioUnitario(),
                    event.getCantidad(),
                    event.getPrecioUnitario() * event.getCantidad()
            ));

        } catch (Exception e) {
            // Registro del error para informar o para debugging
            System.err.println("Error: " + e.getMessage());
            return Optional.empty(); // Retornar vacío si hay un error en la conversión
        }
    }

    @Override
    public CarritoCompras crearCarritoCompras(String idUsuario) throws IOException {
        Optional<CarritoCompras> carritoCompraRecibido = carritoComprasRepo.obtenerPorIdUsuario(idUsuario);

        if (carritoCompraRecibido.isEmpty()) {

            CarritoCompras carritoCompras = new CarritoCompras();
            carritoCompras.setIdUsuario(idUsuario);
            carritoCompras.setFecha(LocalDateTime.now());
            carritoCompras.setId(UUID.randomUUID().toString());//TODO crear metodo que
            carritoCompras.setProductos(new ArrayList<>());
            carritoComprasRepo.guardarCarritoCompra(carritoCompras);
            return carritoCompras;
        } else {
            return carritoCompraRecibido.get();
        }
    }
}
