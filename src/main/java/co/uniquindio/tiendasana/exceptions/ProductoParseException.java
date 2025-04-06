package co.uniquindio.tiendasana.exceptions;

/**
 * Excepcion que será lanzada en el proceso de parsing de los productos
 */
public class ProductoParseException extends Exception {
    /**
     * Metodo constructor de la excepcion
     * @param mensaje
     */
    public ProductoParseException(String mensaje) {
        super(mensaje);
    }
}
