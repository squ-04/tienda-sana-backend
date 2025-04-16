package co.uniquindio.tiendasana.services.interfaces;

import co.uniquindio.tiendasana.dto.TokenDTO;
import co.uniquindio.tiendasana.dto.cuentadtos.*;
import co.uniquindio.tiendasana.model.documents.Cuenta;
import jakarta.validation.Valid;

import java.util.Map;

public interface CuentaService {

    String crearCuenta(CrearCuentaDTO cuentaDTO) throws Exception;

    String actualizarCuenta(ActualizarCuentaDTO cuentaDTO) throws Exception;

    String eliminarCuenta(String email) throws Exception;

    InfoCuentaDTO obtenerInfoCuenta(String id) throws Exception;


    Cuenta obtenerCuentaPorEmail(String email) throws Exception;

    String enviarCodigoRecuperacion(String email) throws Exception;

    String cambiarContrasenia(CambiarContraseniaDTO dto) throws Exception;

    String validarCodigoRegistro(ActivarCuentaDTO activarCuentaDTO) throws Exception;

    String reenviarCodigoRegistro(String email) throws Exception;

    TokenDTO login(@Valid LoginDTO loginDTO) throws Exception;

    TokenDTO refresh(Map<String, Object> claims);
}
