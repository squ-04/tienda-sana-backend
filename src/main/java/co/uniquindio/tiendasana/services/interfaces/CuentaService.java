package co.uniquindio.tiendasana.services.interfaces;

import co.uniquindio.tiendasana.dto.cuentadtos.*;
import co.uniquindio.tiendasana.model.documents.Cuenta;

public interface CuentaService {

    String crearCuenta(CrearCuentaDTO cuentaDTO) throws Exception;

    String actualizarCuenta(ActualizarCuentaDTO cuentaDTO) throws Exception;

    String eliminarCuenta(String id) throws Exception;

    InfoCuentaDTO obtenerInfoCuenta(String id) throws Exception;

    Cuenta obtenerCuenta(String id) throws Exception;

    Cuenta obtenerCuentaPorEmail(String email) throws Exception;

    String enviarCodigoRecuperacion(String email) throws Exception;

    String cambiarContrasenia(CambiarContraseniaDTO dto) throws Exception;

    String validarCodigoRegistro(ActivarCuentaDTO activarCuentaDTO) throws Exception;

    String reenviarCodigoRegistro(String email) throws Exception;
}
