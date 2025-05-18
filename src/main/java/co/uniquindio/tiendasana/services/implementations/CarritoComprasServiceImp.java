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
import org.springframework.security.access.AccessDeniedException;
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
     * Elimina todos los productos del carrito de compras asociado al usuario autenticado.
     * @param emailAutenticado Email del usuario cuyo carrito se va a limpiar.
     * @throws IOException Si ocurre un error al acceder al repositorio.
     */
    @Override
    public void borrarTodosLosItemsDelCarrito(String emailAutenticado) throws IOException {
        // No se necesita idUsuario como parámetro si siempre es el autenticado
        Optional<CarritoCompras> carritoOpt = carritoComprasRepo.obtenerPorIdUsuario(emailAutenticado);
        if (carritoOpt.isPresent()) {
            CarritoCompras carrito = carritoOpt.get();
            if (!carrito.getIdUsuario().equals(emailAutenticado)) {
                throw new AccessDeniedException("Acceso denegado al carrito de compras.");
            }
            if (carrito.getProductos() != null && !carrito.getProductos().isEmpty()) {
                carritoComprasRepo.eliminarDetalles(new ArrayList<>(carrito.getProductos()));
                carrito.setProductos(new ArrayList<>());
                carritoComprasRepo.actualizarCarrito(carrito);
            }
        }
    }

    /**
     * Obtiene el carrito de compras del usuario si existe y pertenece al usuario autenticado.
     * @param idUsuario Email del usuario objetivo (para la búsqueda).
     * @param emailAutenticado Email del usuario autenticado (para la validación).
     * @return El carrito de compras correspondiente.
     * @throws Exception Si el carrito no existe o no pertenece al usuario autenticado.
     */
    @Override
    public CarritoCompras getCarritoCompras(String idUsuario, String emailAutenticado) throws Exception {
        if (!idUsuario.equals(emailAutenticado)) {
            throw new AccessDeniedException("No tiene permiso para acceder al carrito de este usuario.");
        }
        return obtenerCarritoCompra(emailAutenticado);
    }

    /**
     * Agrega un producto al carrito de compras del usuario autenticado.
     * El idUsuario en addShoppingCarDetailDTO se usa para validar que la intención era para el usuario autenticado.
     * @param addShoppingCarDetailDTO DTO con la información del producto a agregar.
     * @param emailAutenticado Email del usuario autenticado.
     * @return El ID del carrito actualizado.
     * @throws IOException            Si hay un error al guardar.
     * @throws ProductoParseException Si ocurre un error relacionado con el producto.
     * @throws AccessDeniedException Si el idUsuario en el DTO no coincide con el emailAutenticado.
     */
    @Override
    public String agregarDetalleCarrito(AgregarDetalleCarritoDTO addShoppingCarDetailDTO, String emailAutenticado) throws IOException, ProductoParseException {
        if (!addShoppingCarDetailDTO.idUsuario().equals(emailAutenticado)) {
            throw new AccessDeniedException("No tiene permiso para agregar productos al carrito de este usuario.");
        }

        CarritoCompras carritoCompras = crearCarritoCompras(emailAutenticado);
        Producto producto = productoService.obtenerProducto(addShoppingCarDetailDTO.idProducto());

        Optional<DetalleCarrito> detalleExistenteOpt = carritoCompras.getProductos().stream()
                .filter(dc -> dc.getProductoId().equals(addShoppingCarDetailDTO.idProducto()))
                .findFirst();

        if (detalleExistenteOpt.isPresent()) {
            DetalleCarrito detalleExistente = detalleExistenteOpt.get();
            int nuevaCantidad = detalleExistente.getCantidad() + addShoppingCarDetailDTO.cantidad();
            if (!producto.estaStockDisponible(nuevaCantidad - detalleExistente.getCantidad())) { // Validar solo el incremento
                throw new ProductoParseException("Cantidad de stock insuficiente para añadir más unidades de " + producto.getNombre());
            }
            detalleExistente.setCantidad(nuevaCantidad);
            detalleExistente.setSubtotal(producto.getPrecioUnitario() * nuevaCantidad);
        } else {
            if (!producto.estaStockDisponible(addShoppingCarDetailDTO.cantidad())) {
                throw new ProductoParseException("Cantidad de stock insuficiente para " + producto.getNombre());
            }
            DetalleCarrito detalleCarrito = new DetalleCarrito();
            detalleCarrito.setCantidad(addShoppingCarDetailDTO.cantidad());
            detalleCarrito.setProductoId(addShoppingCarDetailDTO.idProducto());
            detalleCarrito.setIdCarrito(carritoCompras.getId());
            detalleCarrito.setSubtotal(producto.getPrecioUnitario() * addShoppingCarDetailDTO.cantidad());
            carritoCompras.agregarDetalle(detalleCarrito);
        }

        carritoComprasRepo.actualizarCarrito(carritoCompras);
        return carritoCompras.getId();
    }

    /**
     * Modifica la cantidad de un producto en el carrito de compras del usuario autenticado.
     * @param editarCarritoDetalleDTO DTO con la nueva cantidad y producto.
     * @param emailAutenticado Email del usuario autenticado.
     * @return El ID del carrito actualizado.
     * @throws Exception Si el producto no tiene stock, el carrito no existe, o no tiene permisos.
     */
    @Override
    public String editarDetalleCarrito(EditarDetalleCarritoDTO editarCarritoDetalleDTO, String emailAutenticado) throws Exception {
        if (!editarCarritoDetalleDTO.idUsuario().equals(emailAutenticado)) {
            throw new AccessDeniedException("No tiene permiso para editar el carrito de este usuario.");
        }

        CarritoCompras carritoCompras = obtenerCarritoCompra(emailAutenticado); // Usa emailAutenticado
        Optional<DetalleCarrito> detalleParaEditarOpt = carritoCompras.getProductos().stream()
                .filter(e -> e.getProductoId().equals(editarCarritoDetalleDTO.idProducto()))
                .findFirst();

        if (detalleParaEditarOpt.isEmpty()) {
            throw new Exception("El producto no se encuentra en el carrito.");
        }

        DetalleCarrito detalleParaEditar = detalleParaEditarOpt.get();
        Producto producto = productoService.obtenerProducto(editarCarritoDetalleDTO.idProducto());

        if (!producto.estaStockDisponible(editarCarritoDetalleDTO.cantidad())) {
            int cantidadActual = detalleParaEditar.getCantidad();
            int diferencia = editarCarritoDetalleDTO.cantidad() - cantidadActual;
            if (diferencia > 0 && !producto.estaStockDisponible(diferencia)) {
                throw new Exception("Cantidad de stock insuficiente para el incremento solicitado.");
            }
            throw new Exception("Cantidad de stock insuficiente para la cantidad total solicitada de " + producto.getNombre());
        }

        detalleParaEditar.setCantidad(editarCarritoDetalleDTO.cantidad());
        detalleParaEditar.setSubtotal(detalleParaEditar.getCantidad() * producto.getPrecioUnitario());

        if (detalleParaEditar.getCantidad() <= 0) {
            carritoCompras.getProductos().remove(detalleParaEditar);
            carritoComprasRepo.eliminarDetalle(detalleParaEditar);
        }

        carritoComprasRepo.actualizarCarrito(carritoCompras);
        return carritoCompras.getId();
    }

    /**
     * Metodo auxiliar para obtener el carrito de compras del usuario.
     * Este método es privado y asume que la validación de emailAutenticado ya se hizo.
     * @param idUsuarioVerificado Email del usuario (ya validado).
     * @return Carrito de compras encontrado.
     * @throws Exception Si el carrito no existe.
     */
    private CarritoCompras obtenerCarritoCompra(String idUsuarioVerificado) throws Exception {
        Optional<CarritoCompras> shoppingCar = carritoComprasRepo.obtenerPorIdUsuario(idUsuarioVerificado);
        if (shoppingCar.isEmpty()) {
            throw new Exception("No hay un carrito de compras para el usuario: " + idUsuarioVerificado);
        }
        return shoppingCar.get();
    }

    /**
     * Elimina un producto específico del carrito de compras del usuario autenticado.
     * @param borrarDetalleCarritoDTO DTO con la información del producto y usuario.
     * @param emailAutenticado Email del usuario autenticado.
     * @return ID del carrito actualizado.
     * @throws Exception Si el carrito no existe o no tiene permisos.
     */
    @Override
    public String borrarItemDelCarrito(BorrarDetalleCarritoDTO borrarDetalleCarritoDTO, String emailAutenticado) throws Exception {
        if (!borrarDetalleCarritoDTO.idUsuario().equals(emailAutenticado)) {
            throw new AccessDeniedException("No tiene permiso para eliminar items del carrito de este usuario.");
        }

        CarritoCompras carritoCompras = obtenerCarritoCompra(emailAutenticado); // Usa emailAutenticado

        List<DetalleCarrito> detallesOriginales = carritoCompras.getProductos();
        List<DetalleCarrito> detallesAEliminar = detallesOriginales.stream()
                .filter(e -> e.getProductoId().equals(borrarDetalleCarritoDTO.idProducto()))
                .collect(Collectors.toList());

        if (detallesAEliminar.isEmpty()) {
            throw new Exception("El producto a eliminar no se encuentra en el carrito.");
        }

        detallesOriginales.removeAll(detallesAEliminar);
        carritoCompras.setProductos(detallesOriginales);

        carritoComprasRepo.eliminarDetalles(detallesAEliminar);

        carritoComprasRepo.actualizarCarrito(carritoCompras);
        return carritoCompras.getId();
    }

    /**
     * Lista todos los productos dentro del carrito de compras del usuario autenticado.
     * @param emailUsuario Correo electrónico del usuario (del path variable, para validación).
     * @param emailAutenticado Email del usuario autenticado (del token).
     * @return Lista de DTOs representando los ítems del carrito.
     * @throws IOException Si hay un error al acceder a los datos.
     * @throws AccessDeniedException Si el emailUsuario no coincide con el emailAutenticado.
     */
    @Override
    public List<VistaItemCarritoDTO> listarDetallesCarrito(String emailUsuario, String emailAutenticado) throws IOException {
        if (!emailUsuario.equals(emailAutenticado)) {
            throw new AccessDeniedException("No tiene permiso para listar los items del carrito de este usuario.");
        }

        CarritoCompras shoppingCar = crearCarritoCompras(emailAutenticado);
        List<DetalleCarrito> shoppingCarDetails = shoppingCar.getProductos();

        if (shoppingCarDetails == null) {
            return new ArrayList<>();
        }

        return shoppingCarDetails.stream()
                .map(this::convertToCarItemViewDTO)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    /**
     * Convierte un detalle del carrito en un DTO para mostrar en la interfaz de usuario.
     * @param itemView Detalle del carrito.
     * @return DTO opcional con información del producto y subtotal.
     */
    private Optional<VistaItemCarritoDTO> convertToCarItemViewDTO(DetalleCarrito itemView) {
        try {

            Producto producto = productoService.obtenerProducto(itemView.getProductoId());

            return Optional.of(new VistaItemCarritoDTO(
                    producto.getId(),
                    producto.getNombre(),
                    producto.getCategoria(),
                    producto.getPrecioUnitario(),
                    itemView.getCantidad(),
                    producto.getPrecioUnitario() * itemView.getCantidad()
            ));

        } catch (Exception e) {
            System.err.println("Error al convertir DetalleCarrito a VistaItemCarritoDTO para producto ID " + itemView.getProductoId() + ": " + e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Crea un nuevo carrito de compras para el usuario si aún no tiene uno.
     * @param idUsuarioVerificado ID del usuario (ya validado o es el emailAutenticado).
     * @return El carrito de compras existente o recién creado.
     * @throws IOException Si ocurre un error al guardar.
     */
    @Override
    public CarritoCompras crearCarritoCompras(String idUsuarioVerificado) throws IOException {
        Optional<CarritoCompras> carritoCompraRecibido = carritoComprasRepo.obtenerPorIdUsuario(idUsuarioVerificado);

        if (carritoCompraRecibido.isEmpty()) {
            CarritoCompras carritoCompras = new CarritoCompras();
            carritoCompras.setIdUsuario(idUsuarioVerificado); // Usa el id verificado
            carritoCompras.setFecha(LocalDateTime.now());
            carritoCompras.setId(UUID.randomUUID().toString());
            carritoCompras.setProductos(new ArrayList<>()); // Inicializa la lista de productos
            carritoComprasRepo.guardarCarritoCompra(carritoCompras);
            return carritoCompras;
        } else {
            CarritoCompras existente = carritoCompraRecibido.get();
            if (existente.getProductos() == null) { // Asegurar que la lista de productos no sea nula
                existente.setProductos(new ArrayList<>());
            }
            return existente;
        }
    }

}
