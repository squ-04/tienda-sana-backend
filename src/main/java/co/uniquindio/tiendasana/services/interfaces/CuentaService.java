package co.uniquindio.tiendasana.services.interfaces;

import co.uniquindio.tiendasana.dto.TokenDTO;
import co.uniquindio.tiendasana.dto.cuentadtos.*;
import co.uniquindio.tiendasana.model.documents.Cuenta;
import jakarta.validation.Valid;

import java.util.Map;

public interface CuentaService {
    /**
     * Metodo que crea una cuenta apartir de la inforamción de un formulario de la vista
     * @param cuentaDTO Data transfer object que tiene la informacion del formulario de registro
     * @return
     * @throws Exception
     */
    String crearCuenta(CrearCuentaDTO cuentaDTO) throws Exception;
    /**
     * Meotod para actualizar los datos de una cuenta dada la información del formulario de actualizacion
     * @param cuentaDTO Data Transfer Object con la información del formulario de actualización
     * @return
     * @throws Exception
     */
    String actualizarCuenta(ActualizarCuentaDTO cuentaDTO) throws Exception;
    /**
     * Metodo para eliminar una cuenta (Eliminación logica mendiante el estado de Eliminada) dado el email
     * @param email el email asociado de cada cuenta se establece como unico en el negocio
     * @return
     * @throws Exception
     */
    String eliminarCuenta(String email) throws Exception;
    /**
     * Método para obtener la inforamción relacionada a una cuenta de un usuario dado un usario
     * @param id el email asociado de cada cuenta se establece como unico en el negocio
     * @return
     * @throws Exception
     */
    InfoCuentaDTO obtenerInfoCuenta(String id) throws Exception;
    /**
     * Método para obtener una cuenta dado el email
     * (Este no es un método llamado por controladores pero que apoya a los servicios que si son llamados)
     * @param email
     * @return
     * @throws Exception
     */
    Cuenta obtenerCuentaPorEmail(String email) throws Exception;
    /**
     * Metodo para enviar un código de recuperación de contraseña y asociarlo a la cuenta del usuario
     * @param email
     * @return
     * @throws Exception
     */
    String enviarCodigoRecuperacion(String email) throws Exception;
    /**
     * Método para cambiar la contraseña actual de una cuenta dado el codigo de recuperación
     * y la nueva contraseña
     * @param dto contiene el correo asociado a al cuenta, la nueva contraseña y el código de recuperación
     * @return
     * @throws Exception
     */
    String cambiarContrasenia(CambiarContraseniaDTO dto) throws Exception;
    /**
     * Método para validar (Activar) una cuenta dado un código de validación en el correo de los usuarios
     * @param activarCuentaDTO, posee el correo asociado a la cuenta y el código de validación indicado por el
     *                          usuario
     * @return
     * @throws Exception
     */
    String validarCodigoRegistro(ActivarCuentaDTO activarCuentaDTO) throws Exception;
    /**
     * Método el cual es usado para reenviar y asociar un nuevo código de valicdación en caso de que el usuario no
     * pudiera validar su cuenta ocn el primer código enviado en el registro
     * @param email, correo asociado a una cuenta, se verificara si la cuenta ya está activa antes de reasignar
     * @return
     * @throws Exception
     */
    String reenviarCodigoRegistro(String email) throws Exception;
    /**
     * Método para iniciar sesión el cual devuelve un JWT con los datos de la cuenta que esta iniciando sesión para
     * que sea almacenada en el sessión storage del frontend
     * @param loginDTO
     * @return
     * @throws Exception
     */
    TokenDTO login(@Valid LoginDTO loginDTO) throws Exception;

    TokenDTO refresh(Map<String, Object> claims);
}
