package co.uniquindio.tiendasana.services.implementations;

import co.uniquindio.tiendasana.dto.carritoComprasdtos.AgregarDetalleCarritoDTO;
import co.uniquindio.tiendasana.dto.carritoComprasdtos.BorrarDetalleCarritoDTO;
import co.uniquindio.tiendasana.dto.carritoComprasdtos.EditarDetalleCarritoDTO;
import co.uniquindio.tiendasana.dto.carritoComprasdtos.VistaItemCarritoDTO;
import co.uniquindio.tiendasana.exceptions.ProductoParseException;
import co.uniquindio.tiendasana.model.documents.CarritoCompras;
import co.uniquindio.tiendasana.model.documents.Producto;
import co.uniquindio.tiendasana.model.vo.DetalleCarrito;
import co.uniquindio.tiendasana.repos.CarritoComprasRepo;
import co.uniquindio.tiendasana.services.interfaces.CarritoComprasService;
import co.uniquindio.tiendasana.services.interfaces.ProductoService;
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

    /**
     * Elimina todos los productos del carrito de compras asociado al usuario dado.
     * @param idUsuario Email del usuario cuyo carrito se va a limpiar.
     * @throws IOException Si ocurre un error al acceder al repositorio.
     */
    @Override
    public void borrarCarritoCompras(String idUsuario) throws IOException {
        CarritoCompras carrito=carritoComprasRepo.obtenerPorIdUsuario(idUsuario).get();
        carritoComprasRepo.eliminarDetalles(carrito.getProductos());
    }

    /**
     * Obtiene el carrito de compras del usuario si existe.
     * @param idUsuario Email del usuario.
     * @return El carrito de compras correspondiente.
     * @throws Exception Si el carrito no existe.
     */
    @Override
    public CarritoCompras getCarritoCompras(String idUsuario) throws Exception {
        Optional<CarritoCompras> shoppingCar = carritoComprasRepo.obtenerPorIdUsuario(idUsuario);
        if (shoppingCar.isEmpty()) {
            throw new Exception("No hay un carrito de compras para el usuario: " + idUsuario);
        }
        return shoppingCar.get();
    }

    /**
     * Agrega un producto al carrito de compras del usuario.
     * Si el carrito no existe, se crea uno nuevo.
     * @param addShoppingCarDetailDTO DTO con la información del producto a agregar.
     * @return El ID del carrito actualizado.
     * @throws IOException            Si hay un error al guardar.
     * @throws ProductoParseException Si ocurre un error relacionado con el producto.
     */
    @Override
    public String agregarDetalleCarrito(AgregarDetalleCarritoDTO addShoppingCarDetailDTO) throws IOException, ProductoParseException {
        CarritoCompras carritoCompras = crearCarritoCompras(addShoppingCarDetailDTO.idUsuario());
        Producto producto = productoService.getProducto(addShoppingCarDetailDTO.idProducto());

        DetalleCarrito detalleCarrito = new DetalleCarrito();
        detalleCarrito.setCantidad(addShoppingCarDetailDTO.cantidad());
        detalleCarrito.setProductoId(addShoppingCarDetailDTO.idProducto());
        detalleCarrito.setIdCarrito(carritoCompras.getId());
        detalleCarrito.setSubtotal(producto.getPrecioUnitario()*addShoppingCarDetailDTO.cantidad());

        carritoCompras.agregarDetalle(detalleCarrito);
        carritoComprasRepo.actualizarCarrito(carritoCompras);
        return carritoCompras.getId();
    }

    /**
     * Modifica la cantidad de un producto en el carrito de compras.
     * Valida la disponibilidad del stock antes de aplicar los cambios.
     * @param editarCarritoDetalleDTO DTO con la nueva cantidad y producto.
     * @return El ID del carrito actualizado.
     * @throws Exception Si el producto no tiene stock o el carrito no existe.
     */
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

    /**
     * Metodo auxiliar para obtener el carrito de compras del usuario.
     * @param idUsuario Email del usuario.
     * @return Carrito de compras encontrado.
     * @throws Exception Si el carrito no existe.
     */
    private CarritoCompras obtenerCarritoCompra(String idUsuario) throws Exception {
        Optional<CarritoCompras> shoppingCar = carritoComprasRepo.obtenerPorIdUsuario(idUsuario);
        if (shoppingCar.isEmpty()) {
            throw new Exception("No hay un carrito de compras para el usuario: " + idUsuario);
        }
        return shoppingCar.get();
    }

    /**
     * Elimina un producto específico del carrito de compras del usuario.
     * @param borrarDetalleCarritoDTO DTO con la información del producto y usuario.
     * @return ID del carrito actualizado.
     * @throws Exception Si el carrito no existe.
     */
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

    /**
     * Lista todos los productos dentro del carrito de compras del usuario.
     * @param emailUsuario Correo electrónico o ID del usuario.
     * @return Lista de DTOs representando los ítems del carrito.
     * @throws IOException Si hay un error al acceder a los datos.
     */
    @Override
    public List<VistaItemCarritoDTO> listarDetallesCarrito(String emailUsuario) throws IOException {
        CarritoCompras shoppingCar = crearCarritoCompras(emailUsuario);
        List<DetalleCarrito> shoppingCarDetails = shoppingCar.getProductos();

        return shoppingCarDetails.stream()
                .map(this::convertToCarItemViewDTO)
                .flatMap(Optional::stream) // Descartar valores nulos si la conversión falló
                .collect(Collectors.toList());
    }

    /**
     * Convierte un detalle del carrito en un DTO para mostrar en la interfaz de usuario.
     * @param itemView Detalle del carrito.
     * @return DTO opcional con información del producto y subtotal.
     */
    private Optional<VistaItemCarritoDTO> convertToCarItemViewDTO(DetalleCarrito itemView) {
        try {

            Producto producto = productoService.getProducto(itemView.getProductoId());

            return Optional.of(new VistaItemCarritoDTO(
                    producto.getId(),
                    producto.getNombre(),
                    producto.getCategoria(),
                    producto.getPrecioUnitario(),
                    itemView.getCantidad(),
                    producto.getPrecioUnitario() * itemView.getCantidad()
            ));

        } catch (Exception e) {
            // Registro del error para informar o para debugging
            System.err.println("Error: " + e.getMessage());
            return Optional.empty(); // Retornar vacío si hay un error en la conversión
        }
    }

    /**
     * Crea un nuevo carrito de compras para el usuario si aún no tiene uno.
     * @param idUsuario ID del usuario.
     * @return El carrito de compras existente o recién creado.
     * @throws IOException Si ocurre un error al guardar.
     */
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
